package ar.edu.utn.dds.k3003;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorStatsDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.model.Donador;
import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.repositories.DonadorRepository;

public class Fachada_DonadoresEntidades implements FachadaDonadoresYEntidades  {

    private List<Donador> donadores;
    private DonadorRepository.RepoDonadores repoDonadores;


    public Fachada_DonadoresEntidades() {
        this.donadores = new ArrayList<>();
        this.repoDonadores = new DonadorRepository.RepoDonadores(null);
    }
    @Override
    public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
        
        return donadorDTO;
    }

    @Override
    public DonadorDTO buscarDonadorPorID(String donadorID) throws NoSuchElementException {
        Donador donador = repoDonadores.buscarDonadorPorID(donadorID);
        return new DonadorDTO(donador.getId(), donador.getNombre(), donador.getApellido(), donador.getEdad(), donador.getEmail(), donador.getNroDocumento(), donador.getDomicilio(), donador.getEstado(),donador.getCategoria());
    }

    @Override
    public EntidadBeneficaDTO agregarEntidad(EntidadBeneficaDTO entidadBeneficaDTO) {
       
        return entidadBeneficaDTO;
    }

    @Override
    public EntidadBeneficaDTO buscarEntidadPorID(String entidadID) throws NoSuchElementException {
        return null;
    }

    @Override
    public NecesidadMaterialDTO registrarNecesidad(NecesidadMaterialDTO necesidadMaterialDTO) {
        return null;
    }

    @Override
    public QuejaDTO agregarQueja(QuejaDTO quejaDTO) throws NoSuchElementException {
        return null;
    }

    @Override
    public Boolean puedeDonar(String donadorID) throws NoSuchElementException {
        return null;
    }

    @Override
    public List<QuejaDTO> obtenerQuejasDe(String donadorID) throws NoSuchElementException {
        return null;
    }

    @Override
    public DonadorDTO modificarEstado(String donadorID, EstadoDonadorEnum estado) throws NoSuchElementException {
        return null;
    }

    @Override
    public DonadorDTO modifcarCategoria(String donadorID, String categoria) throws NoSuchElementException {
        return null;
    }

    @Override
    public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoSolicitadoID) {
        return null;
    }

    @Override
    public NecesidadMaterialDTO satisfacerNecesidad(String necesidadID, Integer cantidad)
            throws NoSuchElementException {
        return null;
    }

    @Override
    public DonadorStatsDTO estadisticasDonador(String donadorID) {
        Donador donador = repoDonadores.buscarDonadorPorID(donadorID);
        return new DonadorStatsDTO(donador.getId(), donador.getNombre(), donador.getApellido(), donador.getEdad(), donador.getEstado(), donador.getCategoria(), donador.getMisionActualID(), null);
    }

    @Override
    public void setFachadaIncentivos(FachadaIncentivos fachadaIncentivos) {
        //this.fachadaIncentivos = fachadaIncentivos;
    }
    
    public void asignarMisionADonador(String donadorID, String misionDTOID) throws NoSuchElementException {
        if (misionDTOID == null) {
          throw new RuntimeException("Mision nula");
        }
        repoDonadores.buscarDonadorPorID(donadorID).setMisionActualID(misionDTOID);
      }

    
    
      public void asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO) throws NoSuchElementException {
        if (insigniaDTO == null) {
          throw new RuntimeException("Insignia nula");
        }
    
        this.buscarDonadorPorID(donadorID);
            Donador donador = repoDonadores.buscarDonadorPorID(donadorID);
            Insignia nuevaInsignia = new Insignia(insigniaDTO.id(), insigniaDTO.nombre(), insigniaDTO.descripcion());
            donador.agregarInsignia(nuevaInsignia);
        }
    }


