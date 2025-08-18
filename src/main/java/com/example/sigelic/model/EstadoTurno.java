package com.example.sigelic.model;

/**
 * Enum que define los estados posibles de un turno
 */
public enum EstadoTurno {
    RESERVADO("Turno reservado"),
    CONFIRMADO("Turno confirmado"),
    COMPLETADO("Turno completado"),
    CANCELADO("Turno cancelado"),
    AUSENTE("Ausente al turno");

    private final String descripcion;

    EstadoTurno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
