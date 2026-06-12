package ar.edu.utn.dds.k3003.integration;

import ar.edu.utn.dds.k3003.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.fachadas.FachadaLogisticaImpl;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Prueba de integración real contra el módulo de Logística desplegado en Render.
 * Verifica que Incentivos pueda comunicarse con Logística a través de FachadaLogistica.
 */
public class LogisticaTest {

  private FachadaLogistica fachadaLogistica;

  @BeforeEach
  void setUp() {
    FachadaLogisticaImpl fachada = new FachadaLogisticaImpl(new RestTemplate());
    ReflectionTestUtils.setField(
        fachada, "logisticaBaseUrl", "https://logistica-i4rf.onrender.com");
    fachadaLogistica = fachada;
  }

  @Test
  void testBuscarDepositoPorIDInexistente() {
    Assertions.assertThrows(
        NoSuchElementException.class,
        () -> fachadaLogistica.buscarDepositoPorID("no-existe"));
  }

  @Test
  void testBuscarAsignacionPorPaqueteIDInexistente() {
    Assertions.assertThrows(
        NoSuchElementException.class,
        () -> fachadaLogistica.buscarAsignacionPorPaqueteID("no-existe"));
  }

  @Test
  void testAgregarDepositoYGestionarDonacion() {
    DepositoDTO nuevo =
        new DepositoDTO(null, null, "test-incentivos-render", "Dirección de prueba", 100, null);

    DepositoDTO creado = fachadaLogistica.agregarDeposito(nuevo);

    Assertions.assertNotNull(creado);
    Assertions.assertNotNull(creado.id());
    Assertions.assertEquals("test-incentivos-render", creado.nombre());

    DepositoDTO recuperado = fachadaLogistica.buscarDepositoPorID(creado.id());
    Assertions.assertEquals(creado.id(), recuperado.id());

    fachadaLogistica.setAlgoritmoMM(creado.id(), TipoAlgoritmoEnum.SUB_ATENDIDOS);

    DepositoDTO actualizado =
        fachadaLogistica.gestionarDonacion(creado.id(), "donacion-test", "producto-test", 5);

    Assertions.assertNotNull(actualizado);
    Assertions.assertEquals(creado.id(), actualizado.id());
  }

  @Test
  void testReportarEntrega() {
    PaqueteDTO paquete =
        new PaqueteDTO("paquete-test-incentivos", "donacion-test", "producto-test", 1);

    Assertions.assertDoesNotThrow(() -> fachadaLogistica.reportarEntrega(paquete));
  }

  @Test
  void testEjecutarMatchmakingNoImplementado() {
    Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> fachadaLogistica.ejecutarMatchmaking(null, null, List.of()));
  }
}
