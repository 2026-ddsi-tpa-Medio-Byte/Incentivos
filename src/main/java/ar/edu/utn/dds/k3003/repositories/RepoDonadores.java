package ar.edu.utn.dds.k3003.repositories;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.model.Donador;

public class RepoDonadores {
    private List<Donador> donadores;
    private Map<String, String> misionesPorDonador = new HashMap<>();
    private Map<String, List<String>> insigniasPorDonador = new HashMap<>();
    private Map<String, List<CategoriaDonadorEnum>> categoriasPorDonador = new HashMap<>();

    public RepoDonadores(List<Donador> donadores) {
        this.donadores = donadores;
    }

    public Donador agregarDonador(Donador donador) {
        this.donadores.add(donador);
        return donador;
    }

    public Donador buscarDonadorPorID(String donadorID) {
        return this.donadores.stream().filter(d -> d.getId().equals(donadorID)).findFirst().orElseThrow(() -> new RuntimeException("No se encontró el donador con ID: " + donadorID));
    }

    public List<Donador> getDonadores() {
        return donadores;
    }

    // Métodos para gestionar misiones por donador
    public Map<String, String> getMisionesPorDonador() {
        return misionesPorDonador;
    }

    public void asignarMisionADonador(String donadorID, String misionID) {
        misionesPorDonador.put(donadorID, misionID);
    }

    // Métodos para gestionar insignias por donador
    public Map<String, List<String>> getInsigniasPorDonador() {
        return insigniasPorDonador;
    }

    public void asignarInsigniaADonador(String donadorID, String insigniaID) {
        insigniasPorDonador.computeIfAbsent(donadorID, k -> new ArrayList<>()).add(insigniaID);
    }

    // Métodos para gestionar categorías por donador
    public Map<String, List<CategoriaDonadorEnum>> getCategoriasPorDonador() {
        return categoriasPorDonador;
    }

    public void agregarCategoriADonador(String donadorID, CategoriaDonadorEnum categoria) {
        categoriasPorDonador.computeIfAbsent(donadorID, k -> new ArrayList<>()).add(categoria);
    }

}
