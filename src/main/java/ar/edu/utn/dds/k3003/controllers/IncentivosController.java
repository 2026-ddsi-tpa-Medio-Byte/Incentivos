package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.dtos.incentivos.MisionDTO;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(IncentivosController.class);
  private final Fachada fachada;

  @Autowired
  public IncentivosController(Fachada fachada) {
    this.fachada = fachada;
  }

  @PostMapping("/insignias")
  public ResponseEntity<InsigniaDTO> crearInsignia(@RequestBody InsigniaDTO dto) {
    log.info("POST /insignias - creando insignia con id={}, nombre={}", dto != null ? dto.id() : null, dto != null ? dto.nombre() : null);
    try {
      InsigniaDTO creada = fachada.agregarInsignia(dto);
      log.info("Insignia creada correctamente: id={}", creada.id());
      return ResponseEntity.ok(creada);
    } catch (Exception e) {
      log.error("Error al crear insignia id={}, nombre={}", dto != null ? dto.id() : null, dto != null ? dto.nombre() : null, e);
      throw e;
    }
  }

  @GetMapping("/insignias")
  public ResponseEntity<List<InsigniaDTO>> listarInsignias() {
    log.info("GET /insignias - solicitando listado de insignias");
    List<InsigniaDTO> lista = fachada.getAllInsignias();
    log.info("Listado de insignias solicitado: {} resultados", lista.size());
    return ResponseEntity.ok(lista);
  }

  @GetMapping("/insignias/{id}")
  public ResponseEntity<InsigniaDTO> obtenerInsignia(@PathVariable String id) {
    log.info("GET /insignias/{id} - consultando insignia id={}", id);
    try {
      InsigniaDTO insignia = fachada.getInsigniaById(id);
      log.info("Insignia encontrada id={}", id);
      return ResponseEntity.ok(insignia);
    } catch (NoSuchElementException e) {
      log.warn("Insignia no encontrada id={}", id);
      return ResponseEntity.notFound().build();
    }
  }


  @PostMapping("/misiones")
  public ResponseEntity<MisionDTO> crearMision(@RequestBody MisionDTO dto) {
    log.info("POST /misiones - creando mision con id={}, nombre={}", dto != null ? dto.id() : null, dto != null ? dto.nombre() : null);
    try {
      MisionDTO creada = fachada.agregarMision(dto);
      log.info("Mision creada correctamente: id={}", creada.id());
      return ResponseEntity.ok(creada);
    } catch (Exception e) {
      log.error("Error al crear misión id={}, nombre={}", dto != null ? dto.id() : null, dto != null ? dto.nombre() : null, e);
      throw e;
    }
  }

  @GetMapping("/misiones")
  public ResponseEntity<List<MisionDTO>> listarMisiones() {
    log.info("GET /misiones - solicitando listado de misiones");
    List<MisionDTO> lista = fachada.getAllMisiones();
    log.info("Listado de misiones solicitado: {} resultados", lista.size());
    return ResponseEntity.ok(lista);
  }

  @GetMapping("/misiones/{id}")
  public ResponseEntity<MisionDTO> obtenerMision(@PathVariable String id) {
    log.info("GET /misiones/{id} - consultando misión id={}", id);
    try {
      MisionDTO mision = fachada.getMisionById(id);
      log.info("Misión encontrada id={}", id);
      return ResponseEntity.ok(mision);
    } catch (NoSuchElementException e) {
      log.warn("Misión no encontrada id={}", id);
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/donadores/{donadorID}/insignias")
  public ResponseEntity<List<InsigniaDTO>> getInsigniasDeDonador(@PathVariable String donadorID) {
    log.info("GET /donadores/{donadorID}/insignias - consultando insignias del donador {}", donadorID);
    try {
      List<InsigniaDTO> insignias = fachada.getInsigniasDeDonador(donadorID);
      log.info("Se encontraron {} insignias para el donador {}", insignias.size(), donadorID);
      return ResponseEntity.ok(insignias);
    } catch (NoSuchElementException e) {
      log.warn("El donador {} no tiene insignias asignadas", donadorID);
      return ResponseEntity.ok(List.of());
    }
  }

  @GetMapping("/donadores/{donadorID}/mision-actual")
  public ResponseEntity<MisionDTO> getMisionEnCursoDeDonador(@PathVariable String donadorID) {
    log.info("GET /donadores/{donadorID}/mision-actual - consultando misión actual del donador {}", donadorID);
    try {
      MisionDTO mision = fachada.getMisionEnCursoDeDonador(donadorID);
      log.info("Misión actual del donador {}: id={}", donadorID, mision.id());
      return ResponseEntity.ok(mision);
    } catch (NoSuchElementException e) {
      log.warn("No hay misión actual para el donador {}", donadorID);
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/donadores/{donadorID}/mision-actual")
  public ResponseEntity<Void> asignarMisionADonador(@PathVariable String donadorID, @RequestBody MisionDTO misionDTO) {
    log.info("POST /donadores/{donadorID}/mision-actual - asignando misión {} al donador {}", misionDTO != null ? misionDTO.id() : null, donadorID);
    try {
      fachada.asignarMisionADonador(donadorID, misionDTO);
      log.info("Misión {} asignada correctamente al donador {}", misionDTO != null ? misionDTO.id() : null, donadorID);
      return ResponseEntity.ok().build();
    } catch (NoSuchElementException e) {
      log.warn("No se pudo asignar misión al donador {}: recurso no encontrado", donadorID, e);
      return ResponseEntity.notFound().build();
    } catch (RuntimeException e) {
      log.error("Error al asignar misión {} al donador {}", misionDTO != null ? misionDTO.id() : null, donadorID, e);
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/donadores/{donadorID}/insignias")
  public ResponseEntity<Void> asignarInsigniaADonador(@PathVariable String donadorID, @RequestBody InsigniaDTO insigniaDTO) {
    log.info("POST /donadores/{donadorID}/insignias - asignando insignia {} al donador {}", insigniaDTO != null ? insigniaDTO.id() : null, donadorID);
    try {
      fachada.asignarInsigniaADonador(donadorID, insigniaDTO);
      log.info("Insignia {} asignada correctamente al donador {}", insigniaDTO != null ? insigniaDTO.id() : null, donadorID);
      return ResponseEntity.ok().build();
    } catch (NoSuchElementException e) {
      log.warn("No se pudo asignar insignia al donador {}: recurso no encontrado", donadorID, e);
      return ResponseEntity.notFound().build();
    } catch (RuntimeException e) {
      log.error("Error al asignar insignia {} al donador {}", insigniaDTO != null ? insigniaDTO.id() : null, donadorID, e);
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/donadores/{donadorID}/procesar")
  public ResponseEntity<Void> procesarDonador(@PathVariable String donadorID) {
    log.info("POST /donadores/{donadorID}/procesar - procesando donador {}", donadorID);
    try {
      fachada.procesarDonador(donadorID);
      log.info("Procesamiento finalizado correctamente para donador {}", donadorID);
      return ResponseEntity.ok().build();
    } catch (NoSuchElementException e) {
      log.warn("No se pudo procesar al donador {}: no encontrado", donadorID, e);
      return ResponseEntity.notFound().build();
    }
  }
}
