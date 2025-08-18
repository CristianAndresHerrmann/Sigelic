package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.EstadoLicencia;

import java.time.LocalDate;

/**
 * DTO de respuesta para Licencia
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LicenciaResponseDTO {
    
    private Long id;
    private ClaseLicencia clase;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private EstadoLicencia estado;
    private String numeroLicencia;
    private String observaciones;
    private boolean vigente;
    private boolean vencida;
    private long diasHastaVencimiento;
    private String titularNombre;
    private String titularDni;
}
