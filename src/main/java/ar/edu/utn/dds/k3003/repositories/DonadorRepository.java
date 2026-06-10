package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.model.Donador;
import lombok.val;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public interface DonadorRepository extends JpaRepository<Donador, String> {

  interface DonadoresRepository {
    Optional<Donador> findById(String id);

    Donador save(Donador donador);

    Donador deleteById(String id);
  }

  class InMemoryDonadoresRepo implements DonadoresRepository {

    private List<Donador> donadores;
    private AtomicLong idSecuencial = new AtomicLong(1);

    public InMemoryDonadoresRepo() {
      this.donadores = new ArrayList<>();
    }

    @Override
    public Optional<Donador> findById(String id) {
      return this.donadores.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    @Override
    public Donador save(Donador donador) {
      Donador donadorConID = donador;
      donadorConID.setId(String.valueOf(idSecuencial.getAndIncrement()));

      this.donadores.add(donadorConID);
      return this.findById(donadorConID.getId()).get();
    }

    @Override
    public Donador deleteById(String id) {
      val donador = this.findById(id);
      this.donadores.remove(donador.get());
      return donador.get();
    }
  }

  class RepoDonadores {
    private List<Donador> donadores;
    private Map<String, String> misionesPorDonador = new HashMap<>();
    private Map<String, List<String>> insigniasPorDonador = new HashMap<>();
    private Map<String, List<CategoriaDonadorEnum>> categoriasPorDonador = new HashMap<>();

    public RepoDonadores(List<Donador> donadores) {
      this.donadores = donadores;
    }

    public Donador agregarDonador(Donador donador) {
      this.donadores.add(donador);
      return donador;
    }

    public Donador buscarDonadorPorID(String donadorID) {
      return this.donadores.stream().filter(d -> d.getId().equals(donadorID)).findFirst().orElseThrow(() -> new RuntimeException("No se encontró el donador con ID: " + donadorID));
    }

    public List<Donador> getDonadores() {
      return donadores;
    }

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
