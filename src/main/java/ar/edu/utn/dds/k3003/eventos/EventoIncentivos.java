package ar.edu.utn.dds.k3003.eventos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Sobre de los eventos que consume el Servicio de Consultas. Es el mismo para los cuatro módulos:
 * si un campo falta o cambia de nombre, Consultas no puede procesar el evento.
 *
 * @param datos el estado completo y actual del objeto después del cambio, no lo que cambió
 */
@JsonPropertyOrder({"eventoId", "tipo", "modulo", "entidadId", "ocurridoEn", "datos"})
public record EventoIncentivos(
    String eventoId, String tipo, String modulo, String entidadId, String ocurridoEn, Object datos) {

  public static final String MODULO = "incentivos";

  /**
   * UTC con milisegundos y Z siempre. {@code Instant.toString()} no sirve tal cual: omite los
   * milisegundos cuando valen cero y en Java 21 puede mostrar microsegundos.
   */
  private static final DateTimeFormatter UTC_MILIS =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  public static EventoIncentivos nuevo(String tipo, String entidadId, Object datos) {
    return new EventoIncentivos(
        UUID.randomUUID().toString(), tipo, MODULO, entidadId, ahoraUtc(), datos);
  }

  public static String ahoraUtc() {
    return UTC_MILIS.format(Instant.now().truncatedTo(ChronoUnit.MILLIS));
  }

  /**
   * Routing key en el exchange de eventos, con el formato acordado por el grupo
   * {@code <modulo>.<objeto>.<acción>} (igual que Logística): la última palabra del tipo es la
   * acción y lo anterior, el objeto.
   *
   * <pre>
   * INSIGNIA_CREADA              → incentivos.insignia.creada
   * MISION_ELIMINADA             → incentivos.mision.eliminada
   * PROGRESO_DONADOR_ACTUALIZADO → incentivos.progreso_donador.actualizado
   * MODULO_RESETEADO             → incentivos.modulo.reseteado
   * </pre>
   */
  public String routingKey() {
    String enMinusculas = tipo.toLowerCase(java.util.Locale.ROOT);
    int corte = enMinusculas.lastIndexOf('_');
    if (corte < 0) {
      return MODULO + "." + enMinusculas;
    }
    return MODULO + "." + enMinusculas.substring(0, corte) + "." + enMinusculas.substring(corte + 1);
  }
}
