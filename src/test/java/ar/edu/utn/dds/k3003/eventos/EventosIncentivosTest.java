package ar.edu.utn.dds.k3003.eventos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.controllers.FachadaAdminController;
import ar.edu.utn.dds.k3003.controllers.InsigniaController;
import ar.edu.utn.dds.k3003.controllers.MisionController;
import ar.edu.utn.dds.k3003.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Cada operación del catálogo publica su evento con el sobre completo, recién después de
 * guardar, y una falla de RabbitMQ nunca hace fallar la operación.
 *
 * <p>RabbitMQ, Donaciones y Donadores se simulan; la base es H2. El envío es sincrónico en el
 * test ({@code eventos.envio-sincronico=true}) para poder revisar el mensaje enseguida.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {"eventos.envio-sincronico=true", "eventos.exchange=donatrack.eventos.test"})
@AutoConfigureTestDatabase
class EventosIncentivosTest {

  private static final String EXCHANGE = "donatrack.eventos.test";
  private static final String UTC_CON_MILIS = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  /** Contrato de eventos del grupo (26/10): {@code <modulo>.<objeto>.<acción>}. */
  private static final Map<String, String> ROUTING_KEYS = Map.of(
      "INSIGNIA_CREADA", "incentivos.insignia.creada",
      "INSIGNIA_MODIFICADA", "incentivos.insignia.modificada",
      "INSIGNIA_ELIMINADA", "incentivos.insignia.eliminada",
      "MISION_CREADA", "incentivos.mision.creada",
      "MISION_MODIFICADA", "incentivos.mision.modificada",
      "MISION_ELIMINADA", "incentivos.mision.eliminada",
      "PROGRESO_DONADOR_ACTUALIZADO", "incentivos.progreso_donador.actualizado",
      "MODULO_RESETEADO", "incentivos.modulo.reseteado");

  @Autowired private Fachada fachada;
  @Autowired private FachadaAdminController admin;
  @Autowired private InsigniaController insigniaController;
  @Autowired private MisionController misionController;
  @Autowired private PerfilIncentivosRepository perfiles;
  @Autowired private InsigniaRepository insignias;
  @Autowired private MisionRepository misiones;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private RabbitTemplate rabbitTemplate;
  @MockitoBean private FachadaDonaciones fachadaDonaciones;
  @MockitoBean private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;

  @BeforeEach
  void setUp() {
    perfiles.deleteAll();
    misiones.deleteAll();
    insignias.deleteAll();
    clearInvocations(rabbitTemplate);
    when(fachadaDonadoresYEntidades.buscarDonadorPorID(any()))
        .thenAnswer(inv -> new DonadorDTO(inv.getArgument(0), "Carla", "Rios", 30, "c@test.com",
            "1", "Calle 1", EstadoDonadorEnum.VERIFICADO, "COLABORADOR"));
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private record Publicado(String routingKey, Message mensaje, Map<String, Object> sobre) {}

  /** Mensajes enviados al exchange de eventos, en orden. */
  private List<Publicado> publicados() {
    if (mockingDetails(rabbitTemplate).getInvocations().isEmpty()) {
      return List.of();
    }
    ArgumentCaptor<String> claves = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Message> mensajes = ArgumentCaptor.forClass(Message.class);
    org.mockito.Mockito.verify(rabbitTemplate, atLeast(0))
        .send(eq(EXCHANGE), claves.capture(), mensajes.capture());
    List<Publicado> lista = new ArrayList<>();
    for (int i = 0; i < mensajes.getAllValues().size(); i++) {
      Message m = mensajes.getAllValues().get(i);
      lista.add(new Publicado(claves.getAllValues().get(i), m, leer(m)));
    }
    return lista;
  }

  private List<Publicado> publicados(String tipo, String entidadId) {
    return publicados().stream()
        .filter(p -> tipo.equals(p.sobre().get("tipo")) && entidadId.equals(p.sobre().get("entidadId")))
        .toList();
  }

  private Publicado unico(String tipo, String entidadId) {
    List<Publicado> lista = publicados(tipo, entidadId);
    Assertions.assertEquals(1, lista.size(), "Se esperaba un único " + tipo + " de " + entidadId);
    return lista.get(0);
  }

  private Map<String, Object> leer(Message mensaje) {
    try {
      return objectMapper.readValue(
          new String(mensaje.getBody(), StandardCharsets.UTF_8), new TypeReference<>() {});
    } catch (Exception e) {
      throw new AssertionError("El cuerpo no es JSON válido", e);
    }
  }

  /** El sobre acordado por el grupo, campo por campo, y las propiedades del mensaje. */
  private void assertSobre(Publicado p, String tipo, String entidadId) {
    Map<String, Object> sobre = p.sobre();
    Assertions.assertEquals(
        List.of("eventoId", "tipo", "modulo", "entidadId", "ocurridoEn", "datos"),
        new ArrayList<>(sobre.keySet()), "El sobre tiene exactamente estos campos");
    Assertions.assertDoesNotThrow(() -> UUID.fromString((String) sobre.get("eventoId")));
    Assertions.assertEquals(tipo, sobre.get("tipo"));
    Assertions.assertEquals("incentivos", sobre.get("modulo"));
    Assertions.assertEquals(entidadId, sobre.get("entidadId"));
    Assertions.assertTrue(((String) sobre.get("ocurridoEn")).matches(UTC_CON_MILIS),
        "ocurridoEn en UTC con milisegundos y Z: " + sobre.get("ocurridoEn"));
    Assertions.assertEquals(ROUTING_KEYS.get(tipo), p.routingKey(), "Routing key de " + tipo);
    Assertions.assertEquals("application/json", p.mensaje().getMessageProperties().getContentType());
    Assertions.assertEquals("UTF-8", p.mensaje().getMessageProperties().getContentEncoding());
    Assertions.assertEquals(MessageDeliveryMode.PERSISTENT,
        p.mensaje().getMessageProperties().getDeliveryMode());
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> datos(Publicado p) {
    return (Map<String, Object>) p.sobre().get("datos");
  }

  private static List<DonacionDTO> donaciones(String donadorId, int aceptadas, int ingresadas) {
    List<DonacionDTO> lista = new ArrayList<>();
    for (int i = 0; i < aceptadas; i++) {
      lista.add(new DonacionDTO("a" + i, donadorId, "dep", "x", "prod-" + i, 1, EstadoDonacionEnum.ACEPTADA));
    }
    for (int i = 0; i < ingresadas; i++) {
      lista.add(new DonacionDTO("i" + i, donadorId, "dep", "x", "prod-i" + i, 1, EstadoDonacionEnum.INGRESADA));
    }
    return lista;
  }

  private MisionDTO misionDonacionesExitosas(String id, String insigniaId) {
    return new MisionDTO(id, "Donaciones Exitosas", insigniaId,
        CategoriaDonadorEnum.COLABORADOR, CategoriaDonadorEnum.TRANSFORMADOR,
        TipoMisionEnum.DONACIONES_EXITOSAS);
  }

  // ── Insignias y misiones ──────────────────────────────────────────────────────

  @Test
  void crearInsigniaPublicaInsigniaCreada() {
    fachada.agregarInsignia(new InsigniaDTO("ins-4340", "Donaciones Exitosas", "20 donaciones aceptadas"));

    Publicado p = unico("INSIGNIA_CREADA", "ins-4340");
    assertSobre(p, "INSIGNIA_CREADA", "ins-4340");
    Assertions.assertEquals(
        Map.of("id", "ins-4340", "nombre", "Donaciones Exitosas", "descripcion", "20 donaciones aceptadas"),
        datos(p));
  }

  @Test
  void crearMisionPublicaMisionCreada() {
    fachada.agregarMision(misionDonacionesExitosas("mis-4340", "ins-4340"));

    Publicado p = unico("MISION_CREADA", "mis-4340");
    assertSobre(p, "MISION_CREADA", "mis-4340");
    Assertions.assertEquals(Map.of(
        "id", "mis-4340", "nombre", "Donaciones Exitosas", "insigniaID", "ins-4340",
        "categoriaInicio", "COLABORADOR", "categoriaFin", "TRANSFORMADOR", "tipo", "DONACIONES_EXITOSAS"),
        datos(p));
  }

  @Test
  void elCrudDeInsigniasPublicaModificadaYEliminada() {
    insigniaController.crear(new Insignia("ins-crud", "Vieja", "desc"));
    insigniaController.actualizar("ins-crud", new Insignia(null, "Nueva", null));
    insigniaController.eliminar("ins-crud");

    assertSobre(unico("INSIGNIA_CREADA", "ins-crud"), "INSIGNIA_CREADA", "ins-crud");
    Publicado modificada = unico("INSIGNIA_MODIFICADA", "ins-crud");
    assertSobre(modificada, "INSIGNIA_MODIFICADA", "ins-crud");
    Assertions.assertEquals("Nueva", datos(modificada).get("nombre"));
    Assertions.assertEquals("desc", datos(modificada).get("descripcion"), "Estado completo, no solo lo cambiado");
    Publicado eliminada = unico("INSIGNIA_ELIMINADA", "ins-crud");
    assertSobre(eliminada, "INSIGNIA_ELIMINADA", "ins-crud");
    Assertions.assertEquals(Map.of("id", "ins-crud"), datos(eliminada));
  }

  @Test
  void elCrudDeMisionesPublicaModificadaYEliminada() {
    misionController.crear(new Mision("mis-crud", "Vieja", "ins-x",
        CategoriaDonadorEnum.OCASIONAL, CategoriaDonadorEnum.COLABORADOR, TipoMisionEnum.COMPLETITUD));
    misionController.actualizar("mis-crud", new Mision(null, "Nueva", null, null, null, null));
    misionController.eliminar("mis-crud");

    Publicado modificada = unico("MISION_MODIFICADA", "mis-crud");
    assertSobre(modificada, "MISION_MODIFICADA", "mis-crud");
    Assertions.assertEquals("Nueva", datos(modificada).get("nombre"));
    Assertions.assertEquals("COMPLETITUD", datos(modificada).get("tipo"));
    Assertions.assertEquals(Map.of("id", "mis-crud"), datos(unico("MISION_ELIMINADA", "mis-crud")));
  }

  // ── Progreso del donador ──────────────────────────────────────────────────────

  @Test
  void asignarMisionPublicaElProgresoDeLaMisionEnCurso() {
    fachada.agregarMision(misionDonacionesExitosas("mis-1", "ins-1"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("3"), any())).thenReturn(donaciones("3", 14, 2));

    fachada.asignarMisionADonador("3", misionDonacionesExitosas("mis-1", "ins-1"));

    Publicado p = unico("PROGRESO_DONADOR_ACTUALIZADO", "3");
    assertSobre(p, "PROGRESO_DONADOR_ACTUALIZADO", "3");
    Assertions.assertEquals("3", datos(p).get("donadorId"));
    Assertions.assertEquals(List.of(), datos(p).get("insignias"));
    Assertions.assertEquals(Map.of("id", "mis-1", "nombre", "Donaciones Exitosas",
        "tipo", "DONACIONES_EXITOSAS", "progreso", 14, "objetivo", 20), datos(p).get("misionEnCurso"));
    Assertions.assertFalse(datos(p).containsKey("categoria"), "La categoría la publica Donadores");
  }

  @Test
  void procesarUnDonadorQueCompletaLaMisionPublicaUnSoloEventoConLaInsignia() {
    fachada.agregarInsignia(new InsigniaDTO("ins-2", "Donaciones Exitosas", "20 donaciones aceptadas"));
    fachada.agregarMision(misionDonacionesExitosas("mis-2", "ins-2"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("7"), any())).thenReturn(donaciones("7", 20, 0));
    fachada.asignarMisionADonador("7", misionDonacionesExitosas("mis-2", "ins-2"));
    clearInvocations(rabbitTemplate);

    fachada.procesarDonador("7");

    Publicado p = unico("PROGRESO_DONADOR_ACTUALIZADO", "7");
    assertSobre(p, "PROGRESO_DONADOR_ACTUALIZADO", "7");
    Assertions.assertEquals(List.of(Map.of("id", "ins-2", "nombre", "Donaciones Exitosas")), datos(p).get("insignias"));
    @SuppressWarnings("unchecked")
    Map<String, Object> enCurso = (Map<String, Object>) datos(p).get("misionEnCurso");
    Assertions.assertEquals(20, enCurso.get("progreso"));
    Assertions.assertEquals(20, enCurso.get("objetivo"));
  }

  @Test
  void procesarUnDonadorQuePierdeElProgresoPublicaSinLaInsignia() {
    fachada.agregarInsignia(new InsigniaDTO("ins-3", "Donaciones Exitosas", "20 donaciones aceptadas"));
    fachada.agregarMision(misionDonacionesExitosas("mis-3", "ins-3"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("8"), any())).thenReturn(donaciones("8", 20, 0));
    fachada.asignarMisionADonador("8", misionDonacionesExitosas("mis-3", "ins-3"));
    fachada.procesarDonador("8");
    // Una queja: la donación pasa a CONQUEJA y quedan 19 ACEPTADA.
    List<DonacionDTO> conQueja = new ArrayList<>(donaciones("8", 19, 0));
    conQueja.add(new DonacionDTO("q", "8", "dep", "x", "prod-q", 1, EstadoDonacionEnum.CONQUEJA));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("8"), any())).thenReturn(conQueja);
    clearInvocations(rabbitTemplate);

    fachada.procesarDonador("8");

    Publicado p = unico("PROGRESO_DONADOR_ACTUALIZADO", "8");
    Assertions.assertEquals(List.of(), datos(p).get("insignias"));
    @SuppressWarnings("unchecked")
    Map<String, Object> enCurso = (Map<String, Object>) datos(p).get("misionEnCurso");
    Assertions.assertEquals(19, enCurso.get("progreso"));
  }

  @Test
  void elCronJobPublicaElProgresoDeCadaDonadorQueProcesa() {
    fachada.agregarMision(misionDonacionesExitosas("mis-4", "ins-4"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("9"), any())).thenReturn(donaciones("9", 5, 0));
    fachada.asignarMisionADonador("9", misionDonacionesExitosas("mis-4", "ins-4"));
    clearInvocations(rabbitTemplate);

    admin.procesarPendientes();

    Publicado p = unico("PROGRESO_DONADOR_ACTUALIZADO", "9");
    assertSobre(p, "PROGRESO_DONADOR_ACTUALIZADO", "9");
  }

  @Test
  void unProcesamientoQueFallaNoPublicaNada() {
    fachada.agregarMision(misionDonacionesExitosas("mis-5", "ins-5"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("10"), any())).thenReturn(donaciones("10", 1, 0));
    fachada.asignarMisionADonador("10", misionDonacionesExitosas("mis-5", "ins-5"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("10"), any())).thenReturn(List.of());
    clearInvocations(rabbitTemplate);

    Assertions.assertThrows(NoSuchElementException.class, () -> fachada.procesarDonador("10"));

    Assertions.assertTrue(publicados("PROGRESO_DONADOR_ACTUALIZADO", "10").isEmpty(),
        "Un cambio que no se confirmó no se anuncia");
  }

  @Test
  void unDonadorSinMisionPublicaMisionEnCursoNula() {
    fachada.asignarInsigniaADonador("11", new InsigniaDTO("ins-6", "Manual", "otorgada a mano"));

    Publicado p = unico("PROGRESO_DONADOR_ACTUALIZADO", "11");
    Assertions.assertTrue(datos(p).containsKey("misionEnCurso"));
    Assertions.assertNull(datos(p).get("misionEnCurso"));
    Assertions.assertEquals(List.of(Map.of("id", "ins-6", "nombre", "Manual")), datos(p).get("insignias"));
  }

  // ── Reset y republicación ─────────────────────────────────────────────────────

  @Test
  void vaciarLaBasePublicaModuloReseteado() {
    admin.clearAll();

    Publicado p = unico("MODULO_RESETEADO", "*");
    assertSobre(p, "MODULO_RESETEADO", "*");
    Assertions.assertEquals(Map.of(), datos(p));
  }

  @Test
  void republicarPublicaElEstadoActualDeTodo() {
    fachada.agregarInsignia(new InsigniaDTO("ins-7", "Donaciones Exitosas", "20 donaciones aceptadas"));
    fachada.agregarMision(misionDonacionesExitosas("mis-7", "ins-7"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("12"), any())).thenReturn(donaciones("12", 3, 0));
    fachada.asignarMisionADonador("12", misionDonacionesExitosas("mis-7", "ins-7"));
    clearInvocations(rabbitTemplate);

    Map<String, Integer> resumen = admin.republicarEventos().getBody();

    Assertions.assertEquals(Map.of("insignias", 1, "misiones", 1, "donadores", 1), resumen);
    assertSobre(unico("INSIGNIA_CREADA", "ins-7"), "INSIGNIA_CREADA", "ins-7");
    assertSobre(unico("MISION_CREADA", "mis-7"), "MISION_CREADA", "mis-7");
    assertSobre(unico("PROGRESO_DONADOR_ACTUALIZADO", "12"), "PROGRESO_DONADOR_ACTUALIZADO", "12");
  }

  // ── RabbitMQ caído ────────────────────────────────────────────────────────────

  @Test
  void siRabbitFallaLasOperacionesIgualRespondenBien() {
    doThrow(new AmqpConnectException(new java.net.ConnectException("RabbitMQ caído")))
        .when(rabbitTemplate).send(anyString(), anyString(), any(Message.class));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("13"), any())).thenReturn(donaciones("13", 20, 0));

    InsigniaDTO insignia = Assertions.assertDoesNotThrow(() ->
        fachada.agregarInsignia(new InsigniaDTO("ins-8", "Donaciones Exitosas", "20")));
    Assertions.assertDoesNotThrow(() -> fachada.agregarMision(misionDonacionesExitosas("mis-8", "ins-8")));
    Assertions.assertDoesNotThrow(() -> fachada.asignarMisionADonador("13", misionDonacionesExitosas("mis-8", "ins-8")));
    Assertions.assertDoesNotThrow(() -> fachada.procesarDonador("13"));
    Assertions.assertEquals(200, admin.clearAll().getStatusCode().value());

    Assertions.assertEquals("ins-8", insignia.id());
  }
}
