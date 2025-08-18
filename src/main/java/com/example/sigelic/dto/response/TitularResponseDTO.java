package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para Titular
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TitularResponseDTO {
    
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;
    private String domicilio;
    private String email;
    private String telefono;
    private int edad;
    private String nombreCompleto;
    private boolean tieneInhabilitacionesActivas;
    private List<InhabilitacionResponseDTO> inhabilitacionesActivas;
    private List<LicenciaResponseDTO> licenciasVigentes;
}
