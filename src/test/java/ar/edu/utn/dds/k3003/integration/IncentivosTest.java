package ar.edu.utn.dds.k3003.integration;

import ar.edu.utn.dds.k3003.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.TipoIdentificadorEnum;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonacionesImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Prueba de integración real contra el módulo de Donaciones desplegado en Render.
 * Verifica que Incentivos pueda comunicarse con Donaciones a través de FachadaDonaciones.
 */
public class IncentivosTest {

  private FachadaDonaciones fachadaDonaciones;

  @BeforeEach
  void setUp() {
    FachadaDonacionesImpl fachada = new FachadaDonacionesImpl(new RestTemplate());
    ReflectionTestUtils.setField(
        fachada, "donacionesBaseUrl", "https://donatrack-donaciones.onrender.com");
    fachadaDonaciones = fachada;
  }

  @Test
  void testConexionConModuloDeDonaciones() {
    IdentificadorDTO nuevo =
        new IdentificadorDTO(null, TipoIdentificadorEnum.CODIGODEBARRAS, "test-incentivos-render");

    IdentificadorDTO creado = fachadaDonaciones.agregarIdentificador(nuevo);

    Assertions.assertNotNull(creado);
    Assertions.assertNotNull(creado.id());
    Assertions.assertEquals(TipoIdentificadorEnum.CODIGODEBARRAS, creado.tipo());

    IdentificadorDTO recuperado = fachadaDonaciones.buscarIdentificadorPorID(creado.id());

    Assertions.assertEquals(creado.id(), recuperado.id());
    Assertions.assertEquals(creado.descripcion(), recuperado.descripcion());
  }
}
