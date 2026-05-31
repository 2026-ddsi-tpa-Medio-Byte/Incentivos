package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;


import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import org.springframework.stereotype.Service;
import ar.edu.utn.dds.k3003.repositories.RepoDonadores;
import ar.edu.utn.dds.k3003.repositories.RepoInsignias;
import ar.edu.utn.dds.k3003.repositories.RepoMisiones;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;




@Service
public class Fachada implements FachadaIncentivos {

  private RepoMisiones repoMisiones;
  private RepoInsignias repoInsignias;
  private RepoDonadores repoDonadores;
  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;

  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    this.fachadaDonaciones = fachadaDonaciones;
  }

  public List<InsigniaDTO> getAllInsignias() {
    return repoInsignias.getInsignias().stream()
        .map(i -> new InsigniaDTO(i.getId(), i.getNombre(), i.getDescripcion()))
        .toList();
  }

  public InsigniaDTO getInsigniaById(String id) {
    Insignia i = repoInsignias.getInsignias().stream().filter(ins -> ins.getId().equals(id)).findFirst().orElse(null);
    if (i == null) {
      throw new NoSuchElementException("Insignia no encontrada");
    }
    return new InsigniaDTO(i.getId(), i.getNombre(), i.getDescripcion());
  }

  public List<MisionDTO> getAllMisiones() {
    return repoMisiones.getMisiones().stream()
        .map(m -> new MisionDTO(m.getId(), m.getNombre(), m.getInsigniaID(), m.getCategoriaInicio(), m.getCategoriaFin(), m.getTipo()))
        .toList();
  }

  public MisionDTO getMisionById(String id) {
    Mision m = repoMisiones.getMisionByID(id);
    if (m == null) {
      throw new NoSuchElementException("Misión no encontrada");
    }
    return new MisionDTO(m.getId(), m.getNombre(), m.getInsigniaID(), m.getCategoriaInicio(), m.getCategoriaFin(), m.getTipo());
  }


  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
  }

  public Fachada() {
    /*
    Para que se ejecuten correctamente los tests, se necesita tener un constructor vacio
    Es decir, que no reciba parametros.
    Si necesitan un constructor con parametros
    Java permite tener varios constructores conviviendo sin conflictos.
    */
    this.fachadaDonadoresYEntidades = new Fachada_DonadoresEntidades();
    this.repoMisiones = new RepoMisiones();
    this.repoInsignias = new RepoInsignias();
    this.repoDonadores = new RepoDonadores(new ArrayList<>());
  }
  public Insignia eliminarInsignia(Insignia insignia){
    return repoInsignias.eliminarInsignia(insignia);
  }

  public Insignia modificarInsignia(Insignia insignia){
    return repoInsignias.modificarInsignia(insignia);
  }
  @Override
  public InsigniaDTO agregarInsignia(InsigniaDTO insigniaDTO){
    Insignia entidad = new Insignia(insigniaDTO.id(), insigniaDTO.nombre(), insigniaDTO.descripcion());
    Insignia agregada = repoInsignias.agregarInsignia(entidad);
    return new InsigniaDTO(agregada.getId(), agregada.getNombre(), agregada.getDescripcion());
  }

  @Override
  public MisionDTO agregarMision(MisionDTO misionDTO){
    Mision entidad = new Mision(misionDTO.id(), misionDTO.nombre(), misionDTO.insigniaID(), misionDTO.categoriaInicio(), misionDTO.categoriaFin(), misionDTO.tipo());
    Mision agregada = repoMisiones.agregarMision(entidad);
    return new MisionDTO(agregada.getId(), agregada.getNombre(), agregada.getInsigniaID(), agregada.getCategoriaInicio(), agregada.getCategoriaFin(), agregada.getTipo());
  }

  public Mision eliminarMision(Mision mision){
    return repoMisiones.eliminarMision(mision);
  }

  public Mision modificarMision(Mision mision){
    return repoMisiones.modificarMision(mision);
  }  
/* Metodos a reimplementar */

  @Override
  public List<InsigniaDTO> getInsigniasDeDonador(String donadorID) throws NoSuchElementException {
    if (!repoDonadores.getInsigniasPorDonador().containsKey(donadorID)) {
      var donador = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (donador == null) {
        throw new RuntimeException("Donador no encontrado");
      }
    }
    List<String> insigniasIDs = repoDonadores.getInsigniasPorDonador().get(donadorID);
    if (insigniasIDs == null || insigniasIDs.isEmpty()) {
      throw new NoSuchElementException("No hay insignias para el donador " + donadorID);
    }
    List<Insignia> insignias = repoInsignias.getInsignias().stream().filter(insignia -> insigniasIDs.contains(insignia.getId())).toList();

    return insignias.stream().map(insignia -> new InsigniaDTO(insignia.getId(), insignia.getNombre(), insignia.getDescripcion())).toList();
  }

  @Override
  public MisionDTO getMisionEnCursoDeDonador(String donadorID) {
    if (!repoDonadores.getMisionesPorDonador().containsKey(donadorID)) {
      var donador = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (donador == null) {
        throw new RuntimeException("Donador no encontrado");
      }
      throw new NoSuchElementException("No hay misión en curso para el donador " + donadorID);
    }
    String misionID = repoDonadores.getMisionesPorDonador().get(donadorID);
    if (misionID == null) {
      throw new NoSuchElementException("No hay misión en curso para el donador " + donadorID);
    }
    Mision mision = repoMisiones.getMisionByID(misionID);
    if (mision == null) {
      throw new NoSuchElementException("Misión no encontrada");
    }
    return new MisionDTO(mision.getId(), mision.getNombre(), mision.getInsigniaID(), mision.getCategoriaInicio(), mision.getCategoriaFin(), mision.getTipo());
  }

  public CategoriaDonadorEnum getCategoriaActualDeDonador(String donadorID) {
    MisionDTO misionActual = this.getMisionEnCursoDeDonador(donadorID);
    if (misionActual != null) {
      return misionActual.categoriaInicio();
    }
    return null;
  }

  public Map<String, List<CategoriaDonadorEnum>> getCategoriaDonadores() {
    return repoDonadores.getCategoriasPorDonador();
  }

  public List<CategoriaDonadorEnum> getCategoriasDonador(String donadorID) {
    return repoDonadores.getCategoriasPorDonador().getOrDefault(donadorID, new ArrayList<>());
  }

  public Map<String, String> getMisionDonadores() {
    return repoDonadores.getMisionesPorDonador();
  }

  public Map<String, List<String>> getInsigniasDonadores() {
    return repoDonadores.getInsigniasPorDonador();
  }

  @Override
  public void asignarMisionADonador(String donadorID, MisionDTO misionDTO) throws NoSuchElementException {
    if (misionDTO == null) {
      throw new RuntimeException("Mision nula");
    }
    fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    repoDonadores.asignarMisionADonador(donadorID, misionDTO.id());
    if (repoMisiones.getMisionByID(misionDTO.id()) == null) {
      repoMisiones.agregarMision(new Mision(misionDTO.id(), misionDTO.nombre(), misionDTO.insigniaID(), misionDTO.categoriaInicio(), misionDTO.categoriaFin(), misionDTO.tipo()));
    }
  }

  @Override
  public void asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO) throws NoSuchElementException {
    if (insigniaDTO == null) {
      throw new RuntimeException("Insignia nula");
    }
    fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    if (repoInsignias.getInsignias().stream().noneMatch(i -> i.getId().equals(insigniaDTO.id()))) {
      repoInsignias.agregarInsignia(new Insignia(insigniaDTO.id(), insigniaDTO.nombre(), insigniaDTO.descripcion()));
    }
    repoDonadores.asignarInsigniaADonador(donadorID, insigniaDTO.id());
  }

  @Override
  public void procesarDonador(String donadorID) throws NoSuchElementException {
    fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    DonacionDTO donacionAProcesar = fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, null).getFirst();
    if (donacionAProcesar.estado().equals(EstadoDonacionEnum.ACEPTADA)) { // Donacion OK
      MisionDTO misionActual = this.getMisionEnCursoDeDonador(donadorID);

      if (misionActual != null) {
        if (misionActual.insigniaID() != null) {
          Insignia insigniaDeMision = this.repoInsignias.getInsignias().stream().filter(i -> i.getId().equals(misionActual.insigniaID())).findFirst().orElse(null);
          if (insigniaDeMision != null) {
            InsigniaDTO insigniaDTO = new InsigniaDTO(insigniaDeMision.getId(), insigniaDeMision.getNombre(), insigniaDeMision.getDescripcion());
            this.asignarInsigniaADonador(donadorID, insigniaDTO);
          }
        }
        
        if (misionActual.categoriaFin() != null) {
          fachadaDonadoresYEntidades.modifcarCategoria(donadorID, misionActual.categoriaFin().toString());
          // Persistir la categoría en el repositorio de donadores
          repoDonadores.agregarCategoriADonador(donadorID, misionActual.categoriaFin());
        }
      }
    }
  }





}
