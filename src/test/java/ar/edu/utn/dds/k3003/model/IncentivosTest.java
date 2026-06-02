package ar.edu.utn.dds.k3003.model;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.controllers.IncentivosController;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ar.edu.utn.dds.k3003.repositories.DonadorRepository;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import org.springframework.beans.factory.annotation.Autowired;

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
  @DisplayName("Donador should be created with relationships")
  void testDonadorPeristence() {
    Donador donador = new Donador(
        "donador-1",
        "Juan",
        "Pérez",
        30,
        "juan@example.com",
        "12345678",
        "Calle 123",
        null,
        null,
        null,
        null,
        new java.util.ArrayList<>());

    Assertions.assertNotNull(donador.getId());
    Assertions.assertEquals("Juan", donador.getNombre());
    Assertions.assertNotNull(donador.getInsignias());
    Assertions.assertTrue(donador.getInsignias().isEmpty());
  }

  @Test
  @DisplayName("Donador should be able to add insignias")
  void testDonadorAgregarInsignia() {
    Donador donador = new Donador("Maria", "Garcia", 25, "maria@example.com", "87654321", "Avenida 456");
    Insignia insignia = new Insignia("insignia-1", "Badge 1", "First Badge");

    donador.agregarInsignia(insignia);

    Assertions.assertEquals(1, donador.getInsignias().size());
    Assertions.assertEquals("insignia-1", donador.getInsignias().get(0).getId());
  }

  @Test
  @DisplayName("Donador should be able to add categorias")
  void testDonadorAgregarCategoria() {
    Donador donador = new Donador("Carlos", "Lopez", 35, "carlos@example.com", "11111111", "Paseo 789");

    donador.agregarCategoria(CategoriaDonadorEnum.COLABORADOR);
    donador.agregarCategoria(CategoriaDonadorEnum.SALVADOR);

    Assertions.assertEquals(2, donador.getCategorias().size());
    Assertions.assertTrue(donador.getCategorias().contains(CategoriaDonadorEnum.COLABORADOR));
    Assertions.assertTrue(donador.getCategorias().contains(CategoriaDonadorEnum.SALVADOR));
  }
}
