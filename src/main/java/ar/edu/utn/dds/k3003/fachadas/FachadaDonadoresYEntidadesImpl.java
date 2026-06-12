package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorStatsDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.QuejaDTO;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Implementación de FachadaDonadoresYEntidades que hace llamadas HTTP al módulo de
 * Donadores y Entidades. Se comunica con el servicio desplegado en Render o local
 * según configuración.
 */
@Service
public class FachadaDonadoresYEntidadesImpl implements FachadaDonadoresYEntidades {

  private final RestTemplate restTemplate;

  @Value("${donadoresyentidades.base-url:http://localhost:8083}")
  private String donadoresYEntidadesBaseUrl;

  private FachadaIncentivos fachadaIncentivos;

  @Autowired
  public FachadaDonadoresYEntidadesImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
    String url = donadoresYEntidadesBaseUrl + "/donadores";
    return restTemplate.postForObject(url, donadorDTO, DonadorDTO.class);
  }

  @Override
  public DonadorDTO buscarDonadorPorID(String donadorID) throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/donadores/" + donadorID;
    try {
      return restTemplate.getForObject(url, DonadorDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Donador no encontrado con ID: " + donadorID);
    }
  }

  @Override
  public EntidadBeneficaDTO agregarEntidad(EntidadBeneficaDTO entidadBeneficaDTO) {
    String url = donadoresYEntidadesBaseUrl + "/entidades";
    return restTemplate.postForObject(url, entidadBeneficaDTO, EntidadBeneficaDTO.class);
  }

  @Override
  public EntidadBeneficaDTO buscarEntidadPorID(String entidadID) throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/entidades/" + entidadID;
    try {
      return restTemplate.getForObject(url, EntidadBeneficaDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Entidad no encontrada con ID: " + entidadID);
    }
  }

  @Override
  public NecesidadMaterialDTO registrarNecesidad(NecesidadMaterialDTO necesidadMaterialDTO) {
    String url = donadoresYEntidadesBaseUrl + "/necesidades";
    return restTemplate.postForObject(url, necesidadMaterialDTO, NecesidadMaterialDTO.class);
  }

  @Override
  public QuejaDTO agregarQueja(QuejaDTO quejaDTO) throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/donadores/" + quejaDTO.donadorID() + "/quejas";
    try {
      return restTemplate.postForObject(url, quejaDTO, QuejaDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Donador no encontrado con ID: " + quejaDTO.donadorID());
    }
  }

  @Override
  public Boolean puedeDonar(String donadorID) throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/donadores/" + donadorID + "/puede-donar";
    try {
      Map<?, ?> respuesta = restTemplate.getForObject(url, Map.class);
      return respuesta != null && Boolean.TRUE.equals(respuesta.get("puedeDonar"));
    } catch (Exception e) {
      throw new NoSuchElementException("Donador no encontrado con ID: " + donadorID);
    }
  }

  @Override
  public List<QuejaDTO> obtenerQuejasDe(String donadorID) throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/donadores/" + donadorID + "/quejas";
    try {
      QuejaDTO[] quejas = restTemplate.getForObject(url, QuejaDTO[].class);
      return quejas != null ? Arrays.asList(quejas) : List.of();
    } catch (Exception e) {
      throw new NoSuchElementException("Donador no encontrado con ID: " + donadorID);
    }
  }

  @Override
  public DonadorDTO modificarEstado(String donadorID, EstadoDonadorEnum estado)
      throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/donadores/" + donadorID + "/estado";
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("estado", estado);
    try {
      return restTemplate.patchForObject(url, body, DonadorDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Donador no encontrado con ID: " + donadorID);
    }
  }

  @Override
  public DonadorDTO modifcarCategoria(String donadorID, String categoria)
      throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/donadores/" + donadorID + "/categoria";
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("categoria", categoria);
    try {
      return restTemplate.patchForObject(url, body, DonadorDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Donador no encontrado con ID: " + donadorID);
    }
  }

  @Override
  public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoSolicitadoID) {
    String url =
        UriComponentsBuilder.fromUriString(donadoresYEntidadesBaseUrl + "/necesidades")
            .queryParam("productoID", productoSolicitadoID)
            .toUriString();
    NecesidadMaterialDTO[] necesidades = restTemplate.getForObject(url, NecesidadMaterialDTO[].class);
    return necesidades != null ? Arrays.asList(necesidades) : List.of();
  }

  @Override
  public NecesidadMaterialDTO satisfacerNecesidad(String necesidadID, Integer cantidad)
      throws NoSuchElementException {
    String url = donadoresYEntidadesBaseUrl + "/necesidades/" + necesidadID + "/satisfaccion";
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("cantidad", cantidad);
    try {
      return restTemplate.postForObject(url, body, NecesidadMaterialDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Necesidad no encontrada con ID: " + necesidadID);
    }
  }

  @Override
  public DonadorStatsDTO estadisticasDonador(String donadorID) {
    String url = donadoresYEntidadesBaseUrl + "/donadores/" + donadorID + "/estadisticas";
    return restTemplate.getForObject(url, DonadorStatsDTO.class);
  }

  @Override
  public void setFachadaIncentivos(FachadaIncentivos fachadaIncentivos) {
    this.fachadaIncentivos = fachadaIncentivos;
  }
}
