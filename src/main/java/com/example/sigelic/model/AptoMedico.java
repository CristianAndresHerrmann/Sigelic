package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un apto médico
 */
@Entity
@Table(name = "aptos_medicos")
@Data
@EqualsAndHashCode(exclude = "tramite")
@ToString(exclude = "tramite")
public class AptoMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id", nullable = false)
    private Tramite tramite;

    @NotBlank(message = "El profesional médico es obligatorio")
    @Size(max = 100, message = "El profesional no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String profesional;

    @Size(max = 50, message = "La matrícula no puede exceder 50 caracteres")
    @Column(name = "matricula_profesional", length = 50)
    private String matriculaProfesional;

    @Column(nullable = false)
    private Boolean apto;

    @NotNull(message = "La fecha del examen médico es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @Size(max = 200, message = "Las restricciones no pueden exceder 200 caracteres")
    @Column(length = 200)
    private String restricciones;

    @DecimalMin(value = "0.0", message = "La presión arterial sistólica debe ser positiva")
    @Column(name = "presion_sistolica")
    private Double presionSistolica;

    @DecimalMin(value = "0.0", message = "La presión arterial diastólica debe ser positiva")
    @Column(name = "presion_diastolica")
    private Double presionDiastolica;

    @Size(max = 50, message = "La agudeza visual no puede exceder 50 caracteres")
    @Column(name = "agudeza_visual", length = 50)
    private String agudezaVisual;

    /**
     * Verifica si el apto médico está vigente
     */
    public boolean isVigente() {
        if (!apto) {
            return false;
        }
        
        if (fechaVencimiento == null) {
            // Si no tiene fecha de vencimiento específica, usar 1 año desde la fecha del examen
            return fecha.toLocalDate().isAfter(LocalDate.now().minusYears(1));
        }
        
        return !fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Verifica si el apto médico está vigente en una fecha específica
     */
    public boolean isVigenteEn(LocalDate fechaConsulta) {
        if (!apto) {
            return false;
        }
        
        if (fechaVencimiento == null) {
            // Si no tiene fecha de vencimiento específica, usar 1 año desde la fecha del examen
            return fecha.toLocalDate().isAfter(fechaConsulta.minusYears(1));
        }
        
        return !fechaVencimiento.isBefore(fechaConsulta);
    }

    @PrePersist
    private void establecerVencimientoPorDefecto() {
        if (fechaVencimiento == null && apto) {
            // Por defecto, el apto médico vence al año
            fechaVencimiento = fecha.toLocalDate().plusYears(1);
        }
    }
}
