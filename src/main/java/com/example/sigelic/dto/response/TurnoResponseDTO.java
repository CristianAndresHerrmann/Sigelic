package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.example.sigelic.model.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Turno
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TurnoResponseDTO {
    
    private Long id;
    private TipoTurno tipo;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private TipoRecurso tipoRecurso;
    private String recursoNombre;
    private EstadoTurno estado;
    private String observaciones;
    private String profesionalAsignado;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaCompletion;
    private long duracionEnMinutos;
    private boolean activo;
    private boolean vencido;
    
    // Referencias reducidas
    private String titularNombre;
    private String titularDni;
    private Long tramiteId;
}
