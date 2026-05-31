package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;

public class Mision {
    private String id;
    private String nombre;
    private String insigniaID;
    private CategoriaDonadorEnum categoriaInicio;
    private CategoriaDonadorEnum categoriaFin;
    private TipoMisionEnum tipo;

    public Mision(String id, String nombre, String insigniaID, CategoriaDonadorEnum categoriaInicio,
            CategoriaDonadorEnum categoriaFin, TipoMisionEnum tipo) {
        this.id = id;
        this.nombre = nombre;
        this.insigniaID = insigniaID;
        this.categoriaInicio = categoriaInicio;
        this.categoriaFin = categoriaFin;
        this.tipo = tipo;
    }

    public Mision(String id, String nombre, String insigniaID, CategoriaDonadorEnum categoriaInicio,
            CategoriaDonadorEnum categoriaFin) {
        this(id, nombre, insigniaID, categoriaInicio, categoriaFin, null);
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getInsigniaID() {
        return insigniaID;
    }
    public void setInsigniaID(String insigniaID) {
        this.insigniaID = insigniaID;
    }
    public CategoriaDonadorEnum getCategoriaInicio() {
        return categoriaInicio;
    }
    public void setCategoriaInicio(CategoriaDonadorEnum categoriaInicio) {
        this.categoriaInicio = categoriaInicio;
    }
    public CategoriaDonadorEnum getCategoriaFin() {
        return categoriaFin;
    }
    public void setCategoriaFin(CategoriaDonadorEnum categoriaFin) {
        this.categoriaFin = categoriaFin;
    }  
    public TipoMisionEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoMisionEnum tipo) {
        this.tipo = tipo;
    }
}
