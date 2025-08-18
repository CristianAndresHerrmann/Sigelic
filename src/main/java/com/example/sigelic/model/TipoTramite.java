package com.example.sigelic.model;

/**
 * Enum que define los tipos de trámite disponibles
 */
public enum TipoTramite {
    EMISION("Emisión de nueva licencia"),
    RENOVACION("Renovación de licencia"),
    DUPLICADO("Duplicado por pérdida/robo/deterioro"),
    CAMBIO_DOMICILIO("Cambio de domicilio");

    private final String descripcion;

    TipoTramite(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
