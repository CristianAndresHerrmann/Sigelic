package com.example.sigelic.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.sigelic.dto.request.TurnoRequestDTO;
import com.example.sigelic.dto.response.TitularResponseDTO;
import com.example.sigelic.dto.response.TramiteResponseDTO;
import com.example.sigelic.dto.response.TurnoResponseDTO;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.model.Turno;

/**
 * Mapper para convertir entre entidades Turno y DTOs
 */
@Component
public class TurnoMapper {

    /**
     * Convierte TurnoRequestDTO a entidad Turno
     */
    public Turno toEntity(TurnoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Turno turno = new Turno();
        turno.setTipo(dto.getTipo());
        turno.setInicio(dto.getInicio());
        turno.setFin(dto.getFin());
        turno.setObservaciones(dto.getObservaciones());

        return turno;
    }

    /**
     * Actualiza una entidad Turno con datos del DTO
     */
    public void updateEntity(Turno turno, TurnoRequestDTO dto) {
        if (turno == null || dto == null) {
            return;
        }

        turno.setTipo(dto.getTipo());
        turno.setInicio(dto.getInicio());
        turno.setFin(dto.getFin());
        turno.setObservaciones(dto.getObservaciones());
    }

    /**
     * Convierte entidad Turno a TurnoResponseDTO
     */
    public TurnoResponseDTO toResponseDTO(Turno turno) {
        if (turno == null) {
            return null;
        }

        return TurnoResponseDTO.builder()
                .id(turno.getId())
                .tipo(turno.getTipo())
                .inicio(turno.getInicio())
                .fin(turno.getFin())
                .estado(turno.getEstado())
                .observaciones(turno.getObservaciones())
                .profesionalAsignado(turno.getProfesionalAsignado())
                .fechaReserva(turno.getFechaReserva())
                .fechaConfirmacion(turno.getFechaConfirmacion())
                .fechaCompletion(turno.getFechaCompletion())
                .activo(turno.isActivo())
                .vencido(turno.isVencido())
                .duracionEnMinutos(turno.getDuracionEnMinutos())
                .build();
    }

    /**
     * Convierte entidad Turno a TurnoResponseDTO con detalles completos
     */
    public TurnoResponseDTO toResponseDTOWithDetails(Turno turno) {
        if (turno == null) {
            return null;
        }

        TurnoResponseDTO dto = toResponseDTO(turno);
        
        // Titular - usar campos individuales
        if (turno.getTitular() != null) {
            dto.setTitularNombre(turno.getTitular().getNombre() + " " + turno.getTitular().getApellido());
            dto.setTitularDni(turno.getTitular().getDni());
        }
        
        // Tramite - usar campo ID
        if (turno.getTramite() != null) {
            dto.setTramiteId(turno.getTramite().getId());
        }

        return dto;
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<TurnoResponseDTO> toResponseDTOList(List<Turno> turnos) {
        if (turnos == null) {
            return List.of();
        }

        return turnos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private TitularResponseDTO toTitularDTO(Titular titular) {
        if (titular == null) {
            return null;
        }

        return TitularResponseDTO.builder()
                .id(titular.getId())
                .nombre(titular.getNombre())
                .apellido(titular.getApellido())
                .dni(titular.getDni())
                .fechaNacimiento(titular.getFechaNacimiento())
                .domicilio(titular.getDomicilio())
                .email(titular.getEmail())
                .telefono(titular.getTelefono())
                .edad(titular.getEdad())
                .nombreCompleto(titular.getNombreCompleto())
                .tieneInhabilitacionesActivas(titular.tieneInhabilitacionesActivas())
                .build();
    }

    private TramiteResponseDTO toTramiteDTO(Tramite tramite) {
        if (tramite == null) {
            return null;
        }

        return TramiteResponseDTO.builder()
                .id(tramite.getId())
                .tipo(tramite.getTipo())
                .estado(tramite.getEstado())
                .claseSolicitada(tramite.getClaseSolicitada())
                .fechaCreacion(tramite.getFechaCreacion())
                .fechaActualizacion(tramite.getFechaActualizacion())
                .observaciones(tramite.getObservaciones())
                .documentacionValidada(tramite.getDocumentacionValidada())
                .aptoMedicoVigente(tramite.getAptoMedicoVigente())
                .examenTeoricoAprobado(tramite.getExamenTeoricoAprobado())
                .examenPracticoAprobado(tramite.getExamenPracticoAprobado())
                .pagoAcreditado(tramite.getPagoAcreditado())
                .agenteResponsable(tramite.getAgenteResponsable())
                .todosLosRequisitosCumplidos(tramite.todosLosRequisitosCumplidos())
                .build();
    }
}
