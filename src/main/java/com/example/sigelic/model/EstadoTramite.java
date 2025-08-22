package com.example.sigelic.model;

/**
 * Enum que define los estados posibles de un trámite
 */
public enum EstadoTramite {
    INICIADO("Trámite iniciado"),
    DOCS_OK("Documentación validada"),
    DOCS_RECHAZADAS("Documentación rechazada"),
    APTO_MED("Apto médico obtenido"),
    APTO_MED_RECHAZADO("Apto médico rechazado - Trámite finalizado"),
    EX_TEO_OK("Examen teórico aprobado"),
    EX_TEO_RECHAZADO("Examen teórico desaprobado - Puede reintentar"),
    EX_PRA_OK("Examen práctico aprobado"),
    EX_PRA_RECHAZADO("Examen práctico desaprobado - Puede reintentar"),
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

    /**
     * Verifica si el estado representa un rechazo
     */
    public boolean esRechazo() {
        return this == DOCS_RECHAZADAS || 
               this == APTO_MED_RECHAZADO || 
               this == EX_TEO_RECHAZADO || 
               this == EX_PRA_RECHAZADO || 
               this == RECHAZADA;
    }

    /**
     * Verifica si el estado permite reintento
     */
    public boolean permiteReintento() {
        return this == EX_TEO_RECHAZADO || this == EX_PRA_RECHAZADO;
    }

    /**
     * Verifica si el estado es final (no puede continuar)
     */
    public boolean esFinal() {
        return this == APTO_MED_RECHAZADO || 
               this == EMITIDA || 
               this == RECHAZADA;
    }

    /**
     * Verifica si el trámite puede continuar hacia el siguiente paso
     */
    public boolean puedeAvanzar() {
        return !esRechazo() && !esFinal();
    }
}
