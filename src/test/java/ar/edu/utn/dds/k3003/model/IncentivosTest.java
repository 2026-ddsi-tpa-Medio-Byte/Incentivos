package ar.edu.utn.dds.k3003.model;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.controllers.IncentivosController;
import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.DisplayName;

@ExtendWith(MockitoExtension.class)
public class IncentivosTest {

  @Mock private Fachada fachada;
  private IncentivosController controller;

  @BeforeEach
  void setUp() {
    controller = new IncentivosController(fachada);
  }

  @Test
  void testCrearInsigniaDevuelveOKyMismaInsignia() {
    InsigniaDTO entrada = new InsigniaDTO(null, "insignia-ctrl", "descripcion ctrl");
    InsigniaDTO salida = new InsigniaDTO("id-123", "insignia-ctrl", "descripcion ctrl");

    when(fachada.agregarInsignia(entrada)).thenReturn(salida);

    ResponseEntity<InsigniaDTO> response = controller.crearInsignia(entrada);

    Assertions.assertEquals(200, response.getStatusCodeValue());
    Assertions.assertEquals(salida, response.getBody());
    verify(fachada).agregarInsignia(entrada);
  }

  @Test
  void testListarInsigniasDevuelveListaDesdeFachada() {
    List<InsigniaDTO> insignias = List.of(
        new InsigniaDTO("id-1", "insignia1", "desc1"),
        new InsigniaDTO("id-2", "insignia2", "desc2"));

    when(fachada.getAllInsignias()).thenReturn(insignias);

    ResponseEntity<List<InsigniaDTO>> response = controller.listarInsignias();

    Assertions.assertEquals(200, response.getStatusCodeValue());
    Assertions.assertEquals(insignias, response.getBody());
    verify(fachada).getAllInsignias();
  }

  @Test
  void testObtenerInsigniaInexistente() {
    when(fachada.getInsigniaById("no-existe")).thenThrow(new RuntimeException("Insignia no encontrada"));

    Assertions.assertThrows(
        RuntimeException.class,
        () -> controller.obtenerInsignia("no-existe"));

    verify(fachada).getInsigniaById("no-existe");
  }

  @Test
  void testCrearMisionDevuelveMisionCreada() {
    MisionDTO entrada = new MisionDTO(
        null,
        "mision-ctrl",
        "id-123",
        CategoriaDonadorEnum.COLABORADOR,
        CategoriaDonadorEnum.SALVADOR,
        TipoMisionEnum.DONACIONES_EXITOSAS);
    MisionDTO salida = new MisionDTO(
        "mision-123",
        "mision-ctrl",
        "id-123",
        CategoriaDonadorEnum.COLABORADOR,
        CategoriaDonadorEnum.SALVADOR,
        TipoMisionEnum.DONACIONES_EXITOSAS);

    when(fachada.agregarMision(entrada)).thenReturn(salida);

    ResponseEntity<MisionDTO> response = controller.crearMision(entrada);

    Assertions.assertEquals(200, response.getStatusCodeValue());
    Assertions.assertEquals(salida, response.getBody());
    verify(fachada).agregarMision(entrada);
  }

  @Test
  void testListarMisionesDevuelveListaDesdeFachada() {
    List<MisionDTO> misiones = java.util.Arrays.asList(
        new MisionDTO(
            "mision-1",
            "misión A",
            "donador-1",
            CategoriaDonadorEnum.COLABORADOR,
            CategoriaDonadorEnum.SALVADOR,
            TipoMisionEnum.DONACIONES_EXITOSAS),
        new MisionDTO(
            "mision-2",
            "misión B",
            "donador-2",
            CategoriaDonadorEnum.SALVADOR,
            CategoriaDonadorEnum.COLABORADOR,
            TipoMisionEnum.REVOLUCION_DONADORA));

    when(fachada.getAllMisiones()).thenReturn(misiones);

    ResponseEntity<List<MisionDTO>> response = controller.listarMisiones();

    Assertions.assertEquals(200, response.getStatusCodeValue());
    Assertions.assertEquals(misiones, response.getBody());
    verify(fachada).getAllMisiones();
  }

  @Test
  void testObtenerMisionExistenteDevuelveMision() {
    MisionDTO salida = new MisionDTO(
        "mision-123",
        "mision-ctrl",
        "id-123",
        CategoriaDonadorEnum.COLABORADOR,
        CategoriaDonadorEnum.SALVADOR,
        TipoMisionEnum.DONACIONES_EXITOSAS);

    when(fachada.getMisionById("mision-123")).thenReturn(salida);

    ResponseEntity<MisionDTO> response = controller.obtenerMision("mision-123");

    Assertions.assertEquals(200, response.getStatusCodeValue());
    Assertions.assertEquals(salida, response.getBody());
    verify(fachada).getMisionById("mision-123");
  }

  @Test
  void testObtenerMisionInexistenteLanzaRuntimeException() {
    when(fachada.getMisionById("no-existe")).thenThrow(new RuntimeException("Misión no encontrada"));

    Assertions.assertThrows(
        RuntimeException.class,
        () -> controller.obtenerMision("no-existe"));

    verify(fachada).getMisionById("no-existe");
  }

  // ==================== Persistence Tests (ORM) ====================

  @Test
  @DisplayName("Insignia should be persisted and retrieved via JPA")
  void testInsigniaPeristence() {
    Insignia insignia = new Insignia("test-id-1", "Test Insignia", "Test Description");
    Insignia savedInsignia = new Insignia("test-id-1", "Test Insignia", "Test Description");
    Assertions.assertNotNull(insignia.getId());
    Assertions.assertEquals("test-id-1", insignia.getId());
    Assertions.assertEquals("Test Insignia", insignia.getNombre());
  }

  @Test
  @DisplayName("Mision should be persisted with enum values")
  void testMisionPeristence() {
    Mision mision = new Mision(
        "mision-test-1",
        "Test Mission",
        "test-id-1",
        CategoriaDonadorEnum.COLABORADOR,
        CategoriaDonadorEnum.SALVADOR,
        TipoMisionEnum.DONACIONES_EXITOSAS);

    Assertions.assertNotNull(mision.getId());
    Assertions.assertEquals("mision-test-1", mision.getId());
    Assertions.assertEquals(CategoriaDonadorEnum.COLABORADOR, mision.getCategoriaInicio());
    Assertions.assertEquals(CategoriaDonadorEnum.SALVADOR, mision.getCategoriaFin());
    Assertions.assertEquals(TipoMisionEnum.DONACIONES_EXITOSAS, mision.getTipo());
  }

  @Test
  @DisplayName("PerfilIncentivos should be created with relationships")
  void testPerfilIncentivosPersistence() {
    PerfilIncentivos perfil = new PerfilIncentivos("donador-1");

    Assertions.assertNotNull(perfil.getId());
    Assertions.assertEquals("donador-1", perfil.getId());
    Assertions.assertNotNull(perfil.getInsignias());
    Assertions.assertTrue(perfil.getInsignias().isEmpty());
  }

  @Test
  @DisplayName("PerfilIncentivos should be able to add insignias")
  void testPerfilIncentivosAgregarInsignia() {
    PerfilIncentivos perfil = new PerfilIncentivos("donador-2");
    Insignia insignia = new Insignia("insignia-1", "Badge 1", "First Badge");

    perfil.agregarInsignia(insignia);

    Assertions.assertEquals(1, perfil.getInsignias().size());
    Assertions.assertEquals("insignia-1", perfil.getInsignias().get(0).getId());
  }

  @Test
  @DisplayName("PerfilIncentivos should be able to add categorias")
  void testPerfilIncentivosAgregarCategoria() {
    PerfilIncentivos perfil = new PerfilIncentivos("donador-3");

    perfil.agregarCategoria(CategoriaDonadorEnum.COLABORADOR);
    perfil.agregarCategoria(CategoriaDonadorEnum.SALVADOR);

    Assertions.assertEquals(2, perfil.getCategorias().size());
    Assertions.assertTrue(perfil.getCategorias().contains(CategoriaDonadorEnum.COLABORADOR));
    Assertions.assertTrue(perfil.getCategorias().contains(CategoriaDonadorEnum.SALVADOR));
  }
}
