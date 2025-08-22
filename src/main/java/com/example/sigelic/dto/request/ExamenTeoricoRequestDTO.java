package com.example.sigelic.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para registrar examen teórico
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamenTeoricoRequestDTO {
    
    @NotNull(message = "El trámite es obligatorio")
    private Long tramiteId;
    
    @NotNull(message = "La fecha del examen es obligatoria")
    private LocalDateTime fecha;
    
    @NotNull(message = "La cantidad de preguntas es obligatoria")
    @Min(value = 1, message = "La cantidad de preguntas debe ser mayor a 0")
    private Integer cantidadPreguntas;
    
    @NotNull(message = "Las respuestas correctas son obligatorias")
    @Min(value = 0, message = "Las respuestas correctas no pueden ser negativas")
    private Integer respuestasCorrectas;
    
    @NotBlank(message = "El examinador es obligatorio")
    @Size(max = 100, message = "El examinador no puede exceder 100 caracteres")
    private String examinador;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
