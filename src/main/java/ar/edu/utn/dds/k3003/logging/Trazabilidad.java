package ar.edu.utn.dds.k3003.logging;

import java.util.UUID;

/**
 * Nombres compartidos de la trazabilidad entre módulos.
 *
 * <p>Donaciones, Donadores y Entidades y Logística ya propagan el header {@code X-Trace-Id} y lo
 * guardan en el MDC como {@code traceId}. Incentivos usa los mismos nombres para que, en Datadog,
 * una búsqueda por {@code @traceId} muestre también lo que hizo este módulo.
 */
public final class Trazabilidad {

  public static final String HEADER_TRACE = "X-Trace-Id";
  public static final String MDC_TRACE_ID = "traceId";
  public static final String MDC_REQUEST_ID = "requestId";

  private Trazabilidad() {}

  /** Genera un identificador corto; el prefijo indica dónde nació la traza (por ejemplo "cron"). */
  public static String nuevoId(String prefijo) {
    String id = UUID.randomUUID().toString().substring(0, 8);
    return prefijo == null || prefijo.isBlank() ? id : prefijo + "-" + id;
  }
}
