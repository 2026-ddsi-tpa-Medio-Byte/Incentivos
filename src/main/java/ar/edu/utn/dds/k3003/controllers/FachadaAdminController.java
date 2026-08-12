package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.model.Insignia;
import ar.edu.utn.dds.k3003.model.Mision;
import ar.edu.utn.dds.k3003.model.PerfilIncentivos;
import ar.edu.utn.dds.k3003.repositories.PerfilIncentivosRepository;
import ar.edu.utn.dds.k3003.repositories.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.MisionRepository;
import ar.edu.utn.dds.k3003.scheduler.ProcesamientoMisionesScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class FachadaAdminController {

    private final PerfilIncentivosRepository perfilRepo;
    private final InsigniaRepository insigniaRepo;
    private final MisionRepository misionRepo;
    private final ProcesamientoMisionesScheduler misionesScheduler;

    public FachadaAdminController(PerfilIncentivosRepository perfilRepo, InsigniaRepository insigniaRepo,
            MisionRepository misionRepo, ProcesamientoMisionesScheduler misionesScheduler) {
        this.perfilRepo = perfilRepo;
        this.insigniaRepo = insigniaRepo;
        this.misionRepo = misionRepo;
        this.misionesScheduler = misionesScheduler;
    }

    @GetMapping("/insignias")
    public ResponseEntity<List<Insignia>> listInsignias() {
        return ResponseEntity.ok(insigniaRepo.findAll());
    }

    @GetMapping("/misiones")
    public ResponseEntity<List<Mision>> listMisiones() {
        return ResponseEntity.ok(misionRepo.findAll());
    }

    @GetMapping("/perfiles")
    public ResponseEntity<List<PerfilIncentivos>> listPerfiles() {
        return ResponseEntity.ok(perfilRepo.findAll());
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearAll() {
        // Order matters due to FK constraints; remove children first
        insigniaRepo.deleteAll();
        misionRepo.deleteAll();
        perfilRepo.deleteAll();
        return ResponseEntity.ok("database.cleared");
    }

    /**
     * Dispara manualmente el mismo batch que corre el Cron-Job (procesar a todos los
     * donadores con misión asignada). Pensado para demos/correcciones, para no depender
     * de esperar al intervalo configurado en incentivos.cron.intervalo-ms.
     */
    @PostMapping("/procesar-pendientes")
    public ResponseEntity<String> procesarPendientes() {
        misionesScheduler.procesarDonadoresConMisionAsignada();
        return ResponseEntity.ok("procesamiento.disparado");
    }

}
