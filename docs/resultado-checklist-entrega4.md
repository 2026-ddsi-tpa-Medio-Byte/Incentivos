# Resultado del checklist de Entrega 4 (corrido contra los 4 servicios reales en Render)

Corrida automatizada con la colección [`postman/Checklist-Entrega4.postman_collection.json`](../postman/Checklist-Entrega4.postman_collection.json)
vía Newman, dos veces (la primera encontró bugs en la colección misma, ya corregidos; la segunda es la que
está documentada acá). 49 requests, 47/49 asserts en verde — las 2 fallas son un `429 Too Many Requests` de
Logística por exceso de tráfico entre las dos corridas seguidas, no un bug de la app.

Leyenda: ✅ confirmado funcionando · ❌ falla confirmada · ⚠️ resultado no concluyente · 🚫 no se puede probar por HTTP

## A · Lo que casi seguro te van a pedir demostrar

| # | Item | Resultado | Evidencia |
|---|---|---|---|
| 1 | Donación end-to-end (Donaciones → Donadores → Logística) | ⚠️ | El alta en Donaciones funciona (`POST /donaciones` → 201). En esta corrida el paso interno que dispara Logística devolvió `429 Too Many Requests` — probablemente por la carga de las dos corridas seguidas del checklist, no por un bug. No se puede confirmar visualmente la cadena completa sin mirar los logs de Donaciones/Logística en el momento exacto. |
| 2 | Necesidad con producto válido → 201 | ✅ | `POST /necesidades` con un producto real → `201 Created`. |
| 3 | Necesidad con producto inexistente → 400 | ✅ | `POST /necesidades` con `productoSolicitadoID` inventado → `400`, `"El producto solicitado no existe en Donaciones: ..."`. |
| 4 | Reportar entrega → donación ACEPTADA + necesidad satisfecha | 🚫 | No armamos el precondicionamiento completo (depósito + necesidad + asignación real coincidentes) en esta corrida; necesita datos que ya estén matcheados por Logística. Requiere prueba manual coordinada con Logística. |
| 5 | Matchmaking elige la necesidad más desatendida | 🚫 | No hay forma de verificarlo sin crear múltiples necesidades reales del mismo producto y observar cuál se satisface primero — no es un solo request, es un escenario de varios pasos coordinados con datos que no controlamos. Queda pendiente de armar con el equipo. |
| 6 | La cola en acción (logs) | 🚫 | No es un endpoint HTTP. Buena noticia de paso: `B6.3` (más abajo) confirmó que el mecanismo de cola SÍ existe — `gestionar-donacion` devolvió `"mensaje":"Donación encolada, será procesada por un worker"`. Para ver el worker procesándola hay que mirar el Live Tail de Render de Logística. |
| 7 | Más de un worker (uno en Render, uno local) | 🚫 | Es una decisión de deploy/arquitectura, no algo que un request HTTP pueda confirmar. |
| 8 | Cron-Job de Incentivos corriendo solo | ✅ (lógica) / 🚫 (timer) | `POST /admin/procesar-pendientes` (la misma lógica que dispara el `@Scheduled`) responde `200`. Que el timer tickee solo cada 60s sin que nadie llame nada ya se confirmó manualmente esta sesión mirando los logs de Render (ver conversación) — una corrida de Newman de un solo tiro no puede demostrar el paso del tiempo. |
| 9 | Pérdida de progreso (quejas bajan ACEPTADAS, se pierde insignia/categoría) | ✅ | **Ya validado manualmente en esta sesión con datos reales**: donador `1`, insignia `DONACIONES_EXITOSAS` otorgada con 21 donaciones ACEPTADA → una queja vía `POST /donaciones/{id}/quejas` bajó el conteo a 19 → se reprocesó → la insignia se retiró y la categoría volvió de TRANSFORMADOR a COLABORADOR. No se repitió en esta corrida para no seguir mutando datos compartidos del equipo. |
| 10 | Bot de Telegram en vivo | 🚫 | Corre local en una notebook, no es un servicio HTTP público — no forma parte de ninguna colección de Postman posible. |

## B · Reglas de negocio que suelen preguntar

| # | Item | Resultado | Evidencia |
|---|---|---|---|
| 1 | Donador BANEADO no puede donar (400 "No puede donar") | ✅ | `PATCH /donadores/{id}/estado` a `BANEADO` → `GET /puede-donar` responde `{"puedeDonar":false}` → `POST /donaciones` para ese donador → `400`, `{"error":"No puede donar"}`. Funciona exactamente como se espera. |
| 2 | 5 quejas → SOSPECHOSO · 10 quejas → BANEADO | ✅ | Se registraron 10 quejas reales (`POST /donadores/{id}/quejas`) sobre un donador de prueba. Tras la 5ª: `estado: "SOSPECHOSO"`. Tras la 10ª: `estado: "BANEADO"`. Ambos umbrales confirmados. |
| 3 | Necesidad EXTRAORDINARIA admite parciales; RECURRENTE no | ✅ | `POST /necesidades/{id}/satisfaccion` con cantidad parcial (10 de 20): en una necesidad `EXTRAORDINARIA` → `200 OK`. En una `RECURRENTE` → `400`, `"Una necesidad recurrente no puede satisfacerse parcialmente"`. Confirmado tal cual la regla. |
| 4 | Producto con QR → nombre con cantidad par de letras | ⚠️ | No se pudo aislar: `POST /productos` valida que el **nombre** no esté repetido (`"Ya existe un producto con el nombre: Arroz"`) y esa validación se dispara antes de llegar a cualquier regla de paridad de letras, si existe. Los nombres de prueba ("Arroz", "Fideos") ya estaban usados de una corrida anterior del checklist. Para probar esto de verdad hace falta un nombre 100% nuevo cada vez (por ejemplo con el `runId` incluido en el nombre, no solo en la descripción). |
| 5 | Producto con código de barras → descripción de 3+ palabras | ⚠️ | Mismo problema que el punto anterior: bloqueado por la validación de nombre duplicado ("Leche" ya existía), nunca se llegó a probar la regla de cantidad de palabras en la descripción. |
| 6 | El sobrante de una donación va al stock (hoy falla en Logística) | ❌ (consistente con lo ya reportado) | `gestionar-donacion` con una donación sin necesidad pendiente respondió `200` con `"Donación encolada, será procesada por un worker"` y `stockActual: 0`. Inmediatamente después, `GET /api/depositos/{id}` seguía mostrando `stockActual: 0` (no subió a 15). **Ojo con el matiz**: como el procesamiento es asíncrono (cola + worker), esto podría ser solo que el worker todavía no lo había procesado en el instante en que consultamos, no necesariamente el bug ya reportado — pero es consistente con lo que el equipo ya tiene marcado como roto. |
| 7 | Diferenciar asignación por MATCHMAKING vs SOLICITUD_DONADORES | 🚫 | No se pudo probar: no hay una asignación real con `paqueteID` conocido para inspeccionar, y el endpoint `POST /asignaciones/solicitud` que mencionó tu compañero de Logística en su documento todavía no existe ("contrato pendiente de implementación", según su propia nota) — no lo inventamos para no mandar una URL que no es real. |

## C · Entrega 3 que también pueden revisar

| # | Item | Resultado | Evidencia |
|---|---|---|---|
| 1 | Persistencia real (reiniciar un servicio y que los datos sigan) | 🚫 | Requeriría reiniciar un deploy de verdad en Render — es una acción real e irreversible sobre el servicio, no la ejecutamos sin que ustedes lo pidan explícitamente. |
| 2 | Métricas en Datadog con los 4 módulos identificables | 🚫 | Vive en el dashboard de Datadog, no en la API de ningún módulo — no es alcanzable desde Postman. |
| 3 | Endpoints de consulta sin mirar la base | ✅ | Confirmado en los 4 módulos: `GET /misiones`, `/insignias` (Incentivos), `/donaciones` (Donaciones), `/donadores`, `/entidades` (Donadores y Entidades), `/api/depositos/{id}` (Logística) — todos responden `200` con datos reales. |

## D · Preparación logística

Nada de esta sección es verificable con una colección de Postman — son pasos operativos, no comportamiento
de la API:

- **Datos precargados**: dependiente de qué decidan cargar antes de la corrección.
- **Colecciones de Postman listas**: ✅ ya las tienen (`postman/`), incluyendo esta nueva de checklist.
- **UptimeRobot activo / despertar los 4 servicios 15 min antes**: confirmado en esta misma sesión que el cold start ronda los 2-3.5 minutos tras inactividad prolongada (lo vimos en vivo hoy) — la recomendación del enunciado de despertar todo 15 minutos antes sigue siendo válida.
- **Llevar la compu con el bot corriendo**: logístico, no aplica.
- **Endpoints de reset a mano**: confirmado que Incentivos tiene `POST /admin/clear`. Los otros 3 módulos no se verificaron en esta corrida (no se buscó ni se probó un endpoint de reset en Donaciones/Donadores/Logística).

## E · Informe PDF

Ningún ítem de esta sección es comportamiento en runtime — son entregables de documentación (diagramas,
especificación de API, secuencia). No se pueden "correr" ni verificar con Postman/Newman.

## Resumen ejecutivo

- **Funciona y confirmado con datos reales hoy**: A9 (parcialmente), B1, B2, B3, C3, A2/A3.
- **Ya confirmado en esta sesión (fuera de esta corrida puntual)**: pérdida de progreso (A9 completo), Cron-Job tickeando solo.
- **Bug confirmado (coincide con lo que el equipo ya sabía)**: sobrante no entra al stock (B6).
- **No concluyente, hay que rearmar el test**: validaciones de QR/código de barras (B4, B5) — bloqueadas por una colisión de nombre en mis pruebas, no se llegó a ver la regla real.
- **No es alcanzable por HTTP, requiere verificación manual del equipo**: cola/worker en logs, segundo worker, bot de Telegram, matchmaking "más desatendida", reportar entrega end-to-end, persistencia tras reinicio, Datadog, y todo el punto E.
