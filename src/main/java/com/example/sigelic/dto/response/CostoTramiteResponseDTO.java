package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.ClaseLicencia;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de response para costos de trámites
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CostoTramiteResponseDTO {
    
    private Long id;
    private TipoTramite tipoTramite;
    private ClaseLicencia claseLicencia;
    private BigDecimal costo;
    private LocalDate fechaVigenciaDesde;
    private LocalDate fechaVigenciaHasta;
    private Boolean vigente;
}
