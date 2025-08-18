package com.example.sigelic.model;

/**
 * Enum que define los tipos de turno disponibles
 */
public enum TipoTurno {
    DOCUMENTACION("Validación de documentación"),
    APTO_MEDICO("Apto médico"),
    EXAMEN_TEORICO("Examen teórico"),
    EXAMEN_PRACTICO("Examen práctico"),
    EMISION("Emisión de licencia");

    private final String descripcion;

    TipoTurno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
