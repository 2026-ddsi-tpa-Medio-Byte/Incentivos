package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.MisionDTO;
import java.util.List;
import java.util.NoSuchElementException;

public interface FachadaIncentivos {

  InsigniaDTO agregarInsignia(InsigniaDTO insignia);

  MisionDTO agregarMision(MisionDTO mision);

  List<InsigniaDTO> getInsigniasDeDonador(String donadorID) throws NoSuchElementException;

  MisionDTO getMisionEnCursoDeDonador(String donadorID) throws NoSuchElementException;

  void asignarMisionADonador(String donadorID, MisionDTO misionDTO) throws NoSuchElementException;

  void asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO)
      throws NoSuchElementException;

  void procesarDonador(String donadorID) throws NoSuchElementException;

  void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones);

  void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades);
}
