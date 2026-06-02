package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.model.Donador;
import ar.edu.utn.dds.k3003.repositories.DonadorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/donadores")
public class DonadorController {

  private final DonadorRepository donadorRepository;

  public DonadorController(DonadorRepository donadorRepository) {
    this.donadorRepository = donadorRepository;
  }

  @GetMapping
  public ResponseEntity<List<Donador>> listarTodos() {
    return ResponseEntity.ok(donadorRepository.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Donador> obtenerPorId(@PathVariable String id) {
    Optional<Donador> donador = donadorRepository.findById(id);
    return donador.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Donador> crear(@RequestBody Donador donador) {
    Donador saved = donadorRepository.save(donador);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Donador> actualizar(@PathVariable String id, @RequestBody Donador donadorActualizado) {
    Optional<Donador> donadorOpt = donadorRepository.findById(id);
    if (donadorOpt.isEmpty()) {
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
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable String id) {
    if (!donadorRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    donadorRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
