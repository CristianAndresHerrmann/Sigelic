package com.example.sigelic.model;

/**
 * Enum que define los estados posibles de una licencia
 */
public enum EstadoLicencia {
    VIGENTE("Licencia vigente"),
    VENCIDA("Licencia vencida"),
    SUSPENDIDA("Licencia suspendida"),
    INHABILITADA("Licencia inhabilitada"),
    DUPLICADA("Licencia duplicada");

    private final String descripcion;

    EstadoLicencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
