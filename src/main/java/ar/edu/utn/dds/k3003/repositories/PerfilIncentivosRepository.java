package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface PerfilIncentivosRepository extends JpaRepository<PerfilIncentivos, String> {

  /**
   * Implementación en memoria del tracking de misión/insignias/categorías por donador,
   * usada cuando la Fachada no opera contra JPA (tests unitarios / facade in-memory).
   */
  class RepoPerfiles {
    private final Map<String, String> misionesPorDonador = new HashMap<>();
    private final Map<String, List<String>> insigniasPorDonador = new HashMap<>();
    private final Map<String, List<CategoriaDonadorEnum>> categoriasPorDonador = new HashMap<>();

    public Map<String, String> getMisionesPorDonador() {
      return misionesPorDonador;
    }

    public void asignarMisionADonador(String donadorID, String misionID) {
      misionesPorDonador.put(donadorID, misionID);
    }

    public Map<String, List<String>> getInsigniasPorDonador() {
      return insigniasPorDonador;
    }

    public void asignarInsigniaADonador(String donadorID, String insigniaID) {
      insigniasPorDonador.computeIfAbsent(donadorID, k -> new ArrayList<>()).add(insigniaID);
    }

    public Map<String, List<CategoriaDonadorEnum>> getCategoriasPorDonador() {
      return categoriasPorDonador;
    }

    public void agregarCategoriADonador(String donadorID, CategoriaDonadorEnum categoria) {
      categoriasPorDonador.computeIfAbsent(donadorID, k -> new ArrayList<>()).add(categoria);
    }
  }
}
