package com.example.sigelic.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

/**
 * DTO de request para registrar examen práctico
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamenPracticoRequestDTO {
    
    @NotNull(message = "El trámite es obligatorio")
    private Long tramiteId;
    
    @NotNull(message = "Las faltas leves son obligatorias")
    @Min(value = 0, message = "Las faltas leves no pueden ser negativas")
    private Integer faltasLeves;
    
    @NotNull(message = "Las faltas graves son obligatorias")
    @Min(value = 0, message = "Las faltas graves no pueden ser negativas")
    private Integer faltasGraves;
    
    @Size(max = 100, message = "El examinador no puede exceder 100 caracteres")
    private String examinador;
    
    @Size(max = 50, message = "El vehículo utilizado no puede exceder 50 caracteres")
    private String vehiculoUtilizado;
    
    @Size(max = 50, message = "La pista utilizada no puede exceder 50 caracteres")
    private String pistaUtilizada;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
