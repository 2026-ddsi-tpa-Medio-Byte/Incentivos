package ar.edu.utn.dds.k3003.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class IncentivosMetricsTest {

  @Test
  void registraResultadosDeProcesamientoConTagsDeBajaCardinalidad() {
    var registry = new SimpleMeterRegistry();
    var metrics = new IncentivosMetrics(registry);

    metrics.recordDonadorProcesado("error", Duration.ofMillis(25));

    assertEquals(
        1.0,
        registry.counter("incentivos.donador.procesamientos", "resultado", "error").count());
    assertEquals(
        25.0,
        registry
            .timer("incentivos.donador.procesamiento.duracion", "resultado", "error")
            .totalTime(java.util.concurrent.TimeUnit.MILLISECONDS));
  }

  @Test
  void registraMisionesCompletadasYPerdidaDeProgreso() {
    var registry = new SimpleMeterRegistry();
    var metrics = new IncentivosMetrics(registry);

    metrics.recordMisionCompletada("COMPLETITUD");
    metrics.recordPerdidaDeProgreso();

    assertEquals(1.0, registry.counter("incentivos.misiones.completadas", "tipo", "COMPLETITUD").count());
    assertEquals(1.0, registry.counter("incentivos.progreso.perdido").count());
  }
}
