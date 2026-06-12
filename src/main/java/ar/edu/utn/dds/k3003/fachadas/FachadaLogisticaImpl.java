package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.dtos.logistica.TipoAlgoritmoEnum;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Implementación de FachadaLogistica que hace llamadas HTTP al módulo de Logística.
 * Este módulo no se implementa en este proyecto: solo se realizan consultas a su
 * servicio en Render (o local según configuración).
 */
@Service
public class FachadaLogisticaImpl implements FachadaLogistica {

  private final RestTemplate restTemplate;

  @Value("${logistica.base-url:http://localhost:8082}")
  private String logisticaBaseUrl;

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;

  @Autowired
  public FachadaLogisticaImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public DepositoDTO agregarDeposito(DepositoDTO deposito) {
    String url = logisticaBaseUrl + "/api/depositos";
    return restTemplate.postForObject(url, deposito, DepositoDTO.class);
  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {
    String url = logisticaBaseUrl + "/api/depositos/" + depositoID;
    try {
      return restTemplate.getForObject(url, DepositoDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Depósito no encontrado con ID: " + depositoID);
    }
  }

  @Override
  public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID)
      throws NoSuchElementException {
    String url = logisticaBaseUrl + "/api/asignaciones/paquetes/" + paqueteID;
    try {
      return restTemplate.getForObject(url, AsignacionDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("No se encontró asignación para el paquete: " + paqueteID);
    }
  }

  @Override
  public DepositoDTO gestionarDonacion(
      String depositoID, String donacionID, String productoID, Integer cantidad)
      throws NoSuchElementException {
    String url =
        UriComponentsBuilder.fromUriString(logisticaBaseUrl + "/api/depositos/gestionar-donacion")
            .queryParam("depositoid", depositoID)
            .queryParam("donacionid", donacionID)
            .queryParam("productoid", productoID)
            .queryParam("cantidad", cantidad)
            .toUriString();
    try {
      return restTemplate.postForObject(url, null, DepositoDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Depósito no encontrado con ID: " + depositoID);
    }
  }

  @Override
  public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
    String url =
        UriComponentsBuilder.fromUriString(
                logisticaBaseUrl + "/api/depositos/" + depositoID + "/algoritmo")
            .queryParam("algoritmo", tipoAlgoritmo)
            .toUriString();
    restTemplate.put(url, null);
  }

  @Override
  public AsignacionDTO ejecutarMatchmaking(
      String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
    // No expuesto actualmente por el módulo de Logística
    throw new UnsupportedOperationException(
        "Ejecutar matchmaking no está implementado en el módulo de Logística");
  }

  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {
    String url = logisticaBaseUrl + "/api/asignaciones/reportar-entrega";
    restTemplate.postForObject(url, paqueteDTO, String.class);
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
  }

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    this.fachadaDonaciones = fachadaDonaciones;
  }
}
