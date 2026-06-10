package ar.edu.utn.dds.k3003.dtos.logistica;

import java.time.LocalDateTime;

public record AsignacionDTO(
    String id,
    String paqueteID,
    String necesidadID,
    LocalDateTime fecha,
    EstadoAsginacionEnum estado) {}
