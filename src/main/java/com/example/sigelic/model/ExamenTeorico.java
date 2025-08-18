package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad que representa un examen teórico
 */
@Entity
@Table(name = "examenes_teoricos")
@Data
@EqualsAndHashCode(exclude = "tramite")
@ToString(exclude = "tramite")
public class ExamenTeorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id", nullable = false)
    private Tramite tramite;

    @Min(value = 0, message = "El puntaje no puede ser negativo")
    @Max(value = 100, message = "El puntaje no puede exceder 100")
    @Column(nullable = false)
    private Integer puntaje;

    @Column(nullable = false)
    private Boolean aprobado;

    @NotNull(message = "La fecha del examen es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Min(value = 1, message = "La cantidad de preguntas debe ser mayor a 0")
    @Column(name = "cantidad_preguntas")
    private Integer cantidadPreguntas;

    @Min(value = 0, message = "Las respuestas correctas no pueden ser negativas")
    @Column(name = "respuestas_correctas")
    private Integer respuestasCorrectas;

    @Size(max = 100, message = "El examinador no puede exceder 100 caracteres")
    @Column(length = 100)
    private String examinador;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    /**
     * Calcula si el examen está aprobado según el puntaje mínimo (80%)
     */
    public boolean calcularAprobacion() {
        return puntaje != null && puntaje >= 80;
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
