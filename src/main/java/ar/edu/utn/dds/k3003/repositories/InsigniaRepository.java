package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Insignia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface InsigniaRepository extends JpaRepository<Insignia, String> {

  class RepoInsignias {
    private List<Insignia> insignias = new ArrayList<>();

    public List<Insignia> getInsignias() {
      return insignias;
    }

    public Insignia eliminarInsignia(Insignia insigniaDTO){
      this.insignias.remove(insigniaDTO);
      return insigniaDTO;
    }

    public Insignia modificarInsignia(Insignia insigniaDTO){
      Insignia insigniaAModificar = insignias.stream().filter(i -> i.getId().equals(insigniaDTO.getId())).findFirst().orElse(null);

      if(insigniaAModificar != null){
        this.eliminarInsignia(insigniaAModificar);
        this.agregarInsignia(insigniaDTO);
      }
      return insigniaDTO;
    }

    public Insignia agregarInsignia(Insignia insigniaDTO){
      if (insigniaDTO == null) {
        throw new RuntimeException("Insignia nula");
      }

      String id = insigniaDTO.getId();
      String nuevaId = id == null ? UUID.randomUUID().toString() : id;

      if (insignias.stream().anyMatch(i -> i.getId().equals(nuevaId))) {
        throw new RuntimeException("Insignia ya existente");
      }

      var nueva = new Insignia(nuevaId, insigniaDTO.getNombre(), insigniaDTO.getDescripcion());
      this.insignias.add(nueva);
      return nueva;
    }
