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
import ar.edu.utn.dds.k3003.repositories.DonadorRepository;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import ar.edu.utn.dds.k3003.repositories.DonadorRepository;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;




@Service
public class Fachada implements FachadaIncentivos {

  private MisionRepository.RepoMisiones repoMisiones;
  private InsigniaRepository.RepoInsignias repoInsignias;
  private DonadorRepository.RepoDonadores repoDonadores;
  private DonadorRepository donadorJpaRepository;
  private InsigniaRepository insigniaJpaRepository;
  private MisionRepository misionJpaRepository;
  private boolean useJpa = false;
  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;

  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    this.fachadaDonaciones = fachadaDonaciones;
  }

  public List<InsigniaDTO> getAllInsignias() {
    if (useJpa) {
      return insigniaJpaRepository.findAll().stream()
          .map(i -> new InsigniaDTO(i.getId(), i.getNombre(), i.getDescripcion()))
          .toList();
    }
    return repoInsignias.getInsignias().stream()
        .map(i -> new InsigniaDTO(i.getId(), i.getNombre(), i.getDescripcion()))
        .toList();
  }

  public InsigniaDTO getInsigniaById(String id) {
    Insignia i = null;
    if (useJpa) {
      i = insigniaJpaRepository.findById(id).orElse(null);
    } else {
      i = repoInsignias.getInsignias().stream().filter(ins -> ins.getId().equals(id)).findFirst().orElse(null);
    }
    if (i == null) {
      throw new NoSuchElementException("Insignia no encontrada");
    }
    return new InsigniaDTO(i.getId(), i.getNombre(), i.getDescripcion());
  }

  public List<MisionDTO> getAllMisiones() {
    if (useJpa) {
      return misionJpaRepository.findAll().stream()
          .map(m -> new MisionDTO(m.getId(), m.getNombre(), m.getInsigniaID(), m.getCategoriaInicio(), m.getCategoriaFin(), m.getTipo()))
          .toList();
    }
    return repoMisiones.getMisiones().stream()
        .map(m -> new MisionDTO(m.getId(), m.getNombre(), m.getInsigniaID(), m.getCategoriaInicio(), m.getCategoriaFin(), m.getTipo()))
        .toList();
  }

  public MisionDTO getMisionById(String id) {
    Mision m = null;
    if (useJpa) {
      m = misionJpaRepository.findById(id).orElse(null);
    } else {
      m = repoMisiones.getMisionByID(id);
    }
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
    this.repoMisiones = new MisionRepository.RepoMisiones();
    this.repoInsignias = new InsigniaRepository.RepoInsignias();
    this.repoDonadores = new DonadorRepository.RepoDonadores(new ArrayList<>());
  }

  // Constructor for Spring to inject JPA repositories (will set useJpa=true)
  public Fachada(DonadorRepository donadorJpaRepository, InsigniaRepository insigniaJpaRepository, MisionRepository misionJpaRepository) {
    this(); // initialize fallbacks
    this.donadorJpaRepository = donadorJpaRepository;
    this.insigniaJpaRepository = insigniaJpaRepository;
    this.misionJpaRepository = misionJpaRepository;
    this.useJpa = true;
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
    Insignia agregada;
    if (useJpa) {
      agregada = insigniaJpaRepository.save(entidad);
    } else {
      agregada = repoInsignias.agregarInsignia(entidad);
    }
    return new InsigniaDTO(agregada.getId(), agregada.getNombre(), agregada.getDescripcion());
  }

  @Override
  public MisionDTO agregarMision(MisionDTO misionDTO){
    Mision entidad = new Mision(misionDTO.id(), misionDTO.nombre(), misionDTO.insigniaID(), misionDTO.categoriaInicio(), misionDTO.categoriaFin(), misionDTO.tipo());
    Mision agregada;
    if (useJpa) {
      agregada = misionJpaRepository.save(entidad);
    } else {
      agregada = repoMisiones.agregarMision(entidad);
    }
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
    if (useJpa) {
      var donadorOpt = donadorJpaRepository.findById(donadorID);
      if (donadorOpt.isEmpty()) throw new RuntimeException("Donador no encontrado");
      var insignias = donadorOpt.get().getInsignias();
      if (insignias == null || insignias.isEmpty()) throw new NoSuchElementException("No hay insignias para el donador " + donadorID);
      return insignias.stream().map(insignia -> new InsigniaDTO(insignia.getId(), insignia.getNombre(), insignia.getDescripcion())).toList();
    }

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
    if (useJpa) {
      var donadorOpt = donadorJpaRepository.findById(donadorID);
      if (donadorOpt.isEmpty()) throw new RuntimeException("Donador no encontrado");
      String misionID = donadorOpt.get().getMisionActualID();
      if (misionID == null) throw new NoSuchElementException("No hay misión en curso para el donador " + donadorID);
      var misionOpt = misionJpaRepository.findById(misionID);
      if (misionOpt.isEmpty()) throw new NoSuchElementException("Misión no encontrada");
      Mision mision = misionOpt.get();
      return new MisionDTO(mision.getId(), mision.getNombre(), mision.getInsigniaID(), mision.getCategoriaInicio(), mision.getCategoriaFin(), mision.getTipo());
    }

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
    if (useJpa) {
      var all = donadorJpaRepository.findAll();
      var map = new java.util.HashMap<String, List<CategoriaDonadorEnum>>();
      for (var d : all) map.put(d.getId(), d.getCategorias());
      return map;
    }
    return repoDonadores.getCategoriasPorDonador();
  }

  public List<CategoriaDonadorEnum> getCategoriasDonador(String donadorID) {
    if (useJpa) {
      return donadorJpaRepository.findById(donadorID).map(d -> d.getCategorias()).orElse(new ArrayList<>());
    }
    return repoDonadores.getCategoriasPorDonador().getOrDefault(donadorID, new ArrayList<>());
  }

  public Map<String, String> getMisionDonadores() {
    if (useJpa) {
      var all = donadorJpaRepository.findAll();
      var map = new java.util.HashMap<String, String>();
      for (var d : all) map.put(d.getId(), d.getMisionActualID());
      return map;
    }
    return repoDonadores.getMisionesPorDonador();
  }

  public Map<String, List<String>> getInsigniasDonadores() {
    if (useJpa) {
      var all = donadorJpaRepository.findAll();
      var map = new java.util.HashMap<String, List<String>>();
      for (var d : all) map.put(d.getId(), d.getInsignias().stream().map(i -> i.getId()).toList());
      return map;
    }
    return repoDonadores.getInsigniasPorDonador();
  }

  @Override
  public void asignarMisionADonador(String donadorID, MisionDTO misionDTO) throws NoSuchElementException {
    if (misionDTO == null) {
      throw new RuntimeException("Mision nula");
    }
    fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    if (useJpa) {
      var donador = donadorJpaRepository.findById(donadorID).orElseThrow(() -> new RuntimeException("Donador no encontrado"));
      var misionOpt = misionJpaRepository.findById(misionDTO.id());
      Mision mision = misionOpt.orElseGet(() -> misionJpaRepository.save(new Mision(misionDTO.id(), misionDTO.nombre(), misionDTO.insigniaID(), misionDTO.categoriaInicio(), misionDTO.categoriaFin(), misionDTO.tipo())));
      donador.setMisionActualID(mision.getId());
      donadorJpaRepository.save(donador);
      return;
    }
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
    if (useJpa) {
      // ensure insignia exists
      var insigniaOpt = insigniaJpaRepository.findById(insigniaDTO.id());
      Insignia insignia = insigniaOpt.orElseGet(() -> insigniaJpaRepository.save(new Insignia(insigniaDTO.id(), insigniaDTO.nombre(), insigniaDTO.descripcion())));
      // add to donador
      var donador = donadorJpaRepository.findById(donadorID).orElseThrow(() -> new RuntimeException("Donador no encontrado"));
      if (donador.getInsignias() == null) donador.setInsignias(new java.util.ArrayList<>());
      donador.getInsignias().add(insignia);
      donadorJpaRepository.save(donador);
      return;
    }
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
          if (useJpa) {
            var donador = donadorJpaRepository.findById(donadorID).orElseThrow(() -> new RuntimeException("Donador no encontrado"));
            donador.agregarCategoria(misionActual.categoriaFin());
            donadorJpaRepository.save(donador);
          } else {
            repoDonadores.agregarCategoriADonador(donadorID, misionActual.categoriaFin());
          }
        }
      }
    }
  }





}
