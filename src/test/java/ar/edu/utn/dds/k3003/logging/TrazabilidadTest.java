package ar.edu.utn.dds.k3003.logging;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import ar.edu.utn.dds.k3003.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
import ar.edu.utn.dds.k3003.scheduler.ProcesamientoMisionesScheduler;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/** Trazabilidad de Incentivos: el traceId entra, se propaga a otros módulos y llega a Datadog. */
class TrazabilidadTest {

  @AfterEach
  void limpiarMdc() {
    MDC.clear();
  }

  @Test
  void elFiltroReutilizaElTraceIdQueLlegaYLoDevuelve() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/donadores/1/procesar");
    request.addHeader(Trazabilidad.HEADER_TRACE, "mcp-abc123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    List<String> trazaDuranteLaRequest = new ArrayList<>();

    new TrazabilidadFilter().doFilter(request, response,
        (req, res) -> trazaDuranteLaRequest.add(MDC.get(Trazabilidad.MDC_TRACE_ID)));

    Assertions.assertEquals(List.of("mcp-abc123"), trazaDuranteLaRequest);
    Assertions.assertEquals("mcp-abc123", response.getHeader(Trazabilidad.HEADER_TRACE));
    Assertions.assertNull(MDC.get(Trazabilidad.MDC_TRACE_ID), "El MDC tiene que quedar limpio");
  }

  @Test
  void elFiltroGeneraUnTraceIdSiIncentivosEsElPuntoDeEntrada() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/misiones");
    MockHttpServletResponse response = new MockHttpServletResponse();
    List<String> trazaDuranteLaRequest = new ArrayList<>();

    new TrazabilidadFilter().doFilter(request, response,
        (req, res) -> trazaDuranteLaRequest.add(MDC.get(Trazabilidad.MDC_TRACE_ID)));

    String generado = trazaDuranteLaRequest.get(0);
    Assertions.assertNotNull(generado);
    Assertions.assertEquals(generado, response.getHeader(Trazabilidad.HEADER_TRACE));
  }

  @Test
  void elInterceptorPropagaElTraceIdAlLlamarAOtroModulo() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new TrazabilidadInterceptor());
    MockRestServiceServer donaciones = MockRestServiceServer.bindTo(restTemplate).build();
    donaciones.expect(requestTo("http://donaciones/donaciones?donadorID=1"))
        .andExpect(header(Trazabilidad.HEADER_TRACE, "traza-1"))
        .andRespond(withSuccess("[]", org.springframework.http.MediaType.APPLICATION_JSON));

    MDC.put(Trazabilidad.MDC_TRACE_ID, "traza-1");
    restTemplate.getForObject("http://donaciones/donaciones?donadorID=1", String.class);

    donaciones.verify();
  }

  @Test
  void elCronJobLeDaUnaTrazaPropiaACadaDonador() {
    PerfilIncentivosRepository repositorio = mock(PerfilIncentivosRepository.class);
    FachadaIncentivos fachada = mock(FachadaIncentivos.class);
    PerfilIncentivos uno = new PerfilIncentivos("1");
    uno.setMisionActualID("m1");
    PerfilIncentivos dos = new PerfilIncentivos("2");
    dos.setMisionActualID("m1");
    when(repositorio.findAll()).thenReturn(List.of(uno, dos));
    List<String> trazas = new ArrayList<>();
    doAnswer(inv -> trazas.add(MDC.get(Trazabilidad.MDC_TRACE_ID)))
        .when(fachada).procesarDonador(anyString());

    new ProcesamientoMisionesScheduler(repositorio, fachada).procesarDonadoresConMisionAsignada();

    Assertions.assertEquals(2, trazas.size());
    Assertions.assertTrue(trazas.stream().allMatch(t -> t != null && t.startsWith("cron-")));
    Assertions.assertNotEquals(trazas.get(0), trazas.get(1), "Cada donador tiene su propia traza");
    Assertions.assertNull(MDC.get(Trazabilidad.MDC_TRACE_ID));
  }

  @Test
  void elAppenderMandaElTraceIdYElErrorADatadog() {
    LoggerContext contexto = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
    MDC.put(Trazabilidad.MDC_TRACE_ID, "traza-9");
    LoggingEvent evento = new LoggingEvent(
        "test", contexto.getLogger("ar.edu.utn.dds.k3003.Fachada"), Level.ERROR,
        "No se pudo procesar al donador {}", new IllegalStateException("Donaciones caído"),
        new Object[] {"7"});

    Map<String, Object> cuerpo = DatadogHttpAppender.armarEvento(evento, "prod", "incentivos-wtbd", "incentivos");

    Assertions.assertEquals("incentivos", cuerpo.get("service"));
    Assertions.assertEquals("traza-9", cuerpo.get("traceId"));
    Assertions.assertEquals("No se pudo procesar al donador 7", cuerpo.get("message"));
    @SuppressWarnings("unchecked")
    Map<String, Object> error = (Map<String, Object>) cuerpo.get("error");
    Assertions.assertEquals("java.lang.IllegalStateException", error.get("kind"));
    Assertions.assertTrue(((String) error.get("stack")).contains("Donaciones caído"));
  }
}
