package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.MisionDTO;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class IncentivosController {

  private final Fachada fachada;

  @Autowired
  public IncentivosController(Fachada fachada) {
    this.fachada = fachada;
  }

  @PostMapping("/insignias")
  public ResponseEntity<InsigniaDTO> crearInsignia(@RequestBody InsigniaDTO dto) {
    InsigniaDTO creada = fachada.agregarInsignia(dto);
    return ResponseEntity.ok(creada);
  }

  @GetMapping("/insignias")
  public ResponseEntity<List<InsigniaDTO>> listarInsignias() {
    List<InsigniaDTO> lista = fachada.getAllInsignias();
    return ResponseEntity.ok(lista);
  }

  @GetMapping("/insignias/{id}")
  public ResponseEntity<InsigniaDTO> obtenerInsignia(@PathVariable String id) {
    InsigniaDTO dto = fachada.getInsigniaById(id);
    return ResponseEntity.ok(dto);
  }


  @PostMapping("/misiones")
  public ResponseEntity<MisionDTO> crearMision(@RequestBody MisionDTO dto) {
    MisionDTO creada = fachada.agregarMision(dto);
    return ResponseEntity.ok(creada);
  }

  @GetMapping("/misiones")
  public ResponseEntity<List<MisionDTO>> listarMisiones() {
    List<MisionDTO> lista = fachada.getAllMisiones();
    return ResponseEntity.ok(lista);
  }

  @GetMapping("/misiones/{id}")
  public ResponseEntity<MisionDTO> obtenerMision(@PathVariable String id) {
    MisionDTO dto = fachada.getMisionById(id);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/donadores/{donadorID}/insignias")
  public ResponseEntity<List<InsigniaDTO>> getInsigniasDeDonador(@PathVariable String donadorID) {
    try {
      return ResponseEntity.ok(fachada.getInsigniasDeDonador(donadorID));
    } catch (NoSuchElementException e) {
      return ResponseEntity.ok(List.of());
    }
  }

  @GetMapping("/donadores/{donadorID}/mision-actual")
  public ResponseEntity<MisionDTO> getMisionEnCursoDeDonador(@PathVariable String donadorID) {
    try {
      return ResponseEntity.ok(fachada.getMisionEnCursoDeDonador(donadorID));
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
