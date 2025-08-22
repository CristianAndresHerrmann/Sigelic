package com.example.sigelic.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    // Información del trámite asociado
    private Long tramiteId;
    
    // Información del titular
    private String titularNombre;
    private String titularApellido;
    private String titularDni;
}
