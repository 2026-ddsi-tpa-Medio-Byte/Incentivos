package ar.edu.utn.dds.k3003.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

  @Override
  public void start() {
    apiKey = System.getenv("DD_API_KEY");
    site = System.getenv().getOrDefault("DD_SITE", "datadoghq.com");
    env = System.getenv().getOrDefault("DD_ENV", "local");
    service = System.getenv().getOrDefault("DD_SERVICE", "incentivos");
    hostname = System.getenv().getOrDefault("RENDER_SERVICE_NAME", "incentivos-local");

    if (apiKey == null || apiKey.isBlank()) {
      addInfo("DD_API_KEY no configurada: DatadogHttpAppender queda deshabilitado.");
      return;
    }

    executor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread thread = new Thread(r, "datadog-log-appender");
              thread.setDaemon(true);
              return thread;
            });
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    if (executor == null) {
      return;
    }
    executor.execute(() -> send(event));
  }

  private void send(ILoggingEvent event) {
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("ddsource", "java");
      body.put("ddtags", "env:" + env);
      body.put("hostname", hostname);
      body.put("service", service);
      body.put("status", event.getLevel().toString());
      body.put("logger", event.getLoggerName());
      body.put("message", event.getFormattedMessage());

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("https://http-intake.logs." + site + "/api/v2/logs"))
              .header("Content-Type", "application/json")
              .header("DD-API-KEY", apiKey)
              .timeout(Duration.ofSeconds(5))
              .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
              .build();

      HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    } catch (Exception e) {
      addError("Error enviando log a Datadog", e);
    }
  }

  @Override
  public void stop() {
    if (executor != null) {
      executor.shutdown();
    }
    super.stop();
  }
}
