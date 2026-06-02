package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/misiones")
public class MisionController {

    private final MisionRepository misionRepository;

    public MisionController(MisionRepository misionRepository) {
        this.misionRepository = misionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Mision>> listarTodas() {
        return ResponseEntity.ok(misionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mision> obtenerPorId(@PathVariable String id) {
        Optional<Mision> mision = misionRepository.findById(id);
        return mision.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mision> crear(@RequestBody Mision mision) {
        Mision saved = misionRepository.save(mision);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mision> actualizar(@PathVariable String id, @RequestBody Mision misionActualizada) {
        Optional<Mision> misionOpt = misionRepository.findById(id);
        if (misionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Mision mision = misionOpt.get();
        if (misionActualizada.getNombre() != null) mision.setNombre(misionActualizada.getNombre());
        if (misionActualizada.getInsigniaID() != null) mision.setInsigniaID(misionActualizada.getInsigniaID());
        if (misionActualizada.getCategoriaInicio() != null) mision.setCategoriaInicio(misionActualizada.getCategoriaInicio());
        if (misionActualizada.getCategoriaFin() != null) mision.setCategoriaFin(misionActualizada.getCategoriaFin());
        if (misionActualizada.getTipo() != null) mision.setTipo(misionActualizada.getTipo());

        Mision updated = misionRepository.save(mision);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!misionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        misionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
