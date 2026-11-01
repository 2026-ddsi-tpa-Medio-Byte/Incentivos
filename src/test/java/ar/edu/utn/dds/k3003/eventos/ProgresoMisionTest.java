package ar.edu.utn.dds.k3003.eventos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.services.MisionEvaluatorService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * El progreso publicado usa las mismas reglas que la evaluación: una misión se cumple exactamente
 * cuando progreso >= objetivo. Así "14 de 20" nunca contradice lo que decide Incentivos.
 */
class ProgresoMisionTest {

  private static DonacionDTO donacion(String producto, int cantidad, EstadoDonacionEnum estado) {
    return new DonacionDTO("d", "1", "dep", "x", producto, cantidad, estado);
  }

  private static List<DonacionDTO> cantidades(int... valores) {
    List<DonacionDTO> lista = new ArrayList<>();
    for (int v : valores) {
      lista.add(donacion("p", v, EstadoDonacionEnum.ACEPTADA));
    }
    return lista;
  }

  @Test
  void objetivosDeCadaTipo() {
    Assertions.assertEquals(3, MisionEvaluatorService.objetivo(TipoMisionEnum.COMPLETITUD));
    Assertions.assertEquals(20, MisionEvaluatorService.objetivo(TipoMisionEnum.DONACIONES_EXITOSAS));
    Assertions.assertEquals(5, MisionEvaluatorService.objetivo(TipoMisionEnum.DONACIONES_ASCENDENTES));
    Assertions.assertEquals(10, MisionEvaluatorService.objetivo(TipoMisionEnum.REVOLUCION_DONADORA));
  }

  @Test
  void progresoDeCadaTipo() {
    List<DonacionDTO> mezcla = List.of(
        donacion("arroz", 60, EstadoDonacionEnum.ACEPTADA),
        donacion("arroz", 10, EstadoDonacionEnum.INGRESADA),
        donacion("fideos", 70, EstadoDonacionEnum.CONQUEJA));
    Assertions.assertEquals(2, MisionEvaluatorService.progreso(TipoMisionEnum.COMPLETITUD, mezcla));
    Assertions.assertEquals(1, MisionEvaluatorService.progreso(TipoMisionEnum.DONACIONES_EXITOSAS, mezcla));
    Assertions.assertEquals(2, MisionEvaluatorService.progreso(TipoMisionEnum.REVOLUCION_DONADORA, mezcla));
    Assertions.assertEquals(0, MisionEvaluatorService.progreso(TipoMisionEnum.DONACIONES_EXITOSAS, List.of()));
  }

  @Test
  void ascendentesCuentaLaRachaCrecienteQueTerminaEnLaUltima() {
    Assertions.assertEquals(3, MisionEvaluatorService.progreso(TipoMisionEnum.DONACIONES_ASCENDENTES, cantidades(1, 2, 3)));
    Assertions.assertEquals(1, MisionEvaluatorService.progreso(TipoMisionEnum.DONACIONES_ASCENDENTES, cantidades(1, 2, 3, 1)));
    Assertions.assertEquals(1, MisionEvaluatorService.progreso(TipoMisionEnum.DONACIONES_ASCENDENTES, cantidades(3, 3)));
    Assertions.assertEquals(5, MisionEvaluatorService.progreso(TipoMisionEnum.DONACIONES_ASCENDENTES, cantidades(1, 2, 3, 4, 5, 6, 7)));
  }

  @Test
  void laMisionSeCumpleExactamenteCuandoElProgresoLlegaAlObjetivo() {
    List<List<DonacionDTO>> casos = List.of(
        cantidades(1, 2, 3, 4, 5), cantidades(1, 2, 3, 4), cantidades(5, 4, 3, 2, 1),
        cantidades(1, 2, 2, 3, 4, 5), cantidades(9, 1, 2, 3, 4, 5));
    for (List<DonacionDTO> donaciones : casos) {
      for (TipoMisionEnum tipo : TipoMisionEnum.values()) {
        FachadaDonaciones fachada = mock(FachadaDonaciones.class);
        when(fachada.buscarPorDonadorYFechaInicio(eq("1"), any())).thenReturn(donaciones);
        boolean cumple = new MisionEvaluatorService(fachada).evaluarMision("1", tipo);
        boolean llego = MisionEvaluatorService.progreso(tipo, donaciones) >= MisionEvaluatorService.objetivo(tipo);
        Assertions.assertEquals(cumple, llego, tipo + " con " + donaciones.size() + " donaciones");
      }
    }
  }
}
