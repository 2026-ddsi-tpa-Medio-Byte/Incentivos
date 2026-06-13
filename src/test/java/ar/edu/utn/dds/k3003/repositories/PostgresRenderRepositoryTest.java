package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas del ORM (JPA/Hibernate) que se conectan a la base PostgreSQL real desplegada en
 * Render, usando las credenciales configuradas en {@code .env} (SPRING_DATASOURCE_URL, etc.).
 *
 * <p>Cada test corre dentro de una transacción (para poder acceder a colecciones lazy como
 * {@code insignias}) pero se confirma (commit) al finalizar en lugar de revertirse, para poder
 * verificar los datos con herramientas como DBeaver. Los IDs usados son fijos ("-render-") y
 * cada test usa {@code save()} sobre esos IDs, por lo que volver a ejecutarlos actualiza los
 * mismos registros en lugar de duplicarlos.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Commit
class PostgresRenderRepositoryTest {

  @Autowired private PerfilIncentivosRepository perfilRepository;
  @Autowired private InsigniaRepository insigniaRepository;
  @Autowired private MisionRepository misionRepository;

  @Test
  void testGuardarYBuscarInsigniaEnRender() {
    insigniaRepository.save(new Insignia("insignia-render-1", "Primer Paso", "Otorgada en la primera donación"));

    Insignia recuperada = insigniaRepository.findById("insignia-render-1").orElseThrow();

    Assertions.assertEquals("Primer Paso", recuperada.getNombre());
    Assertions.assertEquals("Otorgada en la primera donación", recuperada.getDescripcion());
  }

  @Test
  void testGuardarYBuscarMisionConEnumsEnRender() {
    Mision mision =
        new Mision(
            "mision-render-1",
            "Donar 5 veces",
            "insignia-render-1",
            CategoriaDonadorEnum.COLABORADOR,
            CategoriaDonadorEnum.SALVADOR,
            TipoMisionEnum.DONACIONES_EXITOSAS);

    misionRepository.save(mision);

    Mision recuperada = misionRepository.findById("mision-render-1").orElseThrow();

    Assertions.assertEquals(CategoriaDonadorEnum.COLABORADOR, recuperada.getCategoriaInicio());
    Assertions.assertEquals(CategoriaDonadorEnum.SALVADOR, recuperada.getCategoriaFin());
    Assertions.assertEquals(TipoMisionEnum.DONACIONES_EXITOSAS, recuperada.getTipo());
  }

  @Test
  void testGuardarPerfilConInsigniasYCategoriasEnRender() {
    Insignia insignia = insigniaRepository.save(new Insignia("insignia-render-2", "Veterano", "5+ donaciones"));

    PerfilIncentivos perfil = new PerfilIncentivos("donador-render-1");
    perfil.agregarInsignia(insignia);
    perfil.agregarCategoria(CategoriaDonadorEnum.COLABORADOR);

    perfilRepository.save(perfil);
    perfilRepository.flush();

    PerfilIncentivos recuperado = perfilRepository.findById("donador-render-1").orElseThrow();

    Assertions.assertEquals(1, recuperado.getInsignias().size());
    Assertions.assertEquals("insignia-render-2", recuperado.getInsignias().get(0).getId());
    Assertions.assertTrue(recuperado.getCategorias().contains(CategoriaDonadorEnum.COLABORADOR));
  }

  @Test
  void testActualizarPerfilEnRender() {
    PerfilIncentivos perfil = new PerfilIncentivos("donador-render-2");
    perfilRepository.save(perfil);

    PerfilIncentivos guardado = perfilRepository.findById("donador-render-2").orElseThrow();
    guardado.setMisionActualID("mision-render-1");
    perfilRepository.save(guardado);

    PerfilIncentivos actualizado = perfilRepository.findById("donador-render-2").orElseThrow();

    Assertions.assertEquals("mision-render-1", actualizado.getMisionActualID());
  }

  @Test
  void testBuscarPerfilInexistenteEnRender() {
    Assertions.assertTrue(perfilRepository.findById("no-existe-render").isEmpty());
  }
}
