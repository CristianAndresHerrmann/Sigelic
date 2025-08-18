package com.example.sigelic.model;

/**
 * Enum que define los estados posibles de un pago
 */
public enum EstadoPago {
    PENDIENTE("Pago pendiente"),
    ACREDITADO("Pago acreditado"),
    RECHAZADO("Pago rechazado"),
    VENCIDO("Orden de pago vencida");

    private final String descripcion;

    EstadoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
