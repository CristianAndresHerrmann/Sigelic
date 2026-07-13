package com.example.sigelic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un turno para realizar trámites
 */
@Entity
@Table(name = "turnos")
@Data
@ToString(exclude = {"titular", "tramite"})
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_id", nullable = false)
    private Titular titular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id")
    private Tramite tramite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTurno tipo;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @Column(nullable = false)
    private LocalDateTime inicio;

    @NotNull(message = "La fecha y hora de fin son obligatorias")
    @Column(nullable = false)
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", length = 20)
    private TipoRecurso tipoRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id")
    private Recurso recurso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estado = EstadoTurno.RESERVADO;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @Size(max = 100, message = "El profesional asignado no puede exceder 100 caracteres")
    @Column(name = "profesional_asignado", length = 100)
    private String profesionalAsignado;

    @CreationTimestamp
    @Column(name = "fecha_reserva", nullable = false, updatable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "fecha_completion")
    private LocalDateTime fechaCompletion;

    /**
     * La identidad de una entidad persistida depende solamente de su ID.
     * Evita que Vaadin inicialice asociaciones LAZY al calcular la clave de
     * una fila y que campos mutables (por ejemplo, el estado) cambien el hash.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Turno turno)) {
            return false;
        }
        return id != null && id.equals(turno.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    /**
     * Verifica si el turno está activo (no cancelado ni completado)
     */
    public boolean isActivo() {
        return estado != EstadoTurno.CANCELADO && estado != EstadoTurno.COMPLETADO;
    }

    /**
     * Verifica si el turno se solapa con otro turno
     */
    public boolean seSolapaCon(Turno otroTurno) {
        if (otroTurno == null || !otroTurno.isActivo() || !this.isActivo()) {
            return false;
        }
        
        return !(this.fin.isBefore(otroTurno.inicio) || this.fin.equals(otroTurno.inicio) ||
                 this.inicio.isAfter(otroTurno.fin) || this.inicio.equals(otroTurno.fin));
    }

    /**
     * Verifica si el turno está en el pasado
     */
    public boolean isVencido() {
        return fin.isBefore(LocalDateTime.now());
    }

    /**
     * Confirma el turno
     */
    public void confirmar() {
        if (estado == EstadoTurno.RESERVADO) {
            estado = EstadoTurno.CONFIRMADO;
            fechaConfirmacion = LocalDateTime.now();
        }
    }

    /**
     * Completa el turno
     */
    public void completar() {
        if (estado == EstadoTurno.CONFIRMADO || estado == EstadoTurno.RESERVADO) {
            estado = EstadoTurno.COMPLETADO;
            fechaCompletion = LocalDateTime.now();
        }
    }

    /**
     * Cancela el turno
     */
    public void cancelar(String motivo) {
        estado = EstadoTurno.CANCELADO;
        observaciones = motivo;
    }

    /**
     * Marca al titular como ausente
     */
    public void marcarAusente() {
        if (estado == EstadoTurno.CONFIRMADO || estado == EstadoTurno.RESERVADO) {
            estado = EstadoTurno.AUSENTE;
        }
    }

    /**
     * Calcula la duración del turno en minutos
     */
    public long getDuracionEnMinutos() {
        return java.time.Duration.between(inicio, fin).toMinutes();
    }
}
