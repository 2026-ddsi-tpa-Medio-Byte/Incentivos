# Demo Entrega 6 — flujos trazados y alarmas

Cómo mostrarle al profesor, con evidencia, que los flujos atraviesan los módulos y que las alarmas
se disparan y notifican. Todo pasa por el **API Gateway** y cada flujo lleva **su propia traza**, así
en Datadog se ve el recorrido completo de cada uno por separado.

Archivos: `postman/Demo-Entrega6-Trazas.postman_collection.json` y
`postman/Demo-Entrega6-Trazas.postman_environment.json`.

## Por qué funciona la traza por flujo

El gateway respeta el `X-Trace-Id` que le manda el cliente y lo reenvía al módulo (verificado: con
`X-Trace-Id: demo-prueba-123` la respuesta vuelve con `x-trace-id: demo-prueba-123`). Desde ahí cada
módulo lo propaga solo:

- en sus logs (MDC `traceId`, que llega a Datadog como `@traceId`);
- en cada llamada HTTP a otro módulo (interceptor del RestTemplate);
- en el mensaje de la cola `cola-donaciones` (el worker de Logística lo recupera);
- en cada **evento** que publica en `donatrack.eventos` (header `X-Trace-Id`).

La colección genera al empezar un id de corrida (`run`, 5 caracteres) y le pone a cada flujo la
traza `demo-<run>-<flujo>`:

| Traza | Flujo | Módulos que deberían aparecer en Datadog |
|---|---|---|
| `demo-<run>-precondiciones` | Despertar servicios, producto, donador, entidad, depósito | los 4 + gateway |
| `demo-<run>-f5-necesidad` | 5 · Registrar necesidad | Donadores → Donaciones (producto) → Logística (stock) |
| `demo-<run>-f1-donacion` | 1 · Realizar donación | Donaciones → Donadores (puede-donar) → Logística (encola) → **Logística worker** → Donadores (necesidades) |
| `demo-<run>-f2-entrega` | 2 · Reportar entrega | Logística → Donaciones (ACEPTADA) → Donadores (satisfacción) |
| `demo-<run>-f4-mision` | Alta de insignia/misión y asignación | Incentivos → Donadores |
| `demo-<run>-f4-procesar` | 4 · Procesar donador | Incentivos → Donadores → Donaciones → Donadores (PATCH categoría) |
| `demo-<run>-f6-estadisticas` | 6 · Estadísticas (síncrono) | Donadores → Incentivos |
| `demo-<run>-f3-queja` | 3 · Queja | Donaciones → Donadores |
| `demo-<run>-f7-modelo-lectura` | Consultas CQRS (ficha, urgentes, depósitos, calidad, estado) | gateway → Servicio de Consultas |
| `demo-<run>-alarma-fallo-procesar` | Dispara «fallos al procesar donadores» | Incentivos → Donadores (404) |
| `demo-<run>-alarma-perdida-progreso` | Dispara «pérdida de progreso» | Incentivos → Donadores → Donaciones |
| `demo-<run>-alarma-donaciones-rechazadas` | Dispara «pico de donaciones rechazadas» | Donaciones → Donadores (403) |

## Antes de la clase (15 min antes)

1. UptimeRobot activo sobre los 4 módulos, el gateway y `servconsultas` (con `/actuator/health`).
2. En Postman: **Import** de los dos archivos y elegir el entorno **DonaTrack - Gateway (Entrega 6)**.
   Si la cuenta de Datadog no está en `us5.datadoghq.com`, cambiar `ddSite` en el entorno.
3. Abrir en pestañas: Datadog **Logs**, **Monitors** (filtrar `service:incentivos` y `[DonaTrack]`),
   el panel de **RabbitMQ** y el mail donde llegan las alertas.
4. Opcional, para mostrar los eventos: en RabbitMQ **Queues → Add a new queue** `demo-eventos`
   (durable) → **Bindings**: exchange `donatrack.eventos`, routing key `#`. Borrarla al terminar.

## Correr la demo

**Runner** de Postman sobre la colección, en orden, 1 iteración, con *Delay* de 300 ms. Tarda de 1 a
3 minutos según cuántos servicios estén dormidos. Las requests que esperan algo asincrónico se
repiten solas: la del worker de Logística (hasta 10 veces cada 3 s) y las del modelo de lectura
(hasta 5 veces, por consistencia eventual). En la consola se ve cada reintento.

Al final, la request **Z1** abre en la pestaña **Visualize** una tabla con un link a Datadog por
cada traza, más uno con `@traceId:demo-<run>-*` que trae toda la corrida.

También se puede ir carpeta por carpeta (botón *Run* de cada carpeta) para explicar un flujo a la
vez: la carpeta `00` siempre primero, porque crea los datos y las trazas.

## Qué mostrar en Datadog para cada flujo

En **Logs → Explorer**:

1. Query `@traceId:demo-<run>-f1-donacion` (o el link de Z1).
2. Ordenar por fecha ascendente: se lee el flujo en el orden en que ocurrió.
3. **Group by → service** (o la columna `service`): muestra qué módulos participaron. En el flujo 1
   tienen que aparecer Donaciones, Donadores y Logística, incluido el log `[WORKER]` que corre
   **asincrónico**, con la misma traza, después de que el cliente ya recibió el 201.
4. En Incentivos, cada llamada saliente aparece como
   `[Incentivos -> <host>] GET status=200 took=…ms`: se ve a qué módulo llamó, con qué resultado y
   cuánto tardó.
5. Para mostrar un error con traza: `@traceId:demo-<run>-alarma-fallo-procesar` muestra el 404 de
   Donadores y el WARN de Incentivos.

Eventos (CQRS): en la cola `demo-eventos`, **Get messages**. Cada mensaje tiene en *Properties →
headers* el `X-Trace-Id` del flujo que lo generó, la routing key (`donaciones.donacion.registrada`,
`logistica.asignacion.completada`, `incentivos.progreso_donador.actualizado`, …) y el sobre común.

## Alarmas: cómo dispararlas y qué esperar

| Monitor | Lo dispara | Cuándo llega el mail |
|---|---|---|
| Incentivos - fallos al procesar donadores | **A1**: procesar un donador inexistente (404, métrica `resultado:error`) | En la próxima evaluación (ventana de 5 min). Se recupera solo a los 5 min. |
| Incentivos - pérdida de progreso de donadores | **A2**: el donador tiene la insignia de una misión DONACIONES_EXITOSAS pero menos de 20 donaciones ACEPTADA → al procesarlo la pierde | Ventana de 15 min. |
| Incentivos - sin misiones completadas | Está en alerta si no hubo misiones completadas en 1 h; **F4** completa una | Se ve la **recuperación** (mail de *Recovered*). |
| Incentivos - Cron-Job detenido o servicio caído | No se dispara a propósito en clase: habría que dejar Incentivos sin procesar 10 min | Mostrar el historial del monitor o **Test Notifications** en la edición del monitor. |
| [DonaTrack] Pico inusual de donaciones rechazadas | **A3**: 5 donaciones de un donador BANEADO (403, `donaciones.errores`) | Depende del umbral que configuró el responsable de Donaciones: si no alcanza, repetir la carpeta A3. |

Notas:

- **A2 usa un atajo honesto**: en vez de cargar 20 donaciones entregadas y una queja, le otorga la
  insignia a mano (`POST /donadores/{id}/insignias`) y procesa. La regla que se ejecuta es la real:
  `revisarPerdidaDeProgreso` cuenta las ACEPTADA en Donaciones, ve menos de 20, retira la insignia,
  vuelve a la categoría de inicio y suma `incentivos.progreso.perdido`. Decirlo así en la demo.
- Los mails salen porque cada monitor tiene `@taraujo@frba.utn.edu.ar` al final del mensaje. Para
  que lleguen a otra casilla, agregar otro `@mail` en Datadog.
- Si no se quiere esperar la ventana, **Test Notifications** en cada monitor manda el mail en el
  momento (muestra el canal de notificación, no el disparo real).
- Mientras esperan las ventanas de 5 y 15 min, mostrar los flujos en Logs: para cuando terminan,
  los monitores ya están en ALERT.

## Si algo falla

- **429 o 502 en la carpeta 00**: un servicio está despertando. Esperar 30 s y volver a correr la
  carpeta `00`.
- **El worker no asigna (F1.04–06 agotan los reintentos)**: mirar `@traceId:demo-<run>-f1-donacion`
  en Datadog; si no hay log `[WORKER]`, revisar la cola en RabbitMQ (mensajes en `cola-donaciones` o
  en la DLQ).
- **F4 responde 404**: el donador no existe para Incentivos. Verificar que `DONADORES_Y_ENTIDADES_URL`
  apunte a `-msg7` con `GET https://incentivos-wtbd.onrender.com/actuator/metrics/http.client.requests`
  (tag `client.name`).
- **F7 agota los reintentos**: el modelo de lectura no está al día. `GET {gw}/consultas/estado`
  muestra el atraso; se resincroniza con `POST /admin/republicar-eventos` en cada módulo (o
  `resincronizar_modelo_lectura` desde el MCP).
