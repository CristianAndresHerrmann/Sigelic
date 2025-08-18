package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un recurso disponible para turnos
 */
@Entity
@Table(name = "recursos")
@Data
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del recurso es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecurso tipo;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Column(nullable = false)
    private Integer capacidad = 1;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Min(value = 1, message = "La duración mínima del turno debe ser mayor a 0")
    @Column(name = "duracion_turno_minutos")
    private Integer duracionTurnoMinutos = 30;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    @Column(length = 100)
    private String ubicacion;

    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Turno> turnos = new ArrayList<>();

    /**
     * Verifica si el recurso está disponible en un horario específico
     */
    public boolean isDisponibleEn(LocalTime hora) {
        if (!activo) {
            return false;
        }
        
        if (horaInicio != null && horaFin != null) {
            return !hora.isBefore(horaInicio) && !hora.isAfter(horaFin);
        }
        
        return true;
    }

    /**
     * Calcula la cantidad de turnos que se pueden dar en un día
     */
    public int getTurnosPorDia() {
        if (horaInicio == null || horaFin == null || duracionTurnoMinutos == null) {
            return 0;
        }
        
        int minutosDisponibles = (int) java.time.Duration.between(horaInicio, horaFin).toMinutes();
        return (minutosDisponibles / duracionTurnoMinutos) * capacidad;
    }
}
