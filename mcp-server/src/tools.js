import { z } from "zod";
import { createIncentivosApi } from "./api.js";

const idSchema = z.string().trim().min(1).max(128);
const categorySchema = z.enum([
  "COLABORADOR",
  "OCASIONAL",
  "REVOLUCIONARIO",
  "SALVADOR",
  "TRANSFORMADOR",
]);
const missionTypeSchema = z.enum([
  "COMPLETITUD",
  "DONACIONES_ASCENDENTES",
  "DONACIONES_EXITOSAS",
  "REVOLUCION_DONADORA",
]);

function textResult(value) {
  return { content: [{ type: "text", text: JSON.stringify(value, null, 2) }] };
}

function register(server, request, name, description, inputSchema, handler, annotations = {}) {
  server.registerTool(name, { description, inputSchema, annotations }, async (input) => {
    try {
      return textResult(await handler(input));
    } catch (error) {
      return {
        isError: true,
        content: [{ type: "text", text: error instanceof Error ? error.message : String(error) }],
      };
    }
  });
}

export function registerIncentivosTools(server, request = createIncentivosApi()) {
  register(server, request, "listar_insignias", "Lista las insignias configuradas en Incentivos.", {},
    () => request("/insignias"), { readOnlyHint: true, destructiveHint: false });

  register(server, request, "obtener_insignia", "Consulta una insignia por ID.", { id: idSchema },
    ({ id }) => request(`/insignias/${encodeURIComponent(id)}`), { readOnlyHint: true, destructiveHint: false });

  register(server, request, "crear_insignia", "Crea una insignia. La operación escribe en Incentivos.", {
    id: idSchema,
    nombre: z.string().trim().min(1).max(120),
    descripcion: z.string().trim().min(1).max(500),
  }, ({ id, nombre, descripcion }) => request("/insignias", {
    method: "POST",
    body: { id, nombre, descripcion },
  }), { readOnlyHint: false, destructiveHint: false, idempotentHint: false, openWorldHint: true });

  register(server, request, "actualizar_insignia", "Actualiza nombre o descripción de una insignia existente. Modifica datos.", {
    id: idSchema,
    nombre: z.string().trim().min(1).max(120).optional(),
    descripcion: z.string().trim().min(1).max(500).optional(),
  }, ({ id, nombre, descripcion }) => request(`/api/insignias/${encodeURIComponent(id)}`, {
    method: "PUT",
    body: { nombre, descripcion },
  }), { readOnlyHint: false, destructiveHint: false, idempotentHint: true });

  register(server, request, "eliminar_insignia", "Elimina una insignia existente. Confirmá que no se necesita antes de ejecutarlo.", { id: idSchema },
    ({ id }) => request(`/api/insignias/${encodeURIComponent(id)}`, { method: "DELETE" }),
    { readOnlyHint: false, destructiveHint: true, idempotentHint: true });

  register(server, request, "listar_misiones", "Lista las misiones de Incentivos.", {},
    () => request("/misiones"), { readOnlyHint: true, destructiveHint: false });

  register(server, request, "obtener_mision", "Consulta una misión por ID.", { id: idSchema },
    ({ id }) => request(`/misiones/${encodeURIComponent(id)}`), { readOnlyHint: true, destructiveHint: false });

  register(server, request, "crear_mision", "Crea una misión con la insignia y categorías indicadas. La operación escribe en Incentivos.", {
    id: idSchema,
    nombre: z.string().trim().min(1).max(120),
    insigniaID: idSchema,
    categoriaInicio: categorySchema,
    categoriaFin: categorySchema,
    tipo: missionTypeSchema,
  }, ({ id, nombre, insigniaID, categoriaInicio, categoriaFin, tipo }) => request("/misiones", {
    method: "POST",
    body: { id, nombre, insigniaID, categoriaInicio, categoriaFin, tipo },
  }), { readOnlyHint: false, destructiveHint: false, idempotentHint: false, openWorldHint: true });

  register(server, request, "actualizar_mision", "Actualiza campos de una misión existente. Modifica las reglas de Incentivos.", {
    id: idSchema,
    nombre: z.string().trim().min(1).max(120).optional(),
    insigniaID: idSchema.optional(),
    categoriaInicio: categorySchema.optional(),
    categoriaFin: categorySchema.optional(),
    tipo: missionTypeSchema.optional(),
  }, ({ id, nombre, insigniaID, categoriaInicio, categoriaFin, tipo }) => request(`/api/misiones/${encodeURIComponent(id)}`, {
    method: "PUT",
    body: { nombre, insigniaID, categoriaInicio, categoriaFin, tipo },
  }), { readOnlyHint: false, destructiveHint: false, idempotentHint: true });

  register(server, request, "eliminar_mision", "Elimina una misión existente. Verificá antes si hay donadores que la tienen asignada.", { id: idSchema },
    ({ id }) => request(`/api/misiones/${encodeURIComponent(id)}`, { method: "DELETE" }),
    { readOnlyHint: false, destructiveHint: true, idempotentHint: true });

  register(server, request, "consultar_estado_incentivos_donador", "Consulta insignias y misión actual del donador. La misión puede no estar asignada.", {
    donadorID: idSchema,
  }, async ({ donadorID }) => {
    const [insignias, misionActual] = await Promise.all([
      request(`/donadores/${encodeURIComponent(donadorID)}/insignias`),
      request(`/donadores/${encodeURIComponent(donadorID)}/mision-actual`, { allowNotFound: true }),
    ]);
    return { donadorID, insignias, misionActual };
  }, { readOnlyHint: true, destructiveHint: false });

  register(server, request, "asignar_mision_a_donador", "Asigna al donador una misión ya existente. Ejecuta las validaciones y reglas del servicio Incentivos.", {
    donadorID: idSchema,
    misionID: idSchema,
  }, async ({ donadorID, misionID }) => {
    const mision = await request(`/misiones/${encodeURIComponent(misionID)}`);
    await request(`/donadores/${encodeURIComponent(donadorID)}/mision-actual`, {
      method: "POST",
      body: mision,
    });
    return { mensaje: "Misión asignada", donadorID, misionID };
  }, { readOnlyHint: false, destructiveHint: false, idempotentHint: true, openWorldHint: true });

  register(server, request, "otorgar_insignia_a_donador", "Otorga al donador una insignia existente. Es una operación que modifica datos.", {
    donadorID: idSchema,
    insigniaID: idSchema,
  }, async ({ donadorID, insigniaID }) => {
    const insignia = await request(`/insignias/${encodeURIComponent(insigniaID)}`);
    await request(`/donadores/${encodeURIComponent(donadorID)}/insignias`, {
      method: "POST",
      body: insignia,
    });
    return { mensaje: "Insignia otorgada", donadorID, insigniaID };
  }, { readOnlyHint: false, destructiveHint: false, idempotentHint: true, openWorldHint: true });

  register(server, request, "procesar_donador", "Evalúa la misión del donador y aplica las reglas de Incentivos. Puede otorgar o retirar insignias y actualizar categorías en servicios relacionados.", {
    donadorID: idSchema,
  }, async ({ donadorID }) => {
    await request(`/donadores/${encodeURIComponent(donadorID)}/procesar`, { method: "POST" });
    return { mensaje: "Donador procesado", donadorID };
  }, { readOnlyHint: false, destructiveHint: true, idempotentHint: false, openWorldHint: true });

  register(server, request, "listar_perfiles_incentivos", "Consulta los perfiles locales de Incentivos, incluyendo misión actual e insignias.", {},
    () => request("/admin/perfiles"), { readOnlyHint: true, destructiveHint: false });

  register(server, request, "procesar_donadores_pendientes", "Ejecuta el batch administrativo de Incentivos para todos los donadores con misión asignada.", {},
    () => request("/admin/procesar-pendientes", { method: "POST" }),
    { readOnlyHint: false, destructiveHint: true, idempotentHint: false, openWorldHint: true });
}
