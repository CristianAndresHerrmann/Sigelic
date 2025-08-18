package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para AptoMedico
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AptoMedicoResponseDTO {
    
    private Long id;
    private String profesional;
    private String matriculaProfesional;
    private Boolean apto;
    private LocalDateTime fecha;
    private LocalDate fechaVencimiento;
    private String observaciones;
    private String restricciones;
    private Double presionSistolica;
    private Double presionDiastolica;
    private String agudezaVisual;
    private boolean vigente;
}
