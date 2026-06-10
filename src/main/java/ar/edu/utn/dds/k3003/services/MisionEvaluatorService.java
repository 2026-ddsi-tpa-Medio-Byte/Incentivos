package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonaciones;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio para evaluar si un donador cumple con los requisitos de una misión.
 * Realiza llamadas HTTP al módulo de Donaciones para obtener el historial de donaciones.
 *
 * Según Entrega 1 y 2:
 * - COMPLETITUD: El donador ha donado en 3 categorías distintas
 * - DONACIONES_EXITOSAS: El donador ha realizado 20 donaciones con estado ACEPTADA (sin quejas)
 * - DONACIONES_ASCENDENTES: Las últimas 5 donaciones tienen cantidades en orden ascendente
 * - REVOLUCION_DONADORA: El donador ha realizado 10+ donaciones con cantidad > 50 unidades
 */
@Service
public class MisionEvaluatorService {

  private final FachadaDonaciones fachadaDonaciones;

  @Autowired
  public MisionEvaluatorService(FachadaDonaciones fachadaDonaciones) {
    this.fachadaDonaciones = fachadaDonaciones;
  }

  /**
   * Verifica si un donador cumple con los requisitos de una misión específica.
   */
  public boolean evaluarMision(String donadorID, TipoMisionEnum tipoMision) {
    try {
      return switch (tipoMision) {
        case COMPLETITUD -> evaluarCompletitud(donadorID);
        case DONACIONES_EXITOSAS -> evaluarDonacionesExitosas(donadorID);
        case DONACIONES_ASCENDENTES -> evaluarDonacionesAscendentes(donadorID);
        case REVOLUCION_DONADORA -> evaluarRevolucionDonadora(donadorID);
      };
    } catch (Exception e) {
      // Si hay error en la comunicación con Donaciones, asumimos que no cumple
      System.err.println("Error al evaluar misión: " + e.getMessage());
      return false;
    }
  }

  /**
   * COMPLETITUD: El donador ha donado en 3 categorías distintas.
   * Obtiene las donaciones del donador y verifica que tengan al menos 3 categorías distintas.
   */
  private boolean evaluarCompletitud(String donadorID) {
    // Obtener donaciones desde la fecha actual (null = desde siempre)
    List<DonacionDTO> donaciones = fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, null);
    
    if (donaciones == null || donaciones.isEmpty()) {
      return false;
    }

    // Contar categorías distintas (a través del productoID, que está en cada donación)
    long categoriasDistintas = donaciones.stream()
        .map(DonacionDTO::productoID)
        .distinct()
        .count();

    // Necesita al menos 3 productos/categorías distintas
    return categoriasDistintas >= 3;
  }

  /**
   * DONACIONES_EXITOSAS: El donador ha realizado 20 donaciones con estado ACEPTADA sin quejas.
   * Obtiene las donaciones y verifica que tenga 20+ con estado ACEPTADA.
   */
  private boolean evaluarDonacionesExitosas(String donadorID) {
    List<DonacionDTO> donaciones = fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, null);
    
    if (donaciones == null || donaciones.isEmpty()) {
      return false;
    }

    // Contar donaciones aceptadas (sin quejas = asumimos estado ACEPTADA)
    long donacionesAceptadas = donaciones.stream()
        .filter(d -> EstadoDonacionEnum.ACEPTADA.equals(d.estado()))
        .count();

    return donacionesAceptadas >= 20;
  }

  /**
   * DONACIONES_ASCENDENTES: Las últimas 5 donaciones tienen cantidad en orden ascendente.
   * Obtiene las donaciones y verifica que las últimas 5 cumplan con la regla.
   */
  private boolean evaluarDonacionesAscendentes(String donadorID) {
    List<DonacionDTO> donaciones = fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, null);
    
    if (donaciones == null || donaciones.size() < 5) {
      return false; // Necesita al menos 5 donaciones
    }

    // Tomar las últimas 5 donaciones (al final de la lista)
    int tamanio = donaciones.size();
    List<DonacionDTO> ultimas5 = donaciones.subList(tamanio - 5, tamanio);

    // Verificar si las cantidades están en orden ascendente
    for (int i = 0; i < ultimas5.size() - 1; i++) {
      Integer cantidadActual = ultimas5.get(i).cantidad();
      Integer cantidadSiguiente = ultimas5.get(i + 1).cantidad();
      
      if (cantidadActual == null || cantidadSiguiente == null || cantidadActual >= cantidadSiguiente) {
        return false; // No está en orden ascendente
      }
    }

    return true;
  }

  /**
   * REVOLUCION_DONADORA: El donador ha realizado 10+ donaciones con cantidad > 50 unidades.
   * Obtiene las donaciones y verifica que tenga 10+ donaciones con cantidad > 50.
   */
  private boolean evaluarRevolucionDonadora(String donadorID) {
    List<DonacionDTO> donaciones = fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, null);
    
    if (donaciones == null || donaciones.isEmpty()) {
      return false;
    }

    // Contar donaciones con cantidad > 50
    long donacionesGrandes = donaciones.stream()
        .filter(d -> d.cantidad() != null && d.cantidad() > 50)
        .count();

    return donacionesGrandes >= 10;
  }
}
