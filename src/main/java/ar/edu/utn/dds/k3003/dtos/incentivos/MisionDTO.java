package ar.edu.utn.dds.k3003.dtos.incentivos;

public record MisionDTO(
    String id,
    String nombre,
    String insigniaID,
    CategoriaDonadorEnum categoriaInicio,
    CategoriaDonadorEnum categoriaFin,
    TipoMisionEnum tipo) {}
