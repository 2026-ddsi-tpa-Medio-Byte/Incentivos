# DonaTrack Incentivos MCP

Servidor MCP local para Claude Desktop, conectado por HTTP a la API de Incentivos.

## Requisitos

- Node.js 20+
- API de Incentivos levantada (por defecto `http://localhost:8080`)

## Instalar y probar

```powershell
npm ci
npm test
npm start
```

Configurar otra URL con `INCENTIVOS_API_URL`. El servidor MCP usa `stdio`; no agregar `console.log`/salida normal al código del servidor porque rompería el protocolo.

La configuración para Claude Desktop y el alcance de herramientas está en `../docs/entrega5-incentivos.md`.
