package com.example.sigelic.model;

import java.time.LocalDate;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad que representa una licencia de conducir
 */
@Entity
@Table(name = "licencias")
@Data
@EqualsAndHashCode(exclude = "titular")
@ToString(exclude = "titular")
public class Licencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_id", nullable = false)
    private Titular titular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaseLicencia clase;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLicencia estado = EstadoLicencia.VIGENTE;

    @Size(max = 20, message = "El número de licencia no puede exceder 20 caracteres")
    @Column(name = "numero_licencia", unique = true, length = 20)
    private String numeroLicencia;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id")
    private Tramite tramite;

    /**
     * Verifica si la licencia está vigente
     */
    public boolean isVigente() {
        return estado == EstadoLicencia.VIGENTE && 
            !fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Verifica si la licencia está vencida
     */
    public boolean isVencida() {
        return fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Calcula los días restantes hasta el vencimiento
     */
    public long getDiasHastaVencimiento() {
        return LocalDate.now().until(fechaVencimiento).getDays();
    }

    /**
     * Calcula la vigencia en años según la edad del titular
     */
    public static int calcularVigenciaEnAnios(int edad, boolean esPrimeraVez) {
        if (edad < 21) {
            return esPrimeraVez ? 1 : 3;
        } else if (edad <= 46) {
            return 5;
        } else if (edad <= 60) {
            return 4;
        } else if (edad <= 70) {
            return 3;
        } else {
            return 1;
        }
    }

    /**
     * Calcula la fecha de vencimiento ajustada al día/mes de nacimiento
     */
    public static LocalDate calcularFechaVencimiento(LocalDate fechaNacimiento, LocalDate fechaEmision, int vigenciaAnios) {
        LocalDate fechaVencimiento = fechaEmision.plusYears(vigenciaAnios);
        
        // Ajustar al día/mes de nacimiento
        LocalDate fechaAjustada = fechaVencimiento.withMonth(fechaNacimiento.getMonthValue())
                                                .withDayOfMonth(fechaNacimiento.getDayOfMonth());
        
        // Si ya pasó en el año actual, ir al siguiente año
        if (fechaAjustada.isBefore(fechaEmision) || fechaAjustada.equals(fechaEmision)) {
            fechaAjustada = fechaAjustada.plusYears(1);
        }
        
        return fechaAjustada;
    }
}
