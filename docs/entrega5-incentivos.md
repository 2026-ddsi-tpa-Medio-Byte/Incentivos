# Entrega 5 — Estado y guía del componente Incentivos

## Alcance de este repositorio

Este repositorio implementa el componente **Incentivos**. Las APIs de Donaciones, Donadores y Entidades, Logística y el bot de Telegram de equipo viven fuera de este repositorio; por eso sus despliegues y su aceptación end-to-end requieren coordinación con sus responsables.

## Servidor MCP local para Claude Desktop

El servidor MCP se encuentra en `mcp-server/`. Usa el SDK oficial de MCP para Node.js y se comunica por `stdio` con Claude Desktop. Las herramientas llaman a la API HTTP existente de Incentivos: las reglas siguen en el servicio Java y no se duplican en el MCP.

Herramientas expuestas (16 en total):

- Consultar, listar y crear insignias.
- Actualizar y eliminar insignias.
- Consultar, listar y crear misiones.
- Actualizar y eliminar misiones.
- Consultar misión actual e insignias de un donador.
- Listar perfiles del componente Incentivos.
- Asignar una misión existente a un donador.
- Otorgar una insignia existente a un donador.
- Procesar un donador o el batch de donadores pendientes.

Requisitos locales: Node.js 20 o superior y la aplicación Incentivos disponible en `http://localhost:8080` (o configurar `INCENTIVOS_API_URL`).

### Instalación y pruebas

Desde `mcp-server/` ejecutar `npm ci` y luego `npm test`. Para iniciar manualmente, usar `npm start`. No escribir mensajes de diagnóstico en stdout del servidor MCP: ese canal transporta el protocolo.

### Claude Desktop en Windows

La configuración de Claude Desktop está en `%APPDATA%\Claude\claude_desktop_config.json`. Agregar un servidor usando rutas absolutas a `node.exe` y a `mcp-server/src/index.js`; configurar `INCENTIVOS_API_URL` como `http://localhost:8080`. La ruta de Node se puede obtener en PowerShell con `where.exe node`. Cerrar y volver a abrir Claude Desktop después de guardar la configuración. En Claude Desktop, comprobar que aparezca `donatrack-incentivos` y probar primero una herramienta de consulta antes de las que modifican datos.

## Estado de requisitos según evidencia disponible

- **MCP de Incentivos:** implementado localmente en este repositorio; sus pruebas unitarias/protocolo deben pasar. Falta conectar y hacer la demo desde Claude Desktop contra la API disponible.
- **Logs centralizados:** hay un appender HTTP orientado a Datadog y logs de operaciones de Incentivos. La visibilidad de aceptación en Datadog debe comprobarse con credenciales/sitio válidos; un HTTP exitoso de la API de Incentivos no demuestra por sí mismo la ingesta de logs.
- **Métricas y alarmas:** ya están instrumentados contadores y duración de procesamiento, misiones completadas y pérdidas de progreso. La exportación Micrometer/Datadog está desactivada por defecto; activar en Render y crear/validar los monitores JSON en la organización Datadog.
- **Seis flujos integrados:** requieren los cuatro servicios y datos coordinados; este repositorio no permite afirmar que todos pasen end-to-end. Consultar `resultado-checklist-entrega4.md` para las pruebas previas y sus límites.
- **Bot de Telegram sobre todos los componentes:** no existe en este repositorio; su código y token/configuración pertenecen al bot/equipo. No se afirma como implementado aquí.
- **Documentación integrada:** los diagramas actuales de este repo son locales al componente y no reemplazan el modelo, despliegue, API y seis secuencias de los cuatro módulos.

## Seguridad

No guardar API keys de Datadog, tokens de Telegram ni credenciales en el repositorio, en la configuración compartida de Claude ni en el chat. Inyectar secretos desde el entorno local o un gestor de secretos.
