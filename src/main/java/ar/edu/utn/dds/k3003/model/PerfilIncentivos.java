package ar.edu.utn.dds.k3003.model;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.dds.k3003.dtos.incentivos.CategoriaDonadorEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Perfil de incentivos de un donador (módulo Incentivos).
 *
 * Se identifica por el mismo id que el Donador del módulo DonadoresYEntidades,
 * pero solo guarda la información propia de Incentivos: la misión en curso,
 * las insignias obtenidas y el historial de categorías alcanzadas.
 */
@Entity
@Table(name = "perfiles_incentivos")
public class PerfilIncentivos {

    @Id
    @Column(name = "id")
    private String id;

    private String misionActualID;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<CategoriaDonadorEnum> categorias;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "perfil_id")
    private List<Insignia> insignias;

    // JPA requires a no-arg constructor
    public PerfilIncentivos() {
    }

    public PerfilIncentivos(String id) {
        this.id = id;
        this.insignias = new ArrayList<>();
        this.categorias = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMisionActual() {
        return this.getMisionActualID();
    }

    public String getMisionActualID() {
        return misionActualID;
    }

    public void setMisionActualID(String misionActualID) {
        this.misionActualID = misionActualID;
    }

    public List<CategoriaDonadorEnum> getCategorias() {
        return categorias;
    }

    public void agregarCategoria(CategoriaDonadorEnum categoria) {
        if (this.categorias == null) this.categorias = new ArrayList<>();
        this.categorias.add(categoria);
    }

    public List<Insignia> getInsignias() {
        return insignias;
    }

    public void setInsignias(List<Insignia> insignias) {
        this.insignias = insignias;
    }

    public void agregarInsignia(Insignia insignia) {
        if (this.insignias == null) this.insignias = new ArrayList<>();
        this.insignias.add(insignia);
    }
}
