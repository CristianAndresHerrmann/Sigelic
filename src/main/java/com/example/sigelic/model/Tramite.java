package com.example.sigelic.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad que representa un trámite de licencia de conducir
 */
@Entity
@Table(name = "tramites")
@Data
@EqualsAndHashCode(exclude = {"titular", "exameneseoricos", "examenespracticos", "aptosMedicos", "pagos", "turnos"})
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_id", nullable = false)
    private Titular titular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTramite tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTramite estado = EstadoTramite.INICIADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "clase_solicitada", nullable = false, length = 20)
    private ClaseLicencia claseSolicitada;

    @Column(name = "documentacion_validada")
    private Boolean documentacionValidada = false;

    @Column(name = "apto_medico_vigente")
    private Boolean aptoMedicoVigente = false;

    @Column(name = "examen_teorico_aprobado")
    private Boolean examenTeoricoAprobado = false;

    @Column(name = "examen_practico_aprobado")
    private Boolean examenPracticoAprobado = false;

    @Column(name = "pago_acreditado")
    private Boolean pagoAcreditado = false;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @Size(max = 100, message = "El agente responsable no puede exceder 100 caracteres")
    @Column(name = "agente_responsable", length = 100)
    private String agenteResponsable;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamenTeorico> exameneseoricos = new ArrayList<>();

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamenPractico> examenespracticos = new ArrayList<>();

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AptoMedico> aptosMedicos = new ArrayList<>();

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pago> pagos = new ArrayList<>();

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Turno> turnos = new ArrayList<>();

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Licencia> licencias = new ArrayList<>();

    /**
     * Verifica si el trámite requiere examen teórico
     */
    public boolean requiereExamenTeorico() {
        return tipo == TipoTramite.EMISION || tipo == TipoTramite.RENOVACION;
    }

    /**
     * Verifica si el trámite requiere examen práctico
     */
    public boolean requiereExamenPractico() {
        return tipo == TipoTramite.EMISION || tipo == TipoTramite.RENOVACION;
    }

    /**
     * Verifica si el trámite requiere apto médico
     */
    public boolean requiereAptoMedico() {
        return tipo != TipoTramite.DUPLICADO && tipo != TipoTramite.CAMBIO_DOMICILIO;
    }

    /**
     * Verifica si todos los requisitos están cumplidos
     */
    public boolean todosLosRequisitosCumplidos() {
        boolean cumple = documentacionValidada && pagoAcreditado;
        
        if (requiereAptoMedico()) {
            cumple = cumple && aptoMedicoVigente;
        }
        
        if (requiereExamenTeorico()) {
            cumple = cumple && examenTeoricoAprobado;
        }
        
        if (requiereExamenPractico()) {
            cumple = cumple && examenPracticoAprobado;
        }
        
        return cumple;
    }

    /**
     * Actualiza el estado del trámite según los requisitos cumplidos
     */
    public void actualizarEstado() {
        if (estado.esFinal()) {
            return; // No cambiar estados finales
        }

        if (todosLosRequisitosCumplidos()) {
            estado = EstadoTramite.PAGO_OK;
        } else if (pagoAcreditado) {
            estado = EstadoTramite.PAGO_OK;
        } else if (examenPracticoAprobado) {
            estado = EstadoTramite.EX_PRA_OK;
        } else if (examenTeoricoAprobado) {
            estado = EstadoTramite.EX_TEO_OK;
        } else if (aptoMedicoVigente) {
            estado = EstadoTramite.APTO_MED;
        } else if (documentacionValidada) {
            estado = EstadoTramite.DOCS_OK;
        }
    }

    /**
     * Verifica si el trámite puede continuar o está rechazado
     */
    public boolean puedeAvanzar() {
        return estado.puedeAvanzar();
    }

    /**
     * Verifica si el trámite permite reintentos en su estado actual
     */
    public boolean permiteReintento() {
        return estado.permiteReintento();
    }

    /**
     * Rechaza el examen teórico y cambia el estado
     */
    public void rechazarExamenTeorico() {
        this.examenTeoricoAprobado = false;
        this.estado = EstadoTramite.EX_TEO_RECHAZADO;
    }

    /**
     * Rechaza el examen práctico y cambia el estado
     */
    public void rechazarExamenPractico() {
        this.examenPracticoAprobado = false;
        this.estado = EstadoTramite.EX_PRA_RECHAZADO;
    }

    /**
     * Rechaza la documentación y cambia el estado
     */
    public void rechazarDocumentacion() {
        this.documentacionValidada = false;
        this.estado = EstadoTramite.DOCS_RECHAZADAS;
    }

    /**
     * Permite reintentar desde un estado de rechazo de examen
     */
    public void permitirReintento() {
        if (estado == EstadoTramite.EX_TEO_RECHAZADO) {
            // Volver al estado anterior (APTO_MED o el que corresponda)
            if (aptoMedicoVigente) {
                estado = EstadoTramite.APTO_MED;
            } else if (documentacionValidada) {
                estado = EstadoTramite.DOCS_OK;
            }
        } else if (estado == EstadoTramite.EX_PRA_RECHAZADO) {
            // Volver al estado EX_TEO_OK
            estado = EstadoTramite.EX_TEO_OK;
        }
    }
}
