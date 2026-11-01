# Entrega 6 — Eventos de Incentivos para el modelo de lectura (CQRS)

Incentivos publica un evento en RabbitMQ cada vez que cambia su estado. El Servicio de Consultas
los consume y arma en MongoDB la ficha del donador («insignias» y «misión en curso: 14 de 20») y
la consulta de calidad de lo donado. La lógica de Incentivos y su comunicación con los otros
módulos no cambian: solo se agregó la publicación.

## Implementación

| Clase | Responsabilidad |
|---|---|
| `eventos/EventoIncentivos` | El sobre común a los cuatro módulos (`eventoId`, `tipo`, `modulo`, `entidadId`, `ocurridoEn`, `datos`). |
| `eventos/EventosIncentivos` | El catálogo: arma el `datos` de cada tipo con el estado completo y actual del objeto. |
| `eventos/PublicadorEventos` | El transporte: exchange topic, JSON UTF-8, `content-type: application/json`, mensajes persistentes. |
| `eventos/RepublicadorEventos` | `POST /admin/republicar-eventos`: vuelve a publicar todo el estado actual. |
| `eventos/EventosConfig` | Declara el exchange (topic, durable) si todavía no existe. |

Reglas que cumple el publicador:

- **Después de guardar.** Si hay una transacción en curso, el envío se registra para después del
  commit (`TransactionSynchronization.afterCommit`, equivalente a
  `@TransactionalEventListener(phase = AFTER_COMMIT)`). Un cambio que termina en rollback —por
  ejemplo, un procesamiento que falla— nunca se anuncia.
- **Publicar nunca hace fallar la operación.** El envío corre en un hilo aparte
  (`eventos-incentivos`) y cualquier error queda en un WARN. Con el broker caído, las operaciones
  responden igual y en el mismo tiempo. Un único hilo mantiene el orden de los cambios.
- **Fechas en UTC** con milisegundos y `Z` (`2026-10-26T18:32:10.123Z`), siempre con tres
  decimales.
- **Trazabilidad:** el mensaje lleva el header `X-Trace-Id` de la operación que lo originó.

## Catálogo

Exchange `donatrack.eventos` (topic, durable), confirmado por el grupo el 26/10. Routing key con
el formato `<modulo>.<objeto>.<acción>`, igual que Logística: la última palabra del tipo es la
acción y lo anterior, el objeto.

| Tipo | Routing key |
|---|---|
| `INSIGNIA_CREADA` / `_MODIFICADA` / `_ELIMINADA` | `incentivos.insignia.creada` / `.modificada` / `.eliminada` |
| `MISION_CREADA` / `_MODIFICADA` / `_ELIMINADA` | `incentivos.mision.creada` / `.modificada` / `.eliminada` |
| `PROGRESO_DONADOR_ACTUALIZADO` | `incentivos.progreso_donador.actualizado` |
| `MODULO_RESETEADO` | `incentivos.modulo.reseteado` |

Incentivos solo publica: no crea colas de eventos (las crea el Servicio de Consultas).

| Tipo | Cuándo | `entidadId` | `datos` |
|---|---|---|---|
| `INSIGNIA_CREADA` | `POST /insignias`, `POST /api/insignias`, o una insignia que se da de alta al otorgarla | id de la insignia | `{id, nombre, descripcion}` |
| `INSIGNIA_MODIFICADA` | `PUT /api/insignias/{id}` | id de la insignia | `{id, nombre, descripcion}` |
| `INSIGNIA_ELIMINADA` | `DELETE /api/insignias/{id}` | id de la insignia | `{id}` |
| `MISION_CREADA` | `POST /misiones`, `POST /api/misiones`, o una misión que se da de alta al asignarla | id de la misión | `{id, nombre, insigniaID, categoriaInicio, categoriaFin, tipo}` |
| `MISION_MODIFICADA` | `PUT /api/misiones/{id}` | id de la misión | igual que `MISION_CREADA` |
| `MISION_ELIMINADA` | `DELETE /api/misiones/{id}` | id de la misión | `{id}` |
| `PROGRESO_DONADOR_ACTUALIZADO` | `POST /donadores/{id}/mision-actual`, `POST /donadores/{id}/procesar` (y cada donador que procesa el Cron-Job), `POST /donadores/{id}/insignias` | id del donador (el mismo de Donadores) | `{donadorId, insignias: [{id, nombre}], misionEnCurso}` |
| `MODULO_RESETEADO` | `POST /admin/clear` | `"*"` | `{}` |

`PROGRESO_DONADOR_ACTUALIZADO`:

- `insignias` son **todas** las que tiene el donador en ese momento.
- `misionEnCurso` es `null` si no tiene misión; si no, `{id, nombre, tipo, progreso, objetivo}`.
- Un procesamiento publica **un solo** evento al final, gane o pierda una insignia o solo cambie su
  progreso. Si el procesamiento falla (por ejemplo, el donador no tiene donaciones), no se publica.
- **No incluye la categoría del donador.** Aunque Incentivos la cambia al completar una misión
  (`PATCH /donadores/{id}/categoria`), el dueño de ese dato es Donadores y Entidades, que publica su
  propio evento.

### Progreso y objetivo por tipo de misión

Se calculan con las mismas reglas que usa Incentivos para decidir si la misión se cumplió: una
misión se completa exactamente cuando `progreso >= objetivo` (lo verifica `ProgresoMisionTest`).

| Tipo | `progreso` | `objetivo` |
|---|---|---|
| `DONACIONES_EXITOSAS` | donaciones en estado ACEPTADA | 20 |
| `COMPLETITUD` | productos distintos donados | 3 |
| `DONACIONES_ASCENDENTES` | largo de la racha de cantidades estrictamente crecientes que termina en la última donación (hasta 5). Con 1, 2, 3 lleva 3; si la siguiente es menor, vuelve a 1 | 5 |
| `REVOLUCION_DONADORA` | donaciones de más de 50 unidades | 10 |

Si Donaciones no responde al calcularlo, `progreso` se publica como `null` (desconocido) en vez de
un 0 que el modelo de lectura mostraría como dato real; el siguiente procesamiento del Cron-Job lo
corrige. `objetivo` siempre está presente.

## Configuración

Variables de entorno en Render (nunca en el código ni en el repo):

| Variable | Qué es |
|---|---|
| `RABBITMQ_URL` | Conexión al RabbitMQ del grupo (la misma que usa Logística), por ejemplo `amqps://usuario:clave@host/vhost`. |
| `EVENTOS_EXCHANGE` | Nombre del exchange de eventos (topic). Por defecto `donatrack.eventos`. **Nunca** la cola de trabajo de los workers de Logística. |

Si el exchange ya existe con otros atributos, el error de declaración se ignora y se publica en el
existente. El health de Actuator no depende de RabbitMQ (`management.health.rabbit.enabled=false`).

## Republicar el estado actual

`POST /admin/republicar-eventos` publica `INSIGNIA_CREADA` por cada insignia, `MISION_CREADA` por
cada misión y `PROGRESO_DONADOR_ACTUALIZADO` por cada donador con misión o insignias, con
`ocurridoEn` = ahora. Responde cuántos se publicaron, por ejemplo
`{"insignias": 1, "misiones": 1, "donadores": 3}`.

## Cómo verificarlo en RabbitMQ (prueba real)

1. En el panel de administración del RabbitMQ del grupo: **Queues → Add a new queue**, por ejemplo
   `prueba-incentivos` (durable). En la cola: **Bindings → From exchange** = `donatrack.eventos`,
   **Routing key** = `incentivos.#`.
2. Contra Incentivos en Render: crear una insignia, crear y asignar una misión y procesar al donador.
3. En la cola: **Get messages** → copiar el *Payload* de cada mensaje para el informe.
4. Borrar la cola de prueba al terminar, para que no acumule mensajes.

## Ejemplos

Formato generado por el código (los JSON reales para el informe se copian del panel en el paso 3):

```json
{
  "eventoId": "8f14e45f-ceea-4e1b-9a6c-2b7e5c1d3a90",
  "tipo": "PROGRESO_DONADOR_ACTUALIZADO",
  "modulo": "incentivos",
  "entidadId": "3",
  "ocurridoEn": "2026-10-26T18:32:10.123Z",
  "datos": {
    "donadorId": "3",
    "insignias": [ { "id": "ins-4340", "nombre": "Donaciones Exitosas" } ],
    "misionEnCurso": { "id": "mis-4340", "nombre": "Donaciones Exitosas",
                       "tipo": "DONACIONES_EXITOSAS", "progreso": 14, "objetivo": 20 }
  }
}
```

```json
{ "eventoId": "…", "tipo": "MODULO_RESETEADO", "modulo": "incentivos", "entidadId": "*",
  "ocurridoEn": "2026-10-26T18:40:00.000Z", "datos": {} }
```

## Tests

- `EventosIncentivosTest` (13): cada operación del catálogo publica su evento con el sobre completo
  (campos exactos, UUID, fecha UTC con milisegundos, routing key, `content-type`, mensaje
  persistente); un procesamiento que falla no publica; con RabbitMQ caído las operaciones responden
  bien.
- `ProgresoMisionTest` (4): progreso y objetivo de cada tipo, y que coinciden con la evaluación.
- `PublicadorEventosTest` (2): el envío no bloquea a quien publica, y una falla en segundo plano no
  se propaga.
