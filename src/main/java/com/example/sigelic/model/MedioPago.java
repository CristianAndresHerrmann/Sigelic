package com.example.sigelic.model;

/**
 * Enum que define los medios de pago disponibles
 */
public enum MedioPago {
    CAJA("Pago en caja"),
    TRANSFERENCIA("Transferencia bancaria"),
    PASARELA_ONLINE("Pago online");

    private final String descripcion;

    MedioPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
