package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para ExamenPractico
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamenPracticoResponseDTO {
    
    private Long id;
    private Integer faltasLeves;
    private Integer faltasGraves;
    private Boolean aprobado;
    private LocalDateTime fecha;
    private String examinador;
    private String vehiculoUtilizado;
    private String pistaUtilizada;
    private String observaciones;
    private boolean vigente;
}
