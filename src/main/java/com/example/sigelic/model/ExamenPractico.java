package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad que representa un examen práctico
 */
@Entity
@Table(name = "examenes_practicos")
@Data
@EqualsAndHashCode(exclude = "tramite")
@ToString(exclude = "tramite")
public class ExamenPractico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id", nullable = false)
    private Tramite tramite;

    @Min(value = 0, message = "Las faltas leves no pueden ser negativas")
    @Column(name = "faltas_leves", nullable = false)
    private Integer faltasLeves = 0;

    @Min(value = 0, message = "Las faltas graves no pueden ser negativas")
    @Column(name = "faltas_graves", nullable = false)
    private Integer faltasGraves = 0;

    @Column(nullable = false)
    private Boolean aprobado;

    @NotNull(message = "La fecha del examen es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Size(max = 100, message = "El examinador no puede exceder 100 caracteres")
    @Column(length = 100)
    private String examinador;

    @Size(max = 50, message = "El vehículo utilizado no puede exceder 50 caracteres")
    @Column(name = "vehiculo_utilizado", length = 50)
    private String vehiculoUtilizado;

    @Size(max = 50, message = "La pista utilizada no puede exceder 50 caracteres")
    @Column(name = "pista_utilizada", length = 50)
    private String pistaUtilizada;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    /**
     * Calcula si el examen está aprobado según las reglas:
     * - Sin faltas graves
     * - Máximo 3 faltas leves
     */
    public boolean calcularAprobacion() {
        return faltasGraves == 0 && faltasLeves <= 3;
    }

    /**
     * Verifica si el examen está vigente (máximo 6 meses)
     */
    public boolean isVigente() {
        return aprobado && fecha.isAfter(LocalDateTime.now().minusMonths(6));
    }

    @PrePersist
    @PreUpdate
    private void calcularAprobado() {
        this.aprobado = calcularAprobacion();
    }
}
