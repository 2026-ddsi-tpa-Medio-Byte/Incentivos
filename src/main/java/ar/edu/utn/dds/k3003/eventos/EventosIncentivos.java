package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
import ar.edu.utn.dds.k3003.services.MisionEvaluatorService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogo de los eventos que publica Incentivos para el modelo de lectura, y el armado de su
 * {@code datos}: siempre el estado completo y actual del objeto, no lo que cambió.
 *
 * <p>Incentivos no publica la categoría del donador aunque la cambie: el dueño de ese dato es
 * Donadores y Entidades, que publica su propio evento.
 */
@Component
public class EventosIncentivos {

  public static final String INSIGNIA_CREADA = "INSIGNIA_CREADA";
  public static final String INSIGNIA_MODIFICADA = "INSIGNIA_MODIFICADA";
  public static final String INSIGNIA_ELIMINADA = "INSIGNIA_ELIMINADA";
  public static final String MISION_CREADA = "MISION_CREADA";
  public static final String MISION_MODIFICADA = "MISION_MODIFICADA";
  public static final String MISION_ELIMINADA = "MISION_ELIMINADA";
  public static final String PROGRESO_DONADOR_ACTUALIZADO = "PROGRESO_DONADOR_ACTUALIZADO";
  public static final String MODULO_RESETEADO = "MODULO_RESETEADO";

  private static final Logger log = LoggerFactory.getLogger(EventosIncentivos.class);

  private final PublicadorEventos publicador;
  private final PerfilIncentivosRepository perfiles;
  private final MisionRepository misiones;
  private final FachadaDonaciones fachadaDonaciones;

  public EventosIncentivos(
      PublicadorEventos publicador,
      PerfilIncentivosRepository perfiles,
      MisionRepository misiones,
      FachadaDonaciones fachadaDonaciones) {
    this.publicador = publicador;
    this.perfiles = perfiles;
    this.misiones = misiones;
    this.fachadaDonaciones = fachadaDonaciones;
  }

  // ── Insignias y misiones ─────────────────────────────────────────────────────

  public void insigniaCreada(Insignia insignia) {
    publicador.publicar(INSIGNIA_CREADA, insignia.getId(), datosInsignia(insignia));
  }

  public void insigniaModificada(Insignia insignia) {
    publicador.publicar(INSIGNIA_MODIFICADA, insignia.getId(), datosInsignia(insignia));
  }

  public void insigniaEliminada(String id) {
    publicador.publicar(INSIGNIA_ELIMINADA, id, Map.of("id", id));
  }

  public void misionCreada(Mision mision) {
    publicador.publicar(MISION_CREADA, mision.getId(), datosMision(mision));
  }

  public void misionModificada(Mision mision) {
    publicador.publicar(MISION_MODIFICADA, mision.getId(), datosMision(mision));
  }

  public void misionEliminada(String id) {
    publicador.publicar(MISION_ELIMINADA, id, Map.of("id", id));
  }

  public void moduloReseteado() {
    publicador.publicar(MODULO_RESETEADO, "*", Map.of());
  }

  public static Map<String, Object> datosInsignia(Insignia insignia) {
    Map<String, Object> datos = new LinkedHashMap<>();
    datos.put("id", insignia.getId());
    datos.put("nombre", insignia.getNombre());
    datos.put("descripcion", insignia.getDescripcion());
    return datos;
  }

  public static Map<String, Object> datosMision(Mision mision) {
    Map<String, Object> datos = new LinkedHashMap<>();
    datos.put("id", mision.getId());
    datos.put("nombre", mision.getNombre());
    datos.put("insigniaID", mision.getInsigniaID());
    datos.put("categoriaInicio", mision.getCategoriaInicio());
    datos.put("categoriaFin", mision.getCategoriaFin());
    datos.put("tipo", mision.getTipo());
    return datos;
  }

  // ── Progreso del donador ─────────────────────────────────────────────────────

  /**
   * Publica el estado actual del donador en Incentivos: todas sus insignias y la misión en curso
   * con su progreso.
   *
   * <p>Corre dentro de la transacción de quien lo llama (así ve los cambios todavía sin confirmar,
   * y el envío espera al commit) o en una propia de solo lectura. Nunca lanza: si no se puede armar
   * el evento queda un WARN y la operación sigue.
   *
   * @param donacionesConocidas las donaciones que ya trajo el procesamiento, para no volver a
   *     pedirlas a Donaciones; {@code null} para consultarlas
   */
  @Transactional(readOnly = true)
  public void progresoActualizado(String donadorId, List<DonacionDTO> donacionesConocidas) {
    try {
      publicador.publicar(PROGRESO_DONADOR_ACTUALIZADO, donadorId,
          datosProgreso(donadorId, donacionesConocidas));
    } catch (RuntimeException e) {
      log.warn("No se pudo armar el evento de progreso del donador {}: {}", donadorId, e.getMessage());
    }
  }

  Map<String, Object> datosProgreso(String donadorId, List<DonacionDTO> donacionesConocidas) {
    PerfilIncentivos perfil = perfiles.findById(donadorId).orElse(null);

    List<Map<String, Object>> insignias = perfil == null || perfil.getInsignias() == null
        ? List.of()
        : perfil.getInsignias().stream()
            .map(i -> {
              Map<String, Object> insignia = new LinkedHashMap<>();
              insignia.put("id", i.getId());
              insignia.put("nombre", i.getNombre());
              return insignia;
            })
            .toList();

    Mision mision = perfil == null || perfil.getMisionActualID() == null
        ? null
        : misiones.findById(perfil.getMisionActualID()).orElse(null);

    Map<String, Object> datos = new LinkedHashMap<>();
    datos.put("donadorId", donadorId);
    datos.put("insignias", insignias);
    datos.put("misionEnCurso", mision == null ? null : misionEnCurso(donadorId, mision, donacionesConocidas));
    return datos;
  }

  private Map<String, Object> misionEnCurso(
      String donadorId, Mision mision, List<DonacionDTO> donacionesConocidas) {
    Map<String, Object> enCurso = new LinkedHashMap<>();
    enCurso.put("id", mision.getId());
    enCurso.put("nombre", mision.getNombre());
    enCurso.put("tipo", mision.getTipo());
    enCurso.put("progreso", progreso(donadorId, mision, donacionesConocidas));
    enCurso.put("objetivo", MisionEvaluatorService.objetivo(mision.getTipo()));
    return enCurso;
  }

  /**
   * Progreso según la regla de la misión. Si Donaciones no responde se publica {@code null}
   * ("desconocido") en vez de un 0 que el modelo de lectura mostraría como dato real; el próximo
   * procesamiento del Cron-Job lo corrige.
   */
  private Long progreso(String donadorId, Mision mision, List<DonacionDTO> donacionesConocidas) {
    List<DonacionDTO> donaciones = donacionesConocidas;
    if (donaciones == null) {
      try {
        donaciones = fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorId, null);
      } catch (RuntimeException e) {
        log.warn("Progreso del donador {} desconocido: Donaciones no respondió ({})",
            donadorId, e.getMessage());
        return null;
      }
    }
    return MisionEvaluatorService.progreso(mision.getTipo(), donaciones);
  }
}
