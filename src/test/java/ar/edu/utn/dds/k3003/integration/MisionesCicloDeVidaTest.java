package ar.edu.utn.dds.k3003.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
import ar.edu.utn.dds.k3003.scheduler.ProcesamientoMisionesScheduler;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integración del ciclo de vida de una misión dentro de Incentivos: otorgarla al
 * cumplirse, el Cron-Job que procesa donadores pendientes, y la pérdida de progreso agregada
 * en la Entrega 4.
 *
 * <p>A diferencia de {@link IncentivosTest} (que pega contra Donaciones real en Render), acá
 * Donaciones y Donadores y Entidades se simulan con Mockito. Es la única forma de reproducir de
 * forma repetible el escenario "20 donaciones ACEPTADA y después bajan por una queja", que
 * depende de datos que viven en otros módulos. Corre contra H2 en memoria: no requiere red ni
 * credenciales, así que cualquiera del equipo lo puede correr con {@code mvn test}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase
@Transactional
class MisionesCicloDeVidaTest {

  @Autowired private Fachada fachada;
  @Autowired private ProcesamientoMisionesScheduler scheduler;
  @Autowired private PerfilIncentivosRepository perfilRepository;
  @Autowired private MisionRepository misionRepository;
  @Autowired private InsigniaRepository insigniaRepository;

  @MockitoBean private FachadaDonaciones fachadaDonaciones;
  @MockitoBean private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;

  @BeforeEach
  void setUp() {
    when(fachadaDonadoresYEntidades.buscarDonadorPorID(any()))
        .thenAnswer(
            inv ->
                new DonadorDTO(
                    inv.getArgument(0),
                    "Donador",
                    "Test",
                    30,
                    "d@test.com",
                    "1",
                    "domicilio",
                    EstadoDonadorEnum.VERIFICADO,
                    "COLABORADOR"));
  }

  private DonacionDTO donacionAceptada(String donadorID, String productoID) {
    return new DonacionDTO(
        "don-" + donadorID + "-" + productoID,
        donadorID,
        "dep-1",
        "test",
        productoID,
        10,
        EstadoDonacionEnum.ACEPTADA);
  }

  @Test
  void procesarDonadorOtorgaInsigniaYCategoriaAlCumplirMision() {
    String donadorID = "donador-ciclo-1";
    Insignia insignia = insigniaRepository.save(new Insignia("insignia-ciclo-1", "20 Donaciones", "desc"));
    Mision mision =
        misionRepository.save(
            new Mision(
                "mision-ciclo-1",
                "Donaciones Exitosas",
                insignia.getId(),
                CategoriaDonadorEnum.COLABORADOR,
                CategoriaDonadorEnum.TRANSFORMADOR,
                TipoMisionEnum.DONACIONES_EXITOSAS));

    List<DonacionDTO> veinteDonaciones = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      veinteDonaciones.add(donacionAceptada(donadorID, "prod-" + i));
    }
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq(donadorID), any()))
        .thenReturn(veinteDonaciones);

    fachada.asignarMisionADonador(
        donadorID,
        new MisionDTO(
            mision.getId(),
            mision.getNombre(),
            mision.getInsigniaID(),
            mision.getCategoriaInicio(),
            mision.getCategoriaFin(),
            mision.getTipo()));

    fachada.procesarDonador(donadorID);

    PerfilIncentivos perfil = perfilRepository.findById(donadorID).orElseThrow();
    Assertions.assertTrue(
        perfil.getInsignias().stream().anyMatch(i -> i.getId().equals(insignia.getId())),
        "Debería haber recibido la insignia de la misión cumplida");
    Assertions.assertTrue(
        perfil.getCategorias().contains(CategoriaDonadorEnum.TRANSFORMADOR),
        "Debería haber avanzado a la categoriaFin de la misión");
    // El aviso a Donadores sale recién al confirmar la transacción.
    verify(fachadaDonadoresYEntidades, never()).modifcarCategoria(any(), any());
    confirmarTransaccion();
    verify(fachadaDonadoresYEntidades).modifcarCategoria(donadorID, "TRANSFORMADOR");
  }

  @Test
  void procesarDonadorRetiraInsigniaYRevierteCategoriaSiBajanLasDonacionesAceptadas() {
    String donadorID = "donador-ciclo-2";
    Insignia insignia = insigniaRepository.save(new Insignia("insignia-ciclo-2", "20 Donaciones", "desc"));
    Mision mision =
        misionRepository.save(
            new Mision(
                "mision-ciclo-2",
                "Donaciones Exitosas",
                insignia.getId(),
                CategoriaDonadorEnum.COLABORADOR,
                CategoriaDonadorEnum.TRANSFORMADOR,
                TipoMisionEnum.DONACIONES_EXITOSAS));

    // Simula un donador que ya había completado la misión en un procesamiento anterior.
    PerfilIncentivos perfil = new PerfilIncentivos(donadorID);
    perfil.agregarInsignia(insignia);
    perfil.agregarCategoria(CategoriaDonadorEnum.TRANSFORMADOR);
    perfilRepository.save(perfil);

    // Ahora solo tiene 15 ACEPTADA: bajó de 20 porque recibió una queja en Donaciones.
    List<DonacionDTO> quinceDonaciones = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      quinceDonaciones.add(donacionAceptada(donadorID, "prod-" + i));
    }
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq(donadorID), any()))
        .thenReturn(quinceDonaciones);

    fachada.procesarDonador(donadorID);

    PerfilIncentivos actualizado = perfilRepository.findById(donadorID).orElseThrow();
    Assertions.assertTrue(
        actualizado.getInsignias().stream().noneMatch(i -> i.getId().equals(insignia.getId())),
        "La insignia debería haberse retirado al caer por debajo de 20 donaciones ACEPTADA");
    Assertions.assertTrue(
        actualizado.getCategorias().contains(CategoriaDonadorEnum.COLABORADOR),
        "Debería volver a la categoriaInicio de la misión");
    Assertions.assertEquals(
        mision.getId(),
        actualizado.getMisionActualID(),
        "La misión debería volver a quedar en curso para que la recumpla");
    confirmarTransaccion();
    verify(fachadaDonadoresYEntidades).modifcarCategoria(donadorID, "COLABORADOR");
  }

  /** Perfil que ya cumplió una misión DONACIONES_EXITOSAS y tiene su insignia. */
  private Insignia perfilConInsigniaDeDonacionesExitosas(String donadorID, String sufijo) {
    Insignia insignia =
        insigniaRepository.save(new Insignia("insignia-" + sufijo, "20 Donaciones", "desc"));
    misionRepository.save(
        new Mision(
            "mision-" + sufijo,
            "Donaciones Exitosas",
            insignia.getId(),
            CategoriaDonadorEnum.COLABORADOR,
            CategoriaDonadorEnum.TRANSFORMADOR,
            TipoMisionEnum.DONACIONES_EXITOSAS));
    PerfilIncentivos perfil = new PerfilIncentivos(donadorID);
    perfil.agregarInsignia(insignia);
    perfil.agregarCategoria(CategoriaDonadorEnum.TRANSFORMADOR);
    perfil.setMisionActualID("mision-" + sufijo);
    perfilRepository.save(perfil);
    return insignia;
  }

  private void assertConservaInsignia(String donadorID, Insignia insignia) {
    PerfilIncentivos perfil = perfilRepository.findById(donadorID).orElseThrow();
    Assertions.assertTrue(
        perfil.getInsignias().stream().anyMatch(i -> i.getId().equals(insignia.getId())),
        "Sin datos confiables de Donaciones no se puede afirmar que perdió progreso");
  }

  @Test
  void noRetiraLaInsigniaSiDonacionesNoResponde() {
    String donadorID = "donador-ciclo-caido";
    Insignia insignia = perfilConInsigniaDeDonacionesExitosas(donadorID, "ciclo-caido");
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq(donadorID), any()))
        .thenThrow(new java.util.NoSuchElementException("Donaciones no disponible"));

    Assertions.assertThrows(
        java.util.NoSuchElementException.class, () -> fachada.procesarDonador(donadorID));

    assertConservaInsignia(donadorID, insignia);
    verify(fachadaDonadoresYEntidades, never()).modifcarCategoria(any(), any());
  }

  @Test
  void noRetiraLaInsigniaSiDonacionesNoTieneDonacionesDelDonador() {
    // Es lo que pasa cuando se resetea la base de Donaciones y la de Incentivos no.
    String donadorID = "donador-ciclo-reset";
    Insignia insignia = perfilConInsigniaDeDonacionesExitosas(donadorID, "ciclo-reset");
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq(donadorID), any()))
        .thenReturn(List.of());

    Assertions.assertThrows(
        java.util.NoSuchElementException.class, () -> fachada.procesarDonador(donadorID));

    assertConservaInsignia(donadorID, insignia);
    verify(fachadaDonadoresYEntidades, never()).modifcarCategoria(any(), any());
  }

  @Test
  void noAvisaADonadoresSiElProcesamientoFallaDespuesDeLaPerdida() {
    // Primera consulta: 15 ACEPTADA (pierde progreso). Segunda: Donaciones deja de responder.
    String donadorID = "donador-ciclo-a-medias";
    perfilConInsigniaDeDonacionesExitosas(donadorID, "ciclo-a-medias");
    List<DonacionDTO> quinceDonaciones = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      quinceDonaciones.add(donacionAceptada(donadorID, "prod-" + i));
    }
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq(donadorID), any()))
        .thenReturn(quinceDonaciones)
        .thenThrow(new java.util.NoSuchElementException("Donaciones no disponible"));

    Assertions.assertThrows(
        java.util.NoSuchElementException.class, () -> fachada.procesarDonador(donadorID));

    // La transacción quedó marcada para rollback: el aviso a Donadores nunca se envía.
    TestTransaction.end();
    verify(fachadaDonadoresYEntidades, never()).modifcarCategoria(any(), any());
  }

  /** Confirma la transacción del test para que corran los avisos registrados para después del commit. */
  private void confirmarTransaccion() {
    TestTransaction.flagForCommit();
    TestTransaction.end();
  }

  @Test
  void schedulerSoloProcesaDonadoresConMisionAsignada() {
    misionRepository.save(
        new Mision(
            "mision-cualquiera",
            "Cualquiera",
            null,
            CategoriaDonadorEnum.OCASIONAL,
            CategoriaDonadorEnum.OCASIONAL,
            TipoMisionEnum.COMPLETITUD));

    PerfilIncentivos conMision = new PerfilIncentivos("donador-con-mision");
    conMision.setMisionActualID("mision-cualquiera");
    perfilRepository.save(conMision);
    perfilRepository.save(new PerfilIncentivos("donador-sin-mision"));

    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("donador-con-mision"), any()))
        .thenReturn(List.of(donacionAceptada("donador-con-mision", "prod-x")));

    scheduler.procesarDonadoresConMisionAsignada();

    verify(fachadaDonadoresYEntidades).buscarDonadorPorID("donador-con-mision");
    verify(fachadaDonadoresYEntidades, never()).buscarDonadorPorID("donador-sin-mision");
  }

  @Test
  void schedulerNoSeDetieneSiUnDonadorFalla() {
    misionRepository.save(
        new Mision(
            "mision-batch",
            "Cualquiera",
            null,
            CategoriaDonadorEnum.OCASIONAL,
            CategoriaDonadorEnum.OCASIONAL,
            TipoMisionEnum.COMPLETITUD));

    PerfilIncentivos falla = new PerfilIncentivos("donador-falla");
    falla.setMisionActualID("mision-batch");
    perfilRepository.save(falla);
    PerfilIncentivos ok = new PerfilIncentivos("donador-ok");
    ok.setMisionActualID("mision-batch");
    perfilRepository.save(ok);

    when(fachadaDonadoresYEntidades.buscarDonadorPorID("donador-falla"))
        .thenThrow(new RuntimeException("Donaciones no disponible"));
    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq("donador-ok"), any()))
        .thenReturn(List.of(donacionAceptada("donador-ok", "prod-x")));

    Assertions.assertDoesNotThrow(() -> scheduler.procesarDonadoresConMisionAsignada());

    verify(fachadaDonadoresYEntidades).buscarDonadorPorID("donador-ok");
  }
}
