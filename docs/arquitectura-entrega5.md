# Arquitectura propuesta — Entrega 5

Este diagrama extiende el componente Incentivos con el MCP local y observabilidad. Los servicios que no están en este repositorio se muestran como dependencias externas; verificar sus URLs/contratos con el equipo.

```mermaid
flowchart LR
    Claude[Claude Desktop]
    MCP[Servidor MCP local<br/>Node.js + SDK oficial<br/>stdio]
    Inc[Incentivos<br/>Spring Boot REST]
    DB[(PostgreSQL<br/>Render / H2 local)]
    Don[Donaciones<br/>servicio externo]
    DE[Donadores y Entidades<br/>servicio externo]
    Log[Logística<br/>servicio externo]
    Telegram[Bot Telegram<br/>fuera de este repo]
    DD[Datadog<br/>Logs + Metrics + Monitors]

    Claude <-->|MCP sobre stdio| MCP
    MCP -->|HTTP /insignias, /misiones,<br/>/donadores/...| Inc
    Inc <--> DB
    Inc -->|REST| Don
    Inc -->|REST| DE
    Inc -->|REST| Log
    Inc -->|Logback HTTP intake| DD
    Inc -->|Micrometer HTTP metrics| DD
    Telegram -.->|integración global pendiente| Don
    Telegram -.-> DE
    Telegram -.-> Inc
    Telegram -.-> Log
```

## Límites de implementación

- El proceso MCP es local y solo expone herramientas del dominio Incentivos. Las herramientas llaman a la API REST y no duplican reglas de negocio.
- El MCP usa `INCENTIVOS_API_URL` para elegir la API local o el despliegue.
- El bot, los otros tres repositorios y la topología real de sus bases/despliegues no están incluidos en este workspace; el diagrama no afirma que esas integraciones estén desplegadas o verificadas.
