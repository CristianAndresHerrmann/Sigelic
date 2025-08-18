package com.example.sigelic.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

/**
 * DTO de request para registrar examen teórico
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamenTeoricoRequestDTO {
    
    @NotNull(message = "El trámite es obligatorio")
    private Long tramiteId;
    
    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 0, message = "El puntaje no puede ser negativo")
    @Max(value = 100, message = "El puntaje no puede exceder 100")
    private Integer puntaje;
    
    @Min(value = 1, message = "La cantidad de preguntas debe ser mayor a 0")
    private Integer cantidadPreguntas;
    
    @Min(value = 0, message = "Las respuestas correctas no pueden ser negativas")
    private Integer respuestasCorrectas;
    
    @Size(max = 100, message = "El examinador no puede exceder 100 caracteres")
    private String examinador;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
