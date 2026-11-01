package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vuelve a publicar el estado actual completo de Incentivos: cada insignia, cada misión y el
 * progreso de cada donador con misión o insignias, con ocurridoEn = ahora.
 *
 * <p>Es la carga inicial del modelo de lectura y la forma de resincronizarlo. No tiene riesgo:
 * el Servicio de Consultas reemplaza el documento entero con cada evento.
 */
@Service
public class RepublicadorEventos {

  private static final Logger log = LoggerFactory.getLogger(RepublicadorEventos.class);

  private final InsigniaRepository insignias;
  private final MisionRepository misiones;
  private final PerfilIncentivosRepository perfiles;
  private final EventosIncentivos eventos;

  public RepublicadorEventos(
      InsigniaRepository insignias,
      MisionRepository misiones,
      PerfilIncentivosRepository perfiles,
      EventosIncentivos eventos) {
    this.insignias = insignias;
    this.misiones = misiones;
    this.perfiles = perfiles;
    this.eventos = eventos;
  }

  /** Transaccional para poder recorrer las insignias de cada perfil; se envía todo al confirmar. */
  @Transactional(readOnly = true)
  public Map<String, Integer> republicarTodo() {
    int cantInsignias = 0;
    for (Insignia insignia : insignias.findAll()) {
      eventos.insigniaCreada(insignia);
      cantInsignias++;
    }
    int cantMisiones = 0;
    for (Mision mision : misiones.findAll()) {
      eventos.misionCreada(mision);
      cantMisiones++;
    }
    int cantDonadores = 0;
    for (PerfilIncentivos perfil : perfiles.findAll()) {
      boolean tieneInsignias = perfil.getInsignias() != null && !perfil.getInsignias().isEmpty();
      if (perfil.getMisionActualID() != null || tieneInsignias) {
        eventos.progresoActualizado(perfil.getId(), null);
        cantDonadores++;
      }
    }
    log.info("Republicación de eventos: {} insignias, {} misiones, {} donadores",
        cantInsignias, cantMisiones, cantDonadores);
    Map<String, Integer> resumen = new LinkedHashMap<>();
    resumen.put("insignias", cantInsignias);
    resumen.put("misiones", cantMisiones);
    resumen.put("donadores", cantDonadores);
    return resumen;
  }
}
