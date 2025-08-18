package com.example.sigelic.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.ClaseLicencia;

/**
 * DTO de request para iniciar un trámite
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TramiteRequestDTO {
    
    @NotNull(message = "El titular es obligatorio")
    private Long titularId;
    
    @NotNull(message = "El tipo de trámite es obligatorio")
    private TipoTramite tipo;
    
    @NotNull(message = "La clase de licencia solicitada es obligatoria")
    private ClaseLicencia claseSolicitada;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
