package ar.edu.utn.dds.k3003.integration;

import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorStatsDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonadoresYEntidadesImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Prueba de integración real contra el módulo de Donadores y Entidades desplegado en Render.
 * Verifica que Incentivos pueda comunicarse con Donadores y Entidades a través de
 * FachadaDonadoresYEntidades.
 */
public class DonadoresYEntidadesTest {

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;

  @BeforeEach
  void setUp() {
    FachadaDonadoresYEntidadesImpl fachada = new FachadaDonadoresYEntidadesImpl(new RestTemplate());
    ReflectionTestUtils.setField(
        fachada, "donadoresYEntidadesBaseUrl", "https://donadoresyentidadesv2.onrender.com");
    fachadaDonadoresYEntidades = fachada;
  }

  /** Genera un DonadorDTO con datos únicos para evitar colisiones en el servicio compartido. */
  private DonadorDTO nuevoDonador(String nombre) {
    String sufijo = UUID.randomUUID().toString().substring(0, 8);
    return new DonadorDTO(
        null,
        nombre,
        "Incentivos",
        25,
        "test-incentivos-" + sufijo + "@render.com",
        sufijo,
        "Domicilio de prueba",
        EstadoDonadorEnum.VERIFICADO,
        "Ocasional");
  }

  @Test
  void testAgregarYBuscarDonador() {
    DonadorDTO creado = fachadaDonadoresYEntidades.agregarDonador(nuevoDonador("Test"));

    Assertions.assertNotNull(creado);
    Assertions.assertNotNull(creado.id());
    Assertions.assertFalse(creado.id().isEmpty());
    Assertions.assertEquals("Test", creado.nombre());

    DonadorDTO recuperado = fachadaDonadoresYEntidades.buscarDonadorPorID(creado.id());

    Assertions.assertEquals(creado.id(), recuperado.id());
    Assertions.assertEquals(creado.email(), recuperado.email());
  }

  @Test
  void testBuscarDonadorPorIDInexistente() {
    Assertions.assertThrows(
        NoSuchElementException.class,
        () -> fachadaDonadoresYEntidades.buscarDonadorPorID("no-existe"));
  }

  @Test
  void testAgregarYBuscarEntidad() {
    String sufijo = UUID.randomUUID().toString().substring(0, 8);
    EntidadBeneficaDTO nueva =
        new EntidadBeneficaDTO(
            null,
            "Entidad de prueba Incentivos " + sufijo,
            "Domicilio entidad",
            "1100000000",
            "entidad-" + sufijo + "@render.com");

    EntidadBeneficaDTO creada = fachadaDonadoresYEntidades.agregarEntidad(nueva);

    Assertions.assertNotNull(creada);
    Assertions.assertNotNull(creada.id());
    Assertions.assertEquals(nueva.razonSocial(), creada.razonSocial());

    EntidadBeneficaDTO recuperada = fachadaDonadoresYEntidades.buscarEntidadPorID(creada.id());

    Assertions.assertEquals(creada.id(), recuperada.id());
    Assertions.assertEquals(creada.razonSocial(), recuperada.razonSocial());
  }

  @Test
  void testBuscarEntidadPorIDInexistente() {
    Assertions.assertThrows(
        NoSuchElementException.class,
        () -> fachadaDonadoresYEntidades.buscarEntidadPorID("no-existe"));
  }

  @Test
  void testPuedeDonarYEstadisticas() {
    DonadorDTO creado = fachadaDonadoresYEntidades.agregarDonador(nuevoDonador("Test2"));

    Boolean puedeDonar = fachadaDonadoresYEntidades.puedeDonar(creado.id());
    Assertions.assertNotNull(puedeDonar);

    DonadorStatsDTO stats = fachadaDonadoresYEntidades.estadisticasDonador(creado.id());
    Assertions.assertEquals(creado.id(), stats.id());
    Assertions.assertEquals(creado.categoria(), stats.categoria());
  }

  @Test
  void testModificarEstadoYCategoria() {
    DonadorDTO creado = fachadaDonadoresYEntidades.agregarDonador(nuevoDonador("Test3"));

    DonadorDTO actualizado =
        fachadaDonadoresYEntidades.modificarEstado(creado.id(), EstadoDonadorEnum.SOSPECHOSO);
    Assertions.assertEquals(EstadoDonadorEnum.SOSPECHOSO, actualizado.estado());

    DonadorDTO conNuevaCategoria =
        fachadaDonadoresYEntidades.modifcarCategoria(creado.id(), "Colaborador");
    Assertions.assertEquals("Colaborador", conNuevaCategoria.categoria());
  }

  @Test
  void testQuejas() {
    DonadorDTO creado = fachadaDonadoresYEntidades.agregarDonador(nuevoDonador("Test4"));

    QuejaDTO queja =
        new QuejaDTO(null, "donacion-test", creado.id(), LocalDate.now(), "Queja de prueba");

    QuejaDTO quejaCreada = fachadaDonadoresYEntidades.agregarQueja(queja);
    Assertions.assertNotNull(quejaCreada);
    Assertions.assertEquals(creado.id(), quejaCreada.donadorID());

    List<QuejaDTO> quejas = fachadaDonadoresYEntidades.obtenerQuejasDe(creado.id());
    Assertions.assertFalse(quejas.isEmpty());
  }

  @Test
  void testNecesidades() {
    String sufijo = UUID.randomUUID().toString().substring(0, 8);
    EntidadBeneficaDTO entidad =
        fachadaDonadoresYEntidades.agregarEntidad(
            new EntidadBeneficaDTO(
                null,
                "Entidad necesidades " + sufijo,
                "Domicilio",
                "1100000001",
                "necesidades-" + sufijo + "@render.com"));

    NecesidadMaterialDTO necesidad =
        new NecesidadMaterialDTO(
            null,
            entidad.id(),
            5,
            "Necesidad de prueba",
            10,
            "producto-test-" + sufijo,
            TipoNecesidadMaterialEnum.EXTRAORDINARIA);

    NecesidadMaterialDTO creada = fachadaDonadoresYEntidades.registrarNecesidad(necesidad);
    Assertions.assertNotNull(creada);
    Assertions.assertNotNull(creada.id());

    List<NecesidadMaterialDTO> insatisfechas =
        fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe("producto-test-" + sufijo);
    Assertions.assertFalse(insatisfechas.isEmpty());

    NecesidadMaterialDTO satisfecha =
        fachadaDonadoresYEntidades.satisfacerNecesidad(creada.id(), 5);
    Assertions.assertNotNull(satisfecha);
    Assertions.assertEquals(creada.id(), satisfecha.id());
  }
}
