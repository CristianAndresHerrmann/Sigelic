package com.example.sigelic.model;

/**
 * Enum para los motivos de duplicación de licencias
 */
public enum MotivoDuplicacion {
    ROBO("Robo"),
    EXTRAVÍO("Extravío"),
    DETERIORO("Deterioro");

    private final String descripcion;

    MotivoDuplicacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
