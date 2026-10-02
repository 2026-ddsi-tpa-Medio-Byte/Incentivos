import test from "node:test";
import assert from "node:assert/strict";
import { createIncentivosApi, IncentivosApiError } from "../src/api.js";

test("API client envía JSON y retorna una respuesta JSON", async () => {
  let captured;
  const request = createIncentivosApi({
    baseUrl: "http://example.test/",
    fetchImpl: async (url, options) => {
      captured = { url, options };
      return new Response('{"id":"i-1"}', { status: 200 });
    },
  });
  const result = await request("/insignias", { method: "POST", body: { id: "i-1" } });
  assert.equal(captured.url, "http://example.test/insignias");
  assert.equal(captured.options.headers["Content-Type"], "application/json");
  assert.deepEqual(JSON.parse(captured.options.body), { id: "i-1" });
  assert.deepEqual(result, { id: "i-1" });
});

test("API client devuelve null cuando se permite 404", async () => {
  const request = createIncentivosApi({
    fetchImpl: async () => new Response("", { status: 404 }),
  });
  assert.equal(await request("/missing", { allowNotFound: true }), null);
});

test("API client conserva el status y acota el texto de error", async () => {
  const request = createIncentivosApi({
    fetchImpl: async () => new Response("No encontrado", { status: 404 }),
  });
  await assert.rejects(request("/missing"), (error) => {
    assert.ok(error instanceof IncentivosApiError);
    assert.equal(error.status, 404);
    assert.match(error.message, /HTTP 404/);
    return true;
  });
});
