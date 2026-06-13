package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.services.MisionEvaluatorService;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
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
  private PerfilIncentivosRepository.RepoPerfiles repoPerfiles;
  private PerfilIncentivosRepository perfilJpaRepository;
  private InsigniaRepository insigniaJpaRepository;
  private MisionRepository misionJpaRepository;
  private boolean useJpa = false;
  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;
  private MisionEvaluatorService misionEvaluatorService;

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
    this.repoPerfiles = new PerfilIncentivosRepository.RepoPerfiles();
  }

  // Constructor for Spring to inject JPA repositories (will set useJpa=true)
  @Autowired
  public Fachada(PerfilIncentivosRepository perfilJpaRepository, InsigniaRepository insigniaJpaRepository, MisionRepository misionJpaRepository, FachadaDonaciones fachadaDonaciones, MisionEvaluatorService misionEvaluatorService) {
    this(); // initialize fallbacks
    this.perfilJpaRepository = perfilJpaRepository;
    this.insigniaJpaRepository = insigniaJpaRepository;
    this.misionJpaRepository = misionJpaRepository;
    this.fachadaDonaciones = fachadaDonaciones;
    this.misionEvaluatorService = misionEvaluatorService;
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
      var perfilOpt = perfilJpaRepository.findById(donadorID);
      var insignias = perfilOpt.map(PerfilIncentivos::getInsignias).orElse(null);
      if (insignias == null || insignias.isEmpty()) throw new NoSuchElementException("No hay insignias para el donador " + donadorID);
      return insignias.stream().map(insignia -> new InsigniaDTO(insignia.getId(), insignia.getNombre(), insignia.getDescripcion())).toList();
    }

    if (!repoPerfiles.getInsigniasPorDonador().containsKey(donadorID)) {
      var donador = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (donador == null) {
        throw new RuntimeException("Donador no encontrado");
      }
    }
    List<String> insigniasIDs = repoPerfiles.getInsigniasPorDonador().get(donadorID);
    if (insigniasIDs == null || insigniasIDs.isEmpty()) {
      throw new NoSuchElementException("No hay insignias para el donador " + donadorID);
    }
    List<Insignia> insignias = repoInsignias.getInsignias().stream().filter(insignia -> insigniasIDs.contains(insignia.getId())).toList();

    return insignias.stream().map(insignia -> new InsigniaDTO(insignia.getId(), insignia.getNombre(), insignia.getDescripcion())).toList();
  }

  @Override
  public MisionDTO getMisionEnCursoDeDonador(String donadorID) {
    if (useJpa) {
      var perfilOpt = perfilJpaRepository.findById(donadorID);
      String misionID = perfilOpt.map(PerfilIncentivos::getMisionActualID).orElse(null);
      if (misionID == null) throw new NoSuchElementException("No hay misión en curso para el donador " + donadorID);
      var misionOpt = misionJpaRepository.findById(misionID);
      if (misionOpt.isEmpty()) throw new NoSuchElementException("Misión no encontrada");
      Mision mision = misionOpt.get();
      return new MisionDTO(mision.getId(), mision.getNombre(), mision.getInsigniaID(), mision.getCategoriaInicio(), mision.getCategoriaFin(), mision.getTipo());
    }

    if (!repoPerfiles.getMisionesPorDonador().containsKey(donadorID)) {
      var donador = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (donador == null) {
        throw new RuntimeException("Donador no encontrado");
      }
      throw new NoSuchElementException("No hay misión en curso para el donador " + donadorID);
    }
    String misionID = repoPerfiles.getMisionesPorDonador().get(donadorID);
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
      var all = perfilJpaRepository.findAll();
      var map = new java.util.HashMap<String, List<CategoriaDonadorEnum>>();
      for (var p : all) map.put(p.getId(), p.getCategorias());
      return map;
    }
    return repoPerfiles.getCategoriasPorDonador();
  }

  public List<CategoriaDonadorEnum> getCategoriasDonador(String donadorID) {
    if (useJpa) {
      return perfilJpaRepository.findById(donadorID).map(PerfilIncentivos::getCategorias).orElse(new ArrayList<>());
    }
    return repoPerfiles.getCategoriasPorDonador().getOrDefault(donadorID, new ArrayList<>());
  }

  public Map<String, String> getMisionDonadores() {
    if (useJpa) {
      var all = perfilJpaRepository.findAll();
      var map = new java.util.HashMap<String, String>();
      for (var p : all) map.put(p.getId(), p.getMisionActualID());
      return map;
    }
    return repoPerfiles.getMisionesPorDonador();
  }

  public Map<String, List<String>> getInsigniasDonadores() {
    if (useJpa) {
      var all = perfilJpaRepository.findAll();
      var map = new java.util.HashMap<String, List<String>>();
      for (var p : all) map.put(p.getId(), p.getInsignias().stream().map(Insignia::getId).toList());
      return map;
    }
    return repoPerfiles.getInsigniasPorDonador();
  }

  @Override
  public void asignarMisionADonador(String donadorID, MisionDTO misionDTO) throws NoSuchElementException {
    if (misionDTO == null) {
      throw new RuntimeException("Mision nula");
    }
    fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    if (useJpa) {
      var perfil = perfilJpaRepository.findById(donadorID).orElseGet(() -> new PerfilIncentivos(donadorID));
      var misionOpt = misionJpaRepository.findById(misionDTO.id());
      Mision mision = misionOpt.orElseGet(() -> misionJpaRepository.save(new Mision(misionDTO.id(), misionDTO.nombre(), misionDTO.insigniaID(), misionDTO.categoriaInicio(), misionDTO.categoriaFin(), misionDTO.tipo())));
      perfil.setMisionActualID(mision.getId());
      perfilJpaRepository.save(perfil);
      return;
    }
    repoPerfiles.asignarMisionADonador(donadorID, misionDTO.id());
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
      // add to perfil de incentivos del donador
      var perfil = perfilJpaRepository.findById(donadorID).orElseGet(() -> new PerfilIncentivos(donadorID));
      if (perfil.getInsignias() == null) perfil.setInsignias(new java.util.ArrayList<>());
      perfil.getInsignias().add(insignia);
      perfilJpaRepository.save(perfil);
      return;
    }
    if (repoInsignias.getInsignias().stream().noneMatch(i -> i.getId().equals(insigniaDTO.id()))) {
      repoInsignias.agregarInsignia(new Insignia(insigniaDTO.id(), insigniaDTO.nombre(), insigniaDTO.descripcion()));
    }
    repoPerfiles.asignarInsigniaADonador(donadorID, insigniaDTO.id());
  }

  @Override
  public void procesarDonador(String donadorID) throws NoSuchElementException {
    fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    DonacionDTO donacionAProcesar = fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, null).getFirst();
    if (donacionAProcesar.estado().equals(EstadoDonacionEnum.ACEPTADA)) { // Donacion OK
      MisionDTO misionActual = this.getMisionEnCursoDeDonador(donadorID);

      if (misionActual != null) {
        // Evaluar si el donador cumple con la misión según su tipo
        boolean cumpleMision = misionEvaluatorService.evaluarMision(donadorID, misionActual.tipo());

        if (cumpleMision) {
          if (misionActual.insigniaID() != null) {
            Insignia insigniaDeMision = this.repoInsignias.getInsignias().stream().filter(i -> i.getId().equals(misionActual.insigniaID())).findFirst().orElse(null);
            if (insigniaDeMision != null) {
              InsigniaDTO insigniaDTO = new InsigniaDTO(insigniaDeMision.getId(), insigniaDeMision.getNombre(), insigniaDeMision.getDescripcion());
              this.asignarInsigniaADonador(donadorID, insigniaDTO);
            }
          }

          if (misionActual.categoriaFin() != null) {
            fachadaDonadoresYEntidades.modifcarCategoria(donadorID, misionActual.categoriaFin().toString());
            // Persistir la categoría en el perfil de incentivos del donador
            if (useJpa) {
              var perfil = perfilJpaRepository.findById(donadorID).orElseGet(() -> new PerfilIncentivos(donadorID));
              perfil.agregarCategoria(misionActual.categoriaFin());
              perfilJpaRepository.save(perfil);
            } else {
              repoPerfiles.agregarCategoriADonador(donadorID, misionActual.categoriaFin());
            }
          }
        }
      }
    }
  }





}
