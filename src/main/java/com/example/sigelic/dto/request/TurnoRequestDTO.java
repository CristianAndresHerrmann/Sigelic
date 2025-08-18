package com.example.sigelic.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import com.example.sigelic.model.TipoTurno;

import java.time.LocalDateTime;

/**
 * DTO de request para reservar un turno
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnoRequestDTO {
    
    @NotNull(message = "El titular es obligatorio")
    private Long titularId;
    
    @NotNull(message = "El tipo de turno es obligatorio")
    private TipoTurno tipo;
    
    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @Future(message = "La fecha de inicio debe ser en el futuro")
    private LocalDateTime inicio;
    
    @NotNull(message = "La fecha y hora de fin son obligatorias")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private LocalDateTime fin;
    
    @NotNull(message = "El recurso es obligatorio")
    private Long recursoId;
    
    private Long tramiteId;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
    
    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isFinPosteriorAInicio() {
        return fin == null || inicio == null || fin.isAfter(inicio);
    }
}
