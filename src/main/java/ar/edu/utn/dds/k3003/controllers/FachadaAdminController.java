package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.eventos.EventosIncentivos;
import ar.edu.utn.dds.k3003.eventos.RepublicadorEventos;
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
    private final EventosIncentivos eventos;
    private final RepublicadorEventos republicador;

    public FachadaAdminController(PerfilIncentivosRepository perfilRepo, InsigniaRepository insigniaRepo,
            MisionRepository misionRepo, ProcesamientoMisionesScheduler misionesScheduler,
            EventosIncentivos eventos, RepublicadorEventos republicador) {
        this.perfilRepo = perfilRepo;
        this.insigniaRepo = insigniaRepo;
        this.misionRepo = misionRepo;
        this.misionesScheduler = misionesScheduler;
        this.eventos = eventos;
        this.republicador = republicador;
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
        // Consultas borra de Mongo todo lo de Incentivos anterior a este evento.
        eventos.moduloReseteado();
        return ResponseEntity.ok("database.cleared");
    }

    /**
     * Vuelve a publicar el estado actual de todo Incentivos (carga inicial del modelo de lectura y
     * resincronización). Devuelve cuántos objetos de cada tipo se republicaron.
     */
    @PostMapping("/republicar-eventos")
    public ResponseEntity<java.util.Map<String, Integer>> republicarEventos() {
        return ResponseEntity.ok(republicador.republicarTodo());
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
