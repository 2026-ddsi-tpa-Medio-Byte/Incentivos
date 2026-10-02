import test from "node:test";
import assert from "node:assert/strict";
import { fileURLToPath } from "node:url";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

test("el ejecutable MCP inicia por stdio y Claude puede enumerar sus tools", async () => {
  const serverScript = fileURLToPath(new URL("../src/index.js", import.meta.url));
  const env = Object.fromEntries(
    Object.entries(process.env).filter(([, value]) => typeof value === "string"),
  );
  const transport = new StdioClientTransport({
    command: process.execPath,
    args: [serverScript],
    env,
  });
  const client = new Client({ name: "stdio-smoke-test", version: "1.0.0" });

  await client.connect(transport);
  try {
    const { tools } = await client.listTools();
    assert.equal(tools.length, 16);
    assert.ok(tools.some((tool) => tool.name === "actualizar_mision"));
    assert.ok(tools.some((tool) => tool.name === "procesar_donador"));
  } finally {
    await client.close();
  }
});
