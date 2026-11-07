# Datadog — métricas de negocio y alarmas de Incentivos

## Métricas emitidas por el componente

Micrometer registra las métricas con tags comunes `application:incentivos`, `service:incentivos` y el valor de `DD_ENV`.

| Métrica | Tipo | Tags propios | Significado |
|---|---|---|---|
| `incentivos.donador.procesamientos` | counter | `resultado` | Resultado de cada procesamiento: `mision_completada`, `mision_no_completada`, `donacion_no_aceptada` o `error`. |
| `incentivos.donador.procesamiento.duracion` | timer | `resultado` | Duración de cada intento de procesamiento. |
| `incentivos.misiones.completadas` | counter | `tipo` | Misiones completadas agrupadas por tipo de misión. |
| `incentivos.progreso.perdido` | counter | — | Regresiones de progreso por quejas/donaciones aceptadas insuficientes. |

Las métricas se exportan solo cuando `DD_METRICS_ENABLED=true`. En Render configurar `DD_API_KEY`, `DD_SITE`, `DD_ENV=prod`, `DD_SERVICE=incentivos` y `DD_METRICS_ENABLED=true` como variables protegidas. No agregar valores de claves al repositorio. El default permanece deshabilitado para que tests/desarrollo sin credenciales no intenten exportar métricas.

## Crear alarmas

Los JSON `datadog-monitor-*.json` de esta carpeta contienen los cuerpos para crear monitores de tipo `query alert`. También se pueden crear desde Datadog → **Monitors → New Monitor → Metric**, copiando la query correspondiente.

**Notificaciones:** el mensaje de cada monitor termina con `@taraujo@frba.utn.edu.ar`. En Datadog, un `@mail` dentro del mensaje hace que la alerta (y su recuperación) se envíe por mail a esa dirección. Para sumar destinatarios, agregar más `@mail` al final del `message` de cada JSON, o pasarlos al script con `-Notificar "@otro@mail.com"`.

Para crearlos o actualizarlos todos de una vez (el script es idempotente):

```powershell
$env:DD_API_KEY = "..."; $env:DD_APP_KEY = "..."; $env:DD_SITE = "us5.datadoghq.com"
.\docs\datadog-crear-monitores.ps1 -SoloValidar   # opcional: solo valida contra la API
.\docs\datadog-crear-monitores.ps1
```

- Errores de negocio/servicios aguas arriba: `sum(last_5m):sum:incentivos.donador.procesamientos{application:incentivos,env:prod,resultado:error}.as_count() > 0`
- Regresiones de progreso: `sum(last_15m):sum:incentivos.progreso.perdido{application:incentivos,env:prod}.as_count() > 0`
- Ausencia de misiones completadas: `sum(last_1h):sum:incentivos.misiones.completadas{application:incentivos,env:prod}.as_count() < 1`. Este monitor puede alertar en horas sin donaciones; ajustarlo al horario/volumen real o no activarlo si no aplica.

## Verificación

1. Desplegar el cambio y confirmar en **Metrics → Explorer** que aparece `incentivos.donador.procesamientos` con `service:incentivos` y `env:prod`.
2. Ejecutar un procesamiento válido de donador y observar el contador, el resultado y la duración.
3. Crear los monitores después de que las métricas existan y configurar sus notificaciones.
4. Revisar **Monitor Status** y confirmar que cada monitor evalúa una serie real, no `No Data`.

La creación de métricas en código está cubierta por pruebas unitarias. La recepción de métricas y alertas en Datadog requiere credenciales de la organización, despliegue actualizado y validación en la UI; no se debe declarar demostrada hasta verlas allí.
