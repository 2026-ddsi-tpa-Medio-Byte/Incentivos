package ar.edu.utn.dds.k3003.model;

import java.util.List;

import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.DonadorStatsDTO;
import ar.edu.utn.dds.k3003.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "donadores")
public class Donador {

    @Id
    @Column(name = "id")
    private String id;
    private String nombre;
    private String apellido;
    private Integer edad;
    private String email;
    private String nroDocumento;
    private String domicilio;
    private EstadoDonadorEnum estado;
    private String categoria;
    private String misionActualID;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private java.util.List<CategoriaDonadorEnum> categorias;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "donador_id")
    private List<Insignia> insignias;

    public Donador(String id, String nombre, String apellido, Integer edad, String email, String nroDocumento,
            String domicilio, EstadoDonadorEnum estado, String categoria, String misionActualID, DonadorStatsDTO stats,
            List<Insignia> insignias) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.email = email;
        this.nroDocumento = nroDocumento;
        this.domicilio = domicilio;
        this.estado = estado;
        this.categoria = categoria;
        this.misionActualID = misionActualID;
        this.insignias = insignias;
    }

    public Donador(String nombre, String apellido, Integer edad, String email, String nroDocumento,
            String domicilio) {
        this.id = null;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.email = email;
        this.nroDocumento = nroDocumento;
        this.domicilio = domicilio;
        this.estado = null;
        this.categoria = null;
        this.misionActualID = null;
        this.insignias = new java.util.ArrayList<>();
        this.categorias = new java.util.ArrayList<>();
    }

    // JPA requires a no-arg constructor
    public Donador() {
    }


    public List<Insignia> getInsignias() {
        return insignias;
    }


    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Donador getDonadorDTO() {
        return this;
    }
    public String getMisionActual(){
        return this.getMisionActualID();
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


    public String getApellido() {
        return apellido;
    }


    public void setApellido(String apellido) {
        this.apellido = apellido;
    }


    public Integer getEdad() {
        return edad;
    }


    public void setEdad(Integer edad) {
        this.edad = edad;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getNroDocumento() {
        return nroDocumento;
    }


    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }


    public String getDomicilio() {
        return domicilio;
    }


    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }


    public EstadoDonadorEnum getEstado() {
        return estado;
    }


    public void setEstado(EstadoDonadorEnum estado) {
        this.estado = estado;
    }


    public String getCategoria() {
        return categoria;
    }


    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }


    public String getMisionActualID() {
        return misionActualID;
    }


    public void setMisionActualID(String misionActualID) {
        this.misionActualID = misionActualID;
    }


    public void setInsignias(List<Insignia> insignias) {
        this.insignias = insignias;
    }
    public void agregarInsignia(Insignia insignia) {
        this.insignias.add(insignia);
    }
    public java.util.List<CategoriaDonadorEnum> getCategorias() {
        return categorias;
    }

    public void agregarCategoria(CategoriaDonadorEnum categoria) {
        if (this.categorias == null) this.categorias = new java.util.ArrayList<>();
        this.categorias.add(categoria);
    }
}
