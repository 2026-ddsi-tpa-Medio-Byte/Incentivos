package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/misiones")
public class MisionController {

    private static final Logger log = LoggerFactory.getLogger(MisionController.class);
    private final MisionRepository misionRepository;

    public MisionController(MisionRepository misionRepository) {
        this.misionRepository = misionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Mision>> listarTodas() {
        log.info("GET /api/misiones - listando todas las misiones");
        List<Mision> misiones = misionRepository.findAll();
        log.info("Listado de misiones solicitado: {} resultados", misiones.size());
        return ResponseEntity.ok(misiones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mision> obtenerPorId(@PathVariable String id) {
        log.info("GET /api/misiones/{id} - consultando misión id={}", id);
        Optional<Mision> mision = misionRepository.findById(id);
        if (mision.isPresent()) {
            log.info("Misión encontrada id={}", id);
            return ResponseEntity.ok(mision.get());
        }
        log.warn("Misión no encontrada id={}", id);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Mision> crear(@RequestBody Mision mision) {
        log.info("POST /api/misiones - creando misión id={}, nombre={}", mision != null ? mision.getId() : null, mision != null ? mision.getNombre() : null);
        Mision saved = misionRepository.save(mision);
        log.info("Misión creada correctamente id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mision> actualizar(@PathVariable String id, @RequestBody Mision misionActualizada) {
        log.info("PUT /api/misiones/{id} - actualizando misión id={}", id);
        Optional<Mision> misionOpt = misionRepository.findById(id);
        if (misionOpt.isEmpty()) {
            log.warn("Intento de actualizar misión inexistente id={}", id);
            return ResponseEntity.notFound().build();
        }

        Mision mision = misionOpt.get();
        if (misionActualizada.getNombre() != null) mision.setNombre(misionActualizada.getNombre());
        if (misionActualizada.getInsigniaID() != null) mision.setInsigniaID(misionActualizada.getInsigniaID());
        if (misionActualizada.getCategoriaInicio() != null) mision.setCategoriaInicio(misionActualizada.getCategoriaInicio());
        if (misionActualizada.getCategoriaFin() != null) mision.setCategoriaFin(misionActualizada.getCategoriaFin());
        if (misionActualizada.getTipo() != null) mision.setTipo(misionActualizada.getTipo());

        Mision updated = misionRepository.save(mision);
        log.info("Misión actualizada correctamente id={}", updated.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        log.info("DELETE /api/misiones/{id} - eliminando misión id={}", id);
        if (!misionRepository.existsById(id)) {
            log.warn("Intento de eliminar misión inexistente id={}", id);
            return ResponseEntity.notFound().build();
        }
        misionRepository.deleteById(id);
        log.info("Misión eliminada correctamente id={}", id);
        return ResponseEntity.noContent().build();
    }

}
