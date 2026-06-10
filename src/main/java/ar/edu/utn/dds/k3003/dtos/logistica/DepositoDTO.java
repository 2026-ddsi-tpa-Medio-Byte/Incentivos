package ar.edu.utn.dds.k3003.dtos.logistica;

import java.util.List;

public record DepositoDTO(
    String id,
    TipoAlgoritmoEnum algoritmo,
    String nombre,
    String direccion,
    Integer capacidadMaxima,
    List<PaqueteDTO> stockActual) {}
