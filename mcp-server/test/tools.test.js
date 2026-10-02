import test from "node:test";
import assert from "node:assert/strict";
import { registerIncentivosTools } from "../src/tools.js";

function createFakeServer() {
  const tools = new Map();
  return {
    tools,
    registerTool(name, _config, handler) {
      tools.set(name, handler);
    },
  };
}

test("registra herramientas de consulta y operación para Incentivos", () => {
  const server = createFakeServer();
  registerIncentivosTools(server, async () => ({}));
  assert.deepEqual([...server.tools.keys()], [
    "listar_insignias",
    "obtener_insignia",
    "crear_insignia",
    "actualizar_insignia",
    "eliminar_insignia",
    "listar_misiones",
    "obtener_mision",
    "crear_mision",
    "actualizar_mision",
    "eliminar_mision",
    "consultar_estado_incentivos_donador",
    "asignar_mision_a_donador",
    "otorgar_insignia_a_donador",
    "procesar_donador",
    "listar_perfiles_incentivos",
    "procesar_donadores_pendientes",
  ]);
});

test("crear_mision envía el DTO esperado a la API", async () => {
  const server = createFakeServer();
  const calls = [];
  registerIncentivosTools(server, async (path, options = {}) => {
    calls.push({ path, options });
    return { id: "m-1" };
  });
  const result = await server.tools.get("crear_mision")({
    id: "m-1",
    nombre: "Primera misión",
    insigniaID: "i-1",
    categoriaInicio: "COLABORADOR",
    categoriaFin: "OCASIONAL",
    tipo: "COMPLETITUD",
  });
  assert.equal(calls[0].path, "/misiones");
  assert.equal(calls[0].options.method, "POST");
  assert.equal(calls[0].options.body.id, "m-1");
  assert.equal(result.isError, undefined);
});

test("eliminar_insignia llama al endpoint CRUD y devuelve el resultado", async () => {
  const server = createFakeServer();
  const calls = [];
  registerIncentivosTools(server, async (path, options = {}) => {
    calls.push({ path, options });
    return null;
  });
  await server.tools.get("eliminar_insignia")({ id: "insignia-1" });
  assert.equal(calls[0].path, "/api/insignias/insignia-1");
  assert.equal(calls[0].options.method, "DELETE");
});

test("asignar_mision consulta la misión antes de invocar la operación de dominio", async () => {
  const server = createFakeServer();
  const calls = [];
  registerIncentivosTools(server, async (path, options = {}) => {
    calls.push({ path, options });
    if (path === "/misiones/m-1") return { id: "m-1", nombre: "Misión" };
    return null;
  });
  await server.tools.get("asignar_mision_a_donador")({ donadorID: "d-1", misionID: "m-1" });
  assert.deepEqual(calls.map(({ path, options }) => [path, options.method ?? "GET"]), [
    ["/misiones/m-1", "GET"],
    ["/donadores/d-1/mision-actual", "POST"],
  ]);
});

test("la herramienta devuelve errores de API como resultado MCP marcado como error", async () => {
  const server = createFakeServer();
  registerIncentivosTools(server, async () => {
    throw new Error("API HTTP 503");
  });
  const result = await server.tools.get("listar_misiones")({});
  assert.equal(result.isError, true);
  assert.match(result.content[0].text, /503/);
});
