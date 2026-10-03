package ar.edu.utn.dds.k3003.logging;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Reenvía el {@code traceId} actual en cada llamada a otro módulo y deja en el log cada llamada
 * saliente, con su resultado y duración.
 *
 * <p>Sin el header, Donaciones y Donadores generarían una traza nueva y la operación quedaría
 * partida en Datadog. Sin el log, cuando Donaciones fallaba solo se veía el efecto ("no se pudo
 * procesar al donador") y no la llamada que había fallado.
 */
public class TrazabilidadInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger log = LoggerFactory.getLogger(TrazabilidadInterceptor.class);

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    String traceId = MDC.get(Trazabilidad.MDC_TRACE_ID);
    if (traceId != null && !traceId.isBlank()) {
      request.getHeaders().set(Trazabilidad.HEADER_TRACE, traceId);
    }

    String destino = request.getURI().getHost() + request.getURI().getPath();
    long inicio = System.currentTimeMillis();
    try {
      ClientHttpResponse response = execution.execute(request, body);
      int status = response.getStatusCode().value();
      long ms = System.currentTimeMillis() - inicio;
      if (status >= 400) {
        log.warn("[Incentivos -> {}] {} status={} took={}ms", destino, request.getMethod(), status, ms);
      } else {
        log.info("[Incentivos -> {}] {} status={} took={}ms", destino, request.getMethod(), status, ms);
      }
      return response;
    } catch (IOException e) {
      log.warn("[Incentivos -> {}] {} sin respuesta tras {}ms: {}",
          destino, request.getMethod(), System.currentTimeMillis() - inicio, e.getMessage());
      throw e;
    }
  }
}
