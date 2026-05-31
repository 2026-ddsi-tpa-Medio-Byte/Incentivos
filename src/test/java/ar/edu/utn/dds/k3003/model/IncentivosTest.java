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
import ar.edu.utn.dds.k3003.repositories.RepoInsignias;
import ar.edu.utn.dds.k3003.repositories.RepoMisiones;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
}
