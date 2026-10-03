package ar.edu.utn.dds.k3003.scheduler;

import ar.edu.utn.dds.k3003.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.logging.Trazabilidad;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron-Job del módulo Incentivos: procesa periódicamente a todos los donadores que
 * tienen una misión asignada al momento (dispara la misma lógica que
 * POST /donadores/{id}/procesar), incluyendo la detección de pérdida de progreso.
 */
@Component
public class ProcesamientoMisionesScheduler {

  private static final Logger log = LoggerFactory.getLogger(ProcesamientoMisionesScheduler.class);

  private final PerfilIncentivosRepository perfilRepository;
  private final FachadaIncentivos fachada;

  @Autowired
  public ProcesamientoMisionesScheduler(PerfilIncentivosRepository perfilRepository, FachadaIncentivos fachada) {
    this.perfilRepository = perfilRepository;
    this.fachada = fachada;
  }

  @Scheduled(fixedRateString = "${incentivos.cron.intervalo-ms:60000}")
  public void procesarDonadoresConMisionAsignada() {
    List<PerfilIncentivos> conMisionAsignada = perfilRepository.findAll().stream()
        .filter(p -> p.getMisionActualID() != null)
        .toList();

    if (conMisionAsignada.isEmpty()) {
      return;
    }

    // Disparado a mano (POST /admin/procesar-pendientes) ya trae la traza de la request; si lo
    // dispara el scheduler, cada donador recibe una propia ("cron-...") para poder seguir en
    // Datadog su procesamiento y las llamadas que generó en Donaciones y Donadores.
    String trazaDeLaRequest = MDC.get(Trazabilidad.MDC_TRACE_ID);
    log.info("Cron-Job: procesando {} donadores con misión asignada", conMisionAsignada.size());
    for (PerfilIncentivos perfil : conMisionAsignada) {
      if (trazaDeLaRequest == null) {
        MDC.put(Trazabilidad.MDC_TRACE_ID, Trazabilidad.nuevoId("cron"));
      }
      try {
        fachada.procesarDonador(perfil.getId());
      } catch (Exception e) {
        // Un donador con datos inconsistentes (ej: sin donaciones cargadas) no debe
        // frenar el procesamiento del resto del batch.
        log.warn("No se pudo procesar al donador {}: {}", perfil.getId(), e.getMessage());
      } finally {
        if (trazaDeLaRequest == null) {
          MDC.remove(Trazabilidad.MDC_TRACE_ID);
        }
      }
    }
  }
}
