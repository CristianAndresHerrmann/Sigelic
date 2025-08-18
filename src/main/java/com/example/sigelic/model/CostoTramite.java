package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa los costos de los diferentes tipos de trámite
 */
@Entity
@Table(name = "costos_tramite")
@Data
public class CostoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTramite tipoTramite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaseLicencia claseLicencia;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo debe ser positivo")
    @Digits(integer = 10, fraction = 2, message = "El costo debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @NotNull(message = "La fecha de vigencia es obligatoria")
    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fechaVigenciaHasta;

    @Column(nullable = false)
    private Boolean activo = true;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(length = 200)
    private String descripcion;

    /**
     * Verifica si el costo está vigente en una fecha específica
     */
    public boolean isVigenteEn(LocalDate fecha) {
        if (!activo) {
            return false;
        }
        
        boolean despuesDelInicio = !fecha.isBefore(fechaVigenciaDesde);
        boolean antesDelFin = fechaVigenciaHasta == null || !fecha.isAfter(fechaVigenciaHasta);
        
        return despuesDelInicio && antesDelFin;
    }

    /**
     * Verifica si el costo está vigente actualmente
     */
    public boolean isVigente() {
        return isVigenteEn(LocalDate.now());
    }
}
