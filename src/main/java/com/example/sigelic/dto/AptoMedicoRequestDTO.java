package com.example.sigelic.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para solicitud de registro de apto médico
 */
@Data
public class AptoMedicoRequestDTO {

    @NotBlank(message = "El médico examinador es obligatorio")
    private String medicoExaminador;

    @NotNull(message = "La fecha del examen es obligatoria")
    private LocalDateTime fechaExamen;

    @NotNull(message = "El resultado del examen es obligatorio")
    private Boolean apto;

    // Datos del examen físico
    @DecimalMin(value = "80.0", message = "La presión sistólica debe ser mayor a 80")
    @DecimalMax(value = "200.0", message = "La presión sistólica debe ser menor a 200")
    private Double presionSistolica;

    @DecimalMin(value = "50.0", message = "La presión diastólica debe ser mayor a 50")
    @DecimalMax(value = "120.0", message = "La presión diastólica debe ser menor a 120")
    private Double presionDiastolica;

    // Examen visual
    @NotNull(message = "La agudeza visual del ojo derecho es obligatoria")
    @DecimalMin(value = "0.1", message = "La agudeza visual debe ser mayor a 0.1")
    @DecimalMax(value = "2.0", message = "La agudeza visual debe ser menor a 2.0")
    private Double agudezaVisualOjoDerecho;

    @NotNull(message = "La agudeza visual del ojo izquierdo es obligatoria")
    @DecimalMin(value = "0.1", message = "La agudeza visual debe ser mayor a 0.1")
    @DecimalMax(value = "2.0", message = "La agudeza visual debe ser menor a 2.0")
    private Double agudezaVisualOjoIzquierdo;

    @NotNull(message = "El campo visual es obligatorio")
    private Boolean campoVisualNormal;

    @NotNull(message = "La visión cromática es obligatoria")
    private Boolean visionCromaticaNormal;

    // Examen auditivo
    @NotNull(message = "La audición es obligatoria")
    private Boolean audicionNormal;

    // Examen neurológico
    @NotNull(message = "Los reflejos son obligatorios")
    private Boolean reflejosNormales;

    @NotNull(message = "La coordinación es obligatoria")
    private Boolean coordinacionNormal;

    @NotNull(message = "El equilibrio es obligatorio")
    private Boolean equilibrioNormal;

    // Examen cardiovascular
    @NotNull(message = "El examen cardiovascular es obligatorio")
    private Boolean cardiovascularNormal;

    // Examen del sistema locomotor
    @NotNull(message = "El sistema locomotor es obligatorio")
    private Boolean sistemaLocomotorNormal;

    // Observaciones médicas
    private String observaciones;

    // Restricciones aplicables
    private String restricciones;

    // Validez del apto (en meses)
    @NotNull(message = "Los meses de validez son obligatorios")
    @DecimalMin(value = "1", message = "La validez debe ser de al menos 1 mes")
    @DecimalMax(value = "60", message = "La validez no puede superar los 60 meses")
    private Integer mesesValidez;
}
