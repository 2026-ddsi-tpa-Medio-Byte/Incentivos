package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.model.Donador;
import ar.edu.utn.dds.k3003.repositories.DonadorRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donadores")
public class DonadorController {

  private static final Logger log = LoggerFactory.getLogger(DonadorController.class);
  private final DonadorRepository donadorRepository;

  public DonadorController(DonadorRepository donadorRepository) {
    this.donadorRepository = donadorRepository;
  }

  @GetMapping
  public ResponseEntity<List<Donador>> listarTodos() {
    log.info("GET /api/donadores - listando todos los donadores");
    List<Donador> donadores = donadorRepository.findAll();
    log.info("Listado de donadores solicitado: {} resultados", donadores.size());
    return ResponseEntity.ok(donadores);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Donador> obtenerPorId(@PathVariable String id) {
    log.info("GET /api/donadores/{id} - consultando donador id={}", id);
    Optional<Donador> donador = donadorRepository.findById(id);
    if (donador.isPresent()) {
      log.info("Donador encontrado id={}", id);
      return ResponseEntity.ok(donador.get());
    }
    log.warn("Donador no encontrado id={}", id);
    return ResponseEntity.notFound().build();
  }

  @PostMapping
  public ResponseEntity<Donador> crear(@RequestBody Donador donador) {
    log.info("POST /api/donadores - creando donador id={}, nombre={}", donador != null ? donador.getId() : null, donador != null ? donador.getNombre() : null);
    Donador saved = donadorRepository.save(donador);
    log.info("Donador creado correctamente id={}", saved.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Donador> actualizar(@PathVariable String id, @RequestBody Donador donadorActualizado) {
    log.info("PUT /api/donadores/{id} - actualizando donador id={}", id);
    Optional<Donador> donadorOpt = donadorRepository.findById(id);
    if (donadorOpt.isEmpty()) {
      log.warn("Intento de actualizar donador inexistente id={}", id);
      return ResponseEntity.notFound().build();
    }

    Donador donador = donadorOpt.get();
    if (donadorActualizado.getNombre() != null) donador.setNombre(donadorActualizado.getNombre());
    if (donadorActualizado.getApellido() != null) donador.setApellido(donadorActualizado.getApellido());
    if (donadorActualizado.getEdad() != null) donador.setEdad(donadorActualizado.getEdad());
    if (donadorActualizado.getEmail() != null) donador.setEmail(donadorActualizado.getEmail());
    if (donadorActualizado.getNroDocumento() != null) donador.setNroDocumento(donadorActualizado.getNroDocumento());
    if (donadorActualizado.getDomicilio() != null) donador.setDomicilio(donadorActualizado.getDomicilio());
    if (donadorActualizado.getEstado() != null) donador.setEstado(donadorActualizado.getEstado());
    if (donadorActualizado.getCategoria() != null) donador.setCategoria(donadorActualizado.getCategoria());
    if (donadorActualizado.getMisionActualID() != null) donador.setMisionActualID(donadorActualizado.getMisionActualID());

    Donador updated = donadorRepository.save(donador);
    log.info("Donador actualizado correctamente id={}", updated.getId());
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable String id) {
    log.info("DELETE /api/donadores/{id} - eliminando donador id={}", id);
    if (!donadorRepository.existsById(id)) {
      log.warn("Intento de eliminar donador inexistente id={}", id);
      return ResponseEntity.notFound().build();
    }
    donadorRepository.deleteById(id);
    log.info("Donador eliminado correctamente id={}", id);
    return ResponseEntity.noContent().build();
  }
}
