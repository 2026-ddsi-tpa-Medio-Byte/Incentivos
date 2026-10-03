package ar.edu.utn.dds.k3003.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Appender de Logback que envía cada evento de log a la API de Logs de Datadog
 * (https://docs.datadoghq.com/api/latest/logs/) sin depender del Datadog Agent.
 *
 * Se activa solo si la variable de entorno DD_API_KEY está presente.
 */
public class DatadogHttpAppender extends AppenderBase<ILoggingEvent> {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  private String apiKey;
  private String site;
  private String env;
  private String service;
  private String hostname;
  private ExecutorService executor;
  private final AtomicBoolean failureReported = new AtomicBoolean();
  private final AtomicBoolean acceptanceReported = new AtomicBoolean();
  private final AtomicBoolean firstEventReported = new AtomicBoolean();

  @Override
  public void start() {
    apiKey = System.getenv("DD_API_KEY");
    if (apiKey != null) {
      apiKey = apiKey.trim();
      if (apiKey.chars().anyMatch(ch -> ch < 0x20 || ch > 0x7e)) {
        addWarn(
            "DD_API_KEY contiene caracteres no válidos para un encabezado HTTP; "
                + "volvé a ingresarla sin espacios ni saltos de línea.");
        apiKey = null;
      }
    }
    site = System.getenv().getOrDefault("DD_SITE", "datadoghq.com");
    env = System.getenv().getOrDefault("DD_ENV", "local");
    service = System.getenv().getOrDefault("DD_SERVICE", "incentivos");
    hostname = System.getenv().getOrDefault("RENDER_SERVICE_NAME", "incentivos-local");

    if (apiKey == null || apiKey.isBlank()) {
      addInfo("DD_API_KEY no configurada: DatadogHttpAppender queda deshabilitado.");
      System.err.println("[DatadogHttpAppender] Deshabilitado: DD_API_KEY no está configurada.");
      super.start();
      return;
    }

    executor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread thread = new Thread(r, "datadog-log-appender");
              thread.setDaemon(true);
              return thread;
            });
    System.err.println(
        "[DatadogHttpAppender] Activo; enviando a "
            + site
            + " con service:"
            + service
            + " env:"
            + env);
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    if (executor == null) {
      return;
    }
    event.prepareForDeferredProcessing();
    try {
      executor.execute(() -> send(event));
      if (firstEventReported.compareAndSet(false, true)) {
        System.err.println("[DatadogHttpAppender] Primer evento de log encolado.");
      }
    } catch (java.util.concurrent.RejectedExecutionException e) {
      reportFailure("No se pudo encolar el log para Datadog", e);
    }
  }

  /**
   * Arma el evento en el formato de la API de logs de Datadog.
   *
   * <p>Además de los datos del servicio, agrega lo que haya en el MDC ({@code traceId},
   * {@code requestId}) como atributos de primer nivel, para buscar con {@code @traceId:...} igual
   * que en los otros módulos, y la excepción en los atributos estándar {@code error.*} de Datadog:
   * antes solo viajaba el mensaje y el stack trace se perdía.
   */
  static Map<String, Object> armarEvento(
      ILoggingEvent event, String env, String hostname, String service) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ddsource", "java");
    body.put("ddtags", "env:" + env);
    body.put("hostname", hostname);
    body.put("service", service);
    body.put("status", event.getLevel().toString());
    body.put("logger", event.getLoggerName());
    body.put("message", event.getFormattedMessage());
    Map<String, String> mdc = event.getMDCPropertyMap();
    if (mdc != null) {
      mdc.forEach((clave, valor) -> {
        if (valor != null && !valor.isBlank()) {
          body.put(clave, valor);
        }
      });
    }
    IThrowableProxy error = event.getThrowableProxy();
    if (error != null) {
      Map<String, Object> detalle = new LinkedHashMap<>();
      detalle.put("kind", error.getClassName());
      detalle.put("message", error.getMessage());
      detalle.put("stack", ThrowableProxyUtil.asString(error));
      body.put("error", detalle);
    }
    return body;
  }

  private void send(ILoggingEvent event) {
    try {
      Map<String, Object> body = armarEvento(event, env, hostname, service);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("https://http-intake.logs." + site + "/api/v2/logs"))
              .header("Content-Type", "application/json")
              .header("DD-API-KEY", apiKey)
              .timeout(Duration.ofSeconds(5))
              .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(List.of(body))))
              .build();

      HTTP_CLIENT
          .sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .whenComplete(
              (response, error) -> {
                if (error != null) {
                  reportFailure("Falló la conexión con Datadog", error);
                } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
                  reportFailure(
                      "Datadog rechazó el log (HTTP "
                          + response.statusCode()
                          + "): "
                          + summarizeResponse(response.body()),
                      null);
                } else if (acceptanceReported.compareAndSet(false, true)) {
                  System.err.println(
                      "[DatadogHttpAppender] Datadog aceptó logs (HTTP "
                          + response.statusCode()
                          + ") para service:"
                          + service
                          + " env:"
                          + env);
                }
              });
    } catch (Exception e) {
      reportFailure("No se pudo preparar el log para Datadog", e);
    }
  }

  private void reportFailure(String message, Throwable error) {
    if (!failureReported.compareAndSet(false, true)) {
      return;
    }
    String detail =
        error == null ? message : message + " (" + error.getClass().getSimpleName() + ")";
    System.err.println("[DatadogHttpAppender] " + detail);
  }

  private String summarizeResponse(String body) {
    if (body == null || body.isBlank()) {
      return "respuesta vacía";
    }
    String singleLine = body.replaceAll("[\\r\\n\\t]+", " ").trim();
    return singleLine.substring(0, Math.min(singleLine.length(), 300));
  }

  @Override
  public void stop() {
    if (executor != null) {
      executor.shutdown();
    }
    super.stop();
  }
}
