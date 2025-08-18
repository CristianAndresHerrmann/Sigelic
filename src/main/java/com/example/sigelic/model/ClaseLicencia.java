package com.example.sigelic.model;

/**
 * Enum que define las clases de licencia de conducir disponibles
 */
public enum ClaseLicencia {
    A("Motocicletas hasta 150cc", 17),
    B("Automóviles particulares", 17),
    C("Camiones hasta 7500kg", 21),
    D("Transporte de pasajeros", 21),
    E("Camiones pesados", 21);

    private final String descripcion;
    private final int edadMinima;

    ClaseLicencia(String descripcion, int edadMinima) {
        this.descripcion = descripcion;
        this.edadMinima = edadMinima;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getEdadMinima() {
        return edadMinima;
    }
}
