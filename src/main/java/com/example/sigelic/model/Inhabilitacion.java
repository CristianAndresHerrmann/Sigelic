package com.example.sigelic.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad que representa una inhabilitación de un titular
 */
@Entity
@Table(name = "inhabilitaciones")
@Data
@EqualsAndHashCode(exclude = "titular")
@ToString(exclude = "titular")
public class Inhabilitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_id", nullable = false)
    private Titular titular;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String motivo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @NotBlank(message = "La autoridad que aplicó la sanción es obligatoria")
    @Size(max = 100, message = "La autoridad no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String autoridad;

    @Size(max = 100, message = "El número de expediente no puede exceder 100 caracteres")
    @Column(name = "numero_expediente", length = 100)
    private String numeroExpediente;

    /**
     * Verifica si la inhabilitación está activa
     */
    public boolean isActiva() {
        LocalDate hoy = LocalDate.now();
        return !fechaInicio.isAfter(hoy) && 
            (fechaFin == null || !fechaFin.isBefore(hoy));
    }

    /**
     * Verifica si la inhabilitación está activa en una fecha específica
     */
    public boolean isActivaEn(LocalDate fecha) {
        return !fechaInicio.isAfter(fecha) && 
            (fechaFin == null || !fechaFin.isBefore(fecha));
    }
}
