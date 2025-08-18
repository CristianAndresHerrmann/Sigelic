package com.example.sigelic.model;

/**
 * Enum que define los estados posibles de un trámite
 */
public enum EstadoTramite {
    INICIADO("Trámite iniciado"),
    DOCS_OK("Documentación validada"),
    APTO_MED("Apto médico obtenido"),
    EX_TEO_OK("Examen teórico aprobado"),
    EX_PRA_OK("Examen práctico aprobado"),
    PAGO_OK("Pago acreditado"),
    EMITIDA("Licencia emitida"),
    RECHAZADA("Trámite rechazado");

    private final String descripcion;

    EstadoTramite(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
