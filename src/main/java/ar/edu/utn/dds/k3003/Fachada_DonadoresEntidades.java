package ar.edu.utn.dds.k3003;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;

public class Fachada_DonadoresEntidades implements FachadaDonadoresYEntidades  {

    private final Map<String, DonadorDTO> donadores;
    private final PerfilIncentivosRepository.RepoPerfiles repoPerfiles;


    public Fachada_DonadoresEntidades() {
        this.donadores = new HashMap<>();
        this.repoPerfiles = new PerfilIncentivosRepository.RepoPerfiles();
    }
    @Override
    public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
        donadores.put(donadorDTO.id(), donadorDTO);
        return donadorDTO;
    }

    @Override
    public DonadorDTO buscarDonadorPorID(String donadorID) throws NoSuchElementException {
        DonadorDTO donador = donadores.get(donadorID);
        if (donador == null) {
            throw new NoSuchElementException("No se encontró el donador con ID: " + donadorID);
        }
        return donador;
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
        DonadorDTO donador = this.buscarDonadorPorID(donadorID);
        String misionActualID = repoPerfiles.getMisionesPorDonador().get(donadorID);
        List<String> insigniasID = repoPerfiles.getInsigniasPorDonador().getOrDefault(donadorID, new ArrayList<>());
        return new DonadorStatsDTO(donador.id(), donador.nombre(), donador.apellido(), donador.edad(), donador.estado(), donador.categoria(), misionActualID, insigniasID);
    }

    @Override
    public void setFachadaIncentivos(FachadaIncentivos fachadaIncentivos) {
        //this.fachadaIncentivos = fachadaIncentivos;
    }

    public void asignarMisionADonador(String donadorID, String misionDTOID) throws NoSuchElementException {
        if (misionDTOID == null) {
          throw new RuntimeException("Mision nula");
        }
        this.buscarDonadorPorID(donadorID);
        repoPerfiles.asignarMisionADonador(donadorID, misionDTOID);
      }



      public void asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO) throws NoSuchElementException {
        if (insigniaDTO == null) {
          throw new RuntimeException("Insignia nula");
        }

        this.buscarDonadorPorID(donadorID);
        repoPerfiles.asignarInsigniaADonador(donadorID, insigniaDTO.id());
        }
    }


