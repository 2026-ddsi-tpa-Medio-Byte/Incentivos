# Entrega 4 — Módulo Incentivos

Guía de paths y pasos para probar el módulo Incentivos end-to-end. Pensada para compartir
con los compañeros de los otros módulos (Donaciones, Donadores y Entidades, Logística), ya
que varios pasos dependen de datos que viven en esos servicios.

Deploy de este módulo: `https://incentivos-wtbd.onrender.com`

## 1. Paths ya probados (flujo base: crear misión/insignia y asignarlas a un donador)

Todos son contra `https://incentivos-wtbd.onrender.com` salvo que se indique lo contrario.

| Paso | Método | Path | Body |
|---|---|---|---|
| Crear misión | POST | `/misiones` | `{"id","nombre","insigniaID","categoriaInicio","categoriaFin","tipo"}` |
| Crear insignia | POST | `/insignias` | `{"id","nombre","descripcion"}` |
| Asignar misión a donador | POST | `/donadores/{donadorID}/mision-actual` | MisionDTO completo |
| Procesar donador | POST | `/donadores/{donadorID}/procesar` | (sin body) |
| Ver insignias del donador | GET | `/donadores/{donadorID}/insignias` | — |
| Ver misión en curso del donador | GET | `/donadores/{donadorID}/mision-actual` | — |

Precondiciones que dependen de **otros módulos** (no de Incentivos):
- El `donadorID` tiene que existir en el módulo **Donadores y Entidades**
  (`GET {donadoresYEntidadesBaseUrl}/donadores/{id}`).
- `procesar` evalúa la **primera** donación que devuelve el módulo **Donaciones**
  para ese donador (`GET {donacionesBaseUrl}/donaciones?donadorID={id}`), y solo sigue
  si esa donación está en estado `ACEPTADA`.
- Los criterios de cada `tipo` de misión (ver `MisionEvaluatorService`) se evalúan sobre
  el historial completo de donaciones del donador en el módulo Donaciones:
  - `COMPLETITUD`: 3 `productoID` distintos.
  - `DONACIONES_EXITOSAS`: 20+ donaciones en estado `ACEPTADA`.
  - `DONACIONES_ASCENDENTES`: últimas 5 donaciones con cantidad ascendente.
  - `REVOLUCION_DONADORA`: 10+ donaciones con cantidad > 50.

## 2. Nuevo en esta entrega (Módulo Incentivos)

### 2.1 Cron-Job de procesamiento periódico

Corre automáticamente cada **60 segundos** (configurable con la env var
`INCENTIVOS_CRON_INTERVALO_MS`, en milisegundos). Busca todos los donadores que tienen
una misión asignada (`misionActualID` no nulo) y les corre la misma lógica de
`POST /donadores/{id}/procesar` a cada uno. Si falla el procesamiento de alguno (por
ejemplo, porque no tiene donaciones cargadas), no frena al resto del batch.

Para no depender de esperar el intervalo durante la corrección, hay un endpoint para
dispararlo manualmente:

```
POST https://incentivos-wtbd.onrender.com/admin/procesar-pendientes
```
Sin body. Devuelve `200 OK` una vez que terminó de recorrer a todos los donadores
pendientes.

### 2.2 Pérdida de progreso

Caso cubierto (el único pedido por la cátedra para esta entrega): un donador que ya
cumplió la misión `DONACIONES_EXITOSAS` (20+ donaciones `ACEPTADA`) recibe una o más
**quejas** que hacen bajar sus donaciones `ACEPTADA` por debajo de 20. La próxima vez
que se lo procesa (manual, vía `/procesar`, o por el Cron-Job), el sistema:

1. Le saca la insignia que había ganado con esa misión.
2. Lo vuelve a la `categoriaInicio` de esa misión, tanto en el perfil de Incentivos
   como en el módulo Donadores y Entidades (`PATCH` de categoría vía
   `modifcarCategoria`).
3. Vuelve a dejar esa misión como "en curso" (`misionActualID`), para que la tenga que
   volver a cumplir.

Esto vive en `Fachada.revisarPerdidaDeProgreso(donadorID)`, que se ejecuta al principio
de cada `procesarDonador`.

## 3. Cómo armar la demo con el equipo

Para mostrar el caso de pérdida de progreso hace falta coordinar con quien tenga el
módulo Donaciones y con quien tenga Donadores y Entidades, porque ninguno de esos pasos
se puede hacer solo desde Incentivos:

1. **Donadores y Entidades**: tener un donador verificado (`GET /donadores/{id}`).
2. **Donaciones**: cargarle a ese donador 20 donaciones en estado `ACEPTADA` (con
   `productoID` los que sea).
3. **Incentivos**: crear una misión `tipo: DONACIONES_EXITOSAS` con una insignia, asignarla
   al donador (`POST /donadores/{id}/mision-actual`) y procesarlo
   (`POST /donadores/{id}/procesar`). Confirmar con `GET /donadores/{id}/insignias` que
   ahora tiene la insignia.
4. **Donaciones/Donadores y Entidades**: generar una queja sobre alguna de esas
   donaciones, de forma que el conteo de `ACEPTADA` baje de 20 (según cómo lo hayan
   implementado ustedes: puede ser cambiar el estado de la donación, o que el módulo de
   Donaciones descuente del conteo al recibir la queja).
5. **Incentivos**: volver a procesar al donador (`POST /donadores/{id}/procesar`, o
   esperar al Cron-Job, o disparar `POST /admin/procesar-pendientes`). Confirmar con
   `GET /donadores/{id}/insignias` que la insignia desapareció, y con la categoría del
   donador en Donadores y Entidades que volvió a la categoría de inicio de la misión.

## 4. Endpoints administrativos útiles para la corrección

- `POST /admin/clear` — borra insignias, misiones y perfiles (útil para arrancar con la
  base limpia antes de la demo).
- `GET /admin/perfiles`, `GET /admin/misiones`, `GET /admin/insignias` — inspección rápida
  sin pasar por los DTOs de la fachada.
- `POST /admin/procesar-pendientes` — dispara el batch del Cron-Job on-demand.
