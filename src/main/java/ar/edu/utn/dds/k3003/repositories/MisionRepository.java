package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Mision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface MisionRepository extends JpaRepository<Mision, String> {

  class RepoMisiones {
    private List<Mision> misiones = new ArrayList<>();

    public Mision agregarMision(Mision mision){
      if (mision == null) {
        throw new RuntimeException("Mision nula");
      }

      String id = mision.getId();
      String nuevaId = id == null ? UUID.randomUUID().toString() : id;

      if (misiones.stream().anyMatch(m -> m.getId().equals(nuevaId))) {
        throw new RuntimeException("Mision ya existente");
      }

      Mision nuevaMision = new Mision(
        nuevaId,
        mision.getNombre(),
        mision.getInsigniaID(),
        mision.getCategoriaInicio(),
        mision.getCategoriaFin(),
        mision.getTipo());
      this.misiones.add(nuevaMision);
      return nuevaMision;
    }

    public Mision eliminarMision(Mision mision){
      this.misiones.remove(mision);
      return mision;
    }

    public Mision modificarMision(Mision mision){
      Mision misionAEliminar = misiones.stream().filter(i -> i.getId().equals(mision.getId())).findFirst().orElse(null);

      if(misionAEliminar != null){
        this.eliminarMision(misionAEliminar);
        this.agregarMision(mision);
      }
      return mision;
    }

    public Mision getMisionByID(String id){
      return misiones.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Mision> getMisiones() {
      return misiones;
    }

}
}
