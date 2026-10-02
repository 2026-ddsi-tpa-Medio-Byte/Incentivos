import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { registerIncentivosTools } from "./tools.js";

const server = new McpServer({
  name: "donatrack-incentivos",
  version: "1.0.0",
});

registerIncentivosTools(server);
await server.connect(new StdioServerTransport());
