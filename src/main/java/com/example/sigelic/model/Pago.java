package com.example.sigelic.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad que representa un pago de trámite
 */
@Entity
@Table(name = "pagos")
@Data
@EqualsAndHashCode(exclude = "tramite")
@ToString(exclude = "tramite")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id", nullable = false)
    private Tramite tramite;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedioPago medio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_acreditacion")
    private LocalDateTime fechaAcreditacion;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Size(max = 100, message = "El número de transacción no puede exceder 100 caracteres")
    @Column(name = "numero_transaccion", length = 100)
    private String numeroTransaccion;

    @Size(max = 100, message = "El comprobante no puede exceder 100 caracteres")
    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @Size(max = 100, message = "El cajero/operador no puede exceder 100 caracteres")
    @Column(length = 100)
    private String cajero;

    /**
     * Verifica si el pago está acreditado
     */
    public boolean isAcreditado() {
        return estado == EstadoPago.ACREDITADO;
    }

    /**
     * Verifica si el pago está vencido
     */
    public boolean isVencido() {
        return estado == EstadoPago.VENCIDO || 
            (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDateTime.now()));
    }

    /**
     * Acredita el pago
     */
    public void acreditar() {
        if (estado == EstadoPago.PENDIENTE) {
            estado = EstadoPago.ACREDITADO;
            fechaAcreditacion = LocalDateTime.now();
        }
    }

    /**
     * Rechaza el pago
     */
    public void rechazar(String motivo) {
        estado = EstadoPago.RECHAZADO;
        observaciones = motivo;
    }

    /**
     * Marca el pago como vencido
     */
    public void marcarVencido() {
        if (estado == EstadoPago.PENDIENTE) {
            estado = EstadoPago.VENCIDO;
        }
    }

    @PrePersist
    private void establecerVencimientoPorDefecto() {
        if (fechaVencimiento == null) {
            // Por defecto, la orden de pago vence en 48 horas
            fechaVencimiento = LocalDateTime.now().plusHours(48);
        }
    }
}
