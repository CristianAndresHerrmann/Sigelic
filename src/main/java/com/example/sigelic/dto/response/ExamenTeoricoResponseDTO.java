package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para ExamenTeorico
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamenTeoricoResponseDTO {
    
    private Long id;
    private Integer puntaje;
    private Boolean aprobado;
    private LocalDateTime fecha;
    private Integer cantidadPreguntas;
    private Integer respuestasCorrectas;
    private String examinador;
    private String observaciones;
    private boolean vigente;
}
