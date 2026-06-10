package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.ProductoDTO;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Implementación de FachadaDonaciones que hace llamadas HTTP al módulo de Donaciones.
 * Se comunica con el servicio de Donaciones en Render o local según configuración.
 */
@Service
public class FachadaDonacionesImpl implements FachadaDonaciones {

  private final RestTemplate restTemplate;
  
  @Value("${donaciones.base-url:http://localhost:8081}")
  private String donacionesBaseUrl;

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaLogistica fachadaLogistica;

  @Autowired
  public FachadaDonacionesImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public DonacionDTO registrarDonacion(DonacionDTO donacionDTO) {
    String url = donacionesBaseUrl + "/donaciones";
    return restTemplate.postForObject(url, donacionDTO, DonacionDTO.class);
  }

  @Override
  public DonacionDTO buscarDonacionPorID(String donacionID) throws NoSuchElementException {
    String url = donacionesBaseUrl + "/donaciones/" + donacionID;
    try {
      return restTemplate.getForObject(url, DonacionDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Donación no encontrada con ID: " + donacionID);
    }
  }

  @Override
  public DonacionDTO cambiarEstadoDeDonacion(String donacionID, EstadoDonacionEnum estado)
      throws NoSuchElementException {
    // Este endpoint no está documentado en Donaciones, se deja como placeholder
    throw new UnsupportedOperationException("Cambiar estado de donación no está implementado en el cliente HTTP");
  }

  /**
   * Busca donaciones de un donador desde una fecha de inicio hasta la fecha actual.
   * Llamada según documentación: "Incentivos → Donaciones: Consulta las donaciones de un donador desde una fecha de inicio hasta la fecha actual"
   */
  @Override
  public List<DonacionDTO> buscarPorDonadorYFechaInicio(String donadorID, LocalDate fecha)
      throws NoSuchElementException {
    String url = UriComponentsBuilder.fromUriString(donacionesBaseUrl + "/donaciones")
        .queryParam("donadorID", donadorID)
        .queryParam("fecha", fecha)
        .toUriString();
    try {
      DonacionDTO[] donaciones = restTemplate.getForObject(url, DonacionDTO[].class);
      return donaciones != null ? Arrays.asList(donaciones) : List.of();
    } catch (Exception e) {
      throw new NoSuchElementException("No se encontraron donaciones para el donador: " + donadorID);
    }
  }

  @Override
  public DonacionDTO registrarQuejaEnDonacion(String donacionID, String descripcion) {
    // Este endpoint no está documentado en Donaciones, se deja como placeholder
    throw new UnsupportedOperationException("Registrar queja en donación no está implementado en el cliente HTTP");
  }

  @Override
  public ProductoDTO agregarProducto(ProductoDTO productoDTO) {
    String url = donacionesBaseUrl + "/productos";
    return restTemplate.postForObject(url, productoDTO, ProductoDTO.class);
  }

  @Override
  public ProductoDTO buscarProductoPorID(String productoID) throws NoSuchElementException {
    String url = donacionesBaseUrl + "/productos/" + productoID;
    try {
      return restTemplate.getForObject(url, ProductoDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Producto no encontrado con ID: " + productoID);
    }
  }

  @Override
  public IdentificadorDTO agregarIdentificador(IdentificadorDTO identificadorDTO) {
    String url = donacionesBaseUrl + "/identificadores";
    return restTemplate.postForObject(url, identificadorDTO, IdentificadorDTO.class);
  }

  @Override
  public IdentificadorDTO buscarIdentificadorPorID(String identificadorID)
      throws NoSuchElementException {
    String url = donacionesBaseUrl + "/identificadores/" + identificadorID;
    try {
      return restTemplate.getForObject(url, IdentificadorDTO.class);
    } catch (Exception e) {
      throw new NoSuchElementException("Identificador no encontrado con ID: " + identificadorID);
    }
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
  }

  @Override
  public void setFachadaLogistica(FachadaLogistica fachadaLogistica) {
    this.fachadaLogistica = fachadaLogistica;
  }
}
