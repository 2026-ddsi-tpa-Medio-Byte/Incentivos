import test from "node:test";
import assert from "node:assert/strict";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { InMemoryTransport } from "@modelcontextprotocol/sdk/inMemory.js";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { registerIncentivosTools } from "../src/tools.js";

test("el servidor completa el handshake MCP y publica herramientas", async () => {
  const server = new McpServer({ name: "test-incentivos", version: "1.0.0" });
  registerIncentivosTools(server, async () => []);
  const [clientTransport, serverTransport] = InMemoryTransport.createLinkedPair();
  const client = new Client({ name: "test-client", version: "1.0.0" });

  await server.connect(serverTransport);
  await client.connect(clientTransport);
  try {
    const result = await client.listTools();
    const names = result.tools.map((tool) => tool.name);
    assert.ok(names.includes("listar_misiones"));
    assert.ok(names.includes("procesar_donador"));
    assert.equal(names.length, 16);
  } finally {
    await client.close();
    await server.close();
  }
});
