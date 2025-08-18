package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.example.sigelic.model.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para Tramite
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TramiteResponseDTO {
    
    private Long id;
    private TipoTramite tipo;
    private EstadoTramite estado;
    private ClaseLicencia claseSolicitada;
    private Boolean documentacionValidada;
    private Boolean aptoMedicoVigente;
    private Boolean examenTeoricoAprobado;
    private Boolean examenPracticoAprobado;
    private Boolean pagoAcreditado;
    private String observaciones;
    private String agenteResponsable;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private boolean todosLosRequisitosCumplidos;
    
    // Referencias a otras entidades
    private TitularResponseDTO titular;
    private List<ExamenTeoricoResponseDTO> exameneseoricos;
    private List<ExamenPracticoResponseDTO> examenespracticos;
    private List<AptoMedicoResponseDTO> aptosMedicos;
    private List<PagoResponseDTO> pagos;
}
