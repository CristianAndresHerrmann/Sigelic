package com.example.sigelic.model;

/**
 * Enum que define los tipos de recurso disponibles para turnos
 */
public enum TipoRecurso {
    BOX("Box de atención"),
    PISTA("Pista de examen práctico"),
    CONSULTORIO_MEDICO("Consultorio médico"),
    AULA_TEORICO("Aula para examen teórico");

    private final String descripcion;

    TipoRecurso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
