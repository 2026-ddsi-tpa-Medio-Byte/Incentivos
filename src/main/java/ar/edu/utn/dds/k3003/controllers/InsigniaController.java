package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/insignias")
public class InsigniaController {

    private final InsigniaRepository insigniaRepository;

    public InsigniaController(InsigniaRepository insigniaRepository) {
        this.insigniaRepository = insigniaRepository;
    }

    @GetMapping
    public ResponseEntity<List<Insignia>> listarTodas() {
        return ResponseEntity.ok(insigniaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insignia> obtenerPorId(@PathVariable String id) {
        Optional<Insignia> insignia = insigniaRepository.findById(id);
        return insignia.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Insignia> crear(@RequestBody Insignia insignia) {
        Insignia saved = insigniaRepository.save(insignia);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Insignia> actualizar(@PathVariable String id, @RequestBody Insignia insigniaActualizada) {
        Optional<Insignia> insigniaOpt = insigniaRepository.findById(id);
        if (insigniaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Insignia insignia = insigniaOpt.get();
        if (insigniaActualizada.getNombre() != null) insignia.setNombre(insigniaActualizada.getNombre());
        if (insigniaActualizada.getDescripcion() != null) insignia.setDescripcion(insigniaActualizada.getDescripcion());

        Insignia updated = insigniaRepository.save(insignia);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!insigniaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        insigniaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
