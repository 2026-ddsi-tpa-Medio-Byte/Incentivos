package ar.edu.utn.dds.k3003.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Pone el {@code traceId} y el {@code requestId} de cada request en el MDC, así todos los logs que
 * se escriben mientras se atiende llevan la traza.
 *
 * <p>Si la request trae {@code X-Trace-Id} (por ejemplo desde el MCP, el bot u otro módulo) se
 * reutiliza; si no, Incentivos es el punto de entrada y genera uno. Se devuelve en la respuesta para
 * que quien llamó pueda buscarlo en Datadog.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrazabilidadFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TrazabilidadFilter.class);

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // El health-check de Render y UptimeRobot llega cada pocos segundos: no aporta a la traza.
    return request.getRequestURI().startsWith("/actuator");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String traceId = request.getHeader(Trazabilidad.HEADER_TRACE);
    if (traceId == null || traceId.isBlank()) {
      traceId = Trazabilidad.nuevoId(null);
    }
    MDC.put(Trazabilidad.MDC_TRACE_ID, traceId.trim());
    MDC.put(Trazabilidad.MDC_REQUEST_ID, Trazabilidad.nuevoId(null));
    response.setHeader(Trazabilidad.HEADER_TRACE, traceId.trim());

    long inicio = System.currentTimeMillis();
    log.info("--> {} {}", request.getMethod(), request.getRequestURI());
    try {
      chain.doFilter(request, response);
    } finally {
      log.info("<-- {} {} status={} took={}ms",
          request.getMethod(), request.getRequestURI(), response.getStatus(),
          System.currentTimeMillis() - inicio);
      MDC.remove(Trazabilidad.MDC_TRACE_ID);
      MDC.remove(Trazabilidad.MDC_REQUEST_ID);
    }
  }
}
