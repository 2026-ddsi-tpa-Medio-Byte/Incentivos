package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Pruebas del ORM (JPA/Hibernate) sobre las entidades PerfilIncentivos, Insignia y Mision.
 *
 * <p>Usa {@code @DataJpaTest}, que reemplaza el datasource configurado (PostgreSQL en Render)
 * por una base embebida H2, ejecutando cada test en su propia transacción con rollback. Esto
 * valida el mapeo JPA (anotaciones, relaciones, enums) sin escribir en la base de datos de
 * producción de Render.
 */
@DataJpaTest
class OrmRepositoryTest {

  @Autowired private PerfilIncentivosRepository perfilRepository;
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
  void testGuardarPerfilConInsigniasYCategorias() {
    Insignia insignia = insigniaRepository.save(new Insignia("insignia-orm-2", "Veterano", "5+ donaciones"));

    PerfilIncentivos perfil = new PerfilIncentivos("donador-orm-1");
    perfil.agregarInsignia(insignia);
    perfil.agregarCategoria(CategoriaDonadorEnum.COLABORADOR);

    perfilRepository.save(perfil);

    PerfilIncentivos recuperado = perfilRepository.findById("donador-orm-1").orElseThrow();

    Assertions.assertEquals(1, recuperado.getInsignias().size());
    Assertions.assertEquals("insignia-orm-2", recuperado.getInsignias().get(0).getId());
    Assertions.assertTrue(recuperado.getCategorias().contains(CategoriaDonadorEnum.COLABORADOR));
  }

  @Test
  void testActualizarPerfil() {
    PerfilIncentivos perfil = new PerfilIncentivos("donador-orm-2");
    perfilRepository.save(perfil);

    PerfilIncentivos guardado = perfilRepository.findById("donador-orm-2").orElseThrow();
    guardado.setMisionActualID("mision-orm-1");
    perfilRepository.save(guardado);

    PerfilIncentivos actualizado = perfilRepository.findById("donador-orm-2").orElseThrow();

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
  void testBuscarPerfilInexistente() {
    Assertions.assertTrue(perfilRepository.findById("no-existe").isEmpty());
  }

  @Test
  void testListarTodosLosPerfiles() {
    perfilRepository.save(new PerfilIncentivos("donador-orm-3"));
    perfilRepository.save(new PerfilIncentivos("donador-orm-4"));

    List<PerfilIncentivos> perfiles = perfilRepository.findAll();

    Assertions.assertTrue(perfiles.size() >= 2);
  }
}
