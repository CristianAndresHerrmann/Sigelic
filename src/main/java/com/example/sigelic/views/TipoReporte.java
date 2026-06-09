package com.example.sigelic.views;

/**
 * Enumeración de los tipos de reportes disponibles en el sistema
 */
public enum TipoReporte {
    PAGOS("Pagos Recibidos"),
    TRAMITES("Trámites por Estado"),
    LICENCIAS("Licencias Emitidas"),
    EXAMENES("Exámenes Realizados"),
    TURNOS("Turnos"),
    RECAUDACION("Recaudación"),
    INHABILITACIONES("Inhabilitaciones"),
    RENDIMIENTO("Rendimiento Examinadores"),
    DASHBOARD("Dashboard General");

    private final String descripcion;

    TipoReporte(String descripcion) {
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
