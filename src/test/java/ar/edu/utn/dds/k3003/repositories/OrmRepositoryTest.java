package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.Donador;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Pruebas del ORM (JPA/Hibernate) sobre las entidades Donador, Insignia y Mision.
 *
 * <p>Usa {@code @DataJpaTest}, que reemplaza el datasource configurado (PostgreSQL en Render)
 * por una base embebida H2, ejecutando cada test en su propia transacción con rollback. Esto
 * valida el mapeo JPA (anotaciones, relaciones, enums) sin escribir en la base de datos de
 * producción de Render.
 */
@DataJpaTest
class OrmRepositoryTest {

  @Autowired private DonadorRepository donadorRepository;
  @Autowired private InsigniaRepository insigniaRepository;
  @Autowired private MisionRepository misionRepository;

  @Test
  void testGuardarYBuscarInsignia() {
    insigniaRepository.save(new Insignia("insignia-orm-1", "Primer Paso", "Otorgada en la primera donación"));

    Insignia recuperada = insigniaRepository.findById("insignia-orm-1").orElseThrow();

    Assertions.assertEquals("Primer Paso", recuperada.getNombre());
    Assertions.assertEquals("Otorgada en la primera donación", recuperada.getDescripcion());
  }

  @Test
  void testGuardarYBuscarMisionConEnums() {
    Mision mision =
        new Mision(
            "mision-orm-1",
            "Donar 5 veces",
            "insignia-orm-1",
            CategoriaDonadorEnum.COLABORADOR,
            CategoriaDonadorEnum.SALVADOR,
            TipoMisionEnum.DONACIONES_EXITOSAS);

    misionRepository.save(mision);

    Mision recuperada = misionRepository.findById("mision-orm-1").orElseThrow();

    Assertions.assertEquals(CategoriaDonadorEnum.COLABORADOR, recuperada.getCategoriaInicio());
    Assertions.assertEquals(CategoriaDonadorEnum.SALVADOR, recuperada.getCategoriaFin());
    Assertions.assertEquals(TipoMisionEnum.DONACIONES_EXITOSAS, recuperada.getTipo());
  }

  @Test
  void testGuardarDonadorConInsigniasYCategorias() {
    Insignia insignia = insigniaRepository.save(new Insignia("insignia-orm-2", "Veterano", "5+ donaciones"));

    Donador donador = new Donador("Ana", "Gomez", 28, "ana@example.com", "20200200", "Calle Falsa 123");
    donador.setId("donador-orm-1");
    donador.setEstado(EstadoDonadorEnum.VERIFICADO);
    donador.setCategoria("Colaborador");
    donador.agregarInsignia(insignia);
    donador.agregarCategoria(CategoriaDonadorEnum.COLABORADOR);

    donadorRepository.save(donador);

    Donador recuperado = donadorRepository.findById("donador-orm-1").orElseThrow();

    Assertions.assertEquals("Ana", recuperado.getNombre());
    Assertions.assertEquals(EstadoDonadorEnum.VERIFICADO, recuperado.getEstado());
    Assertions.assertEquals(1, recuperado.getInsignias().size());
    Assertions.assertEquals("insignia-orm-2", recuperado.getInsignias().get(0).getId());
    Assertions.assertTrue(recuperado.getCategorias().contains(CategoriaDonadorEnum.COLABORADOR));
  }

  @Test
  void testActualizarDonador() {
    Donador donador = new Donador("Luis", "Martinez", 40, "luis@example.com", "30300300", "Otra Calle 456");
    donador.setId("donador-orm-2");
    donadorRepository.save(donador);

    Donador guardado = donadorRepository.findById("donador-orm-2").orElseThrow();
    guardado.setCategoria("Salvador");
    guardado.setMisionActualID("mision-orm-1");
    donadorRepository.save(guardado);

    Donador actualizado = donadorRepository.findById("donador-orm-2").orElseThrow();

    Assertions.assertEquals("Salvador", actualizado.getCategoria());
    Assertions.assertEquals("mision-orm-1", actualizado.getMisionActualID());
  }

  @Test
  void testEliminarInsignia() {
    insigniaRepository.save(new Insignia("insignia-orm-3", "Temporal", "Para borrar"));
    Assertions.assertTrue(insigniaRepository.existsById("insignia-orm-3"));

    insigniaRepository.deleteById("insignia-orm-3");

    Assertions.assertFalse(insigniaRepository.existsById("insignia-orm-3"));
  }

  @Test
  void testBuscarDonadorInexistente() {
    Assertions.assertTrue(donadorRepository.findById("no-existe").isEmpty());
  }

  @Test
  void testListarTodosLosDonadores() {
    donadorRepository.save(crearDonador("donador-orm-3", "Pedro"));
    donadorRepository.save(crearDonador("donador-orm-4", "Sofia"));

    List<Donador> donadores = donadorRepository.findAll();

    Assertions.assertTrue(donadores.size() >= 2);
  }

  private Donador crearDonador(String id, String nombre) {
    Donador donador =
        new Donador(nombre, "Apellido", 20, nombre + "@example.com", id + "-doc", "Domicilio");
    donador.setId(id);
    return donador;
  }
}
