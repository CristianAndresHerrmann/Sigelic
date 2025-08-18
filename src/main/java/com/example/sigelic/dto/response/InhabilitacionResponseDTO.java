package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de respuesta para Inhabilitacion
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InhabilitacionResponseDTO {
    
    private Long id;
    private String motivo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String autoridad;
    private String numeroExpediente;
    private boolean activa;
}
