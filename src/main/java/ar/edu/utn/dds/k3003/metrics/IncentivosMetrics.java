package ar.edu.utn.dds.k3003.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.stereotype.Component;

/** Métricas de dominio del componente Incentivos, exportadas por Micrometer. */
@Component
public class IncentivosMetrics {

  private final MeterRegistry registry;

  public IncentivosMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  public void recordDonadorProcesado(String resultado, Duration duracion) {
    registry.counter("incentivos.donador.procesamientos", "resultado", resultado).increment();
    registry
        .timer("incentivos.donador.procesamiento.duracion", "resultado", resultado)
        .record(duracion);
  }

  public void recordMisionCompletada(String tipoMision) {
    registry.counter("incentivos.misiones.completadas", "tipo", tipoMision).increment();
  }

  public void recordPerdidaDeProgreso() {
    registry.counter("incentivos.progreso.perdido").increment();
  }
}
