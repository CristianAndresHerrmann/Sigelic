package com.example.sigelic.mapper;

import com.example.sigelic.dto.request.TramiteRequestDTO;
import com.example.sigelic.dto.response.TramiteResponseDTO;
import com.example.sigelic.dto.response.TitularResponseDTO;
import com.example.sigelic.dto.response.CostoTramiteResponseDTO;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.CostoTramite;
import com.example.sigelic.model.EstadoTramite;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Tramite y DTOs
 */
@Component
public class TramiteMapper {

    /**
     * Convierte TramiteRequestDTO a entidad Tramite
     */
    public Tramite toEntity(TramiteRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Tramite tramite = new Tramite();
        tramite.setTipo(dto.getTipo());
        tramite.setClaseSolicitada(dto.getClaseSolicitada());
        tramite.setEstado(EstadoTramite.INICIADO); // Estado inicial
        tramite.setObservaciones(dto.getObservaciones());

        return tramite;
    }

    /**
     * Actualiza una entidad Tramite con datos del DTO
     */
    public void updateEntity(Tramite tramite, TramiteRequestDTO dto) {
        if (tramite == null || dto == null) {
            return;
        }

        tramite.setTipo(dto.getTipo());
        tramite.setClaseSolicitada(dto.getClaseSolicitada());
        tramite.setObservaciones(dto.getObservaciones());
    }

    /**
     * Convierte entidad Tramite a TramiteResponseDTO
     */
    public TramiteResponseDTO toResponseDTO(Tramite tramite) {
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
                .build();
    }

    /**
     * Convierte entidad Tramite a TramiteResponseDTO con detalles completos
     */
    public TramiteResponseDTO toResponseDTOWithDetails(Tramite tramite) {
        if (tramite == null) {
            return null;
        }

        TramiteResponseDTO dto = toResponseDTO(tramite);
        
        // Agregar campo calculado
        dto.setTodosLosRequisitosCumplidos(tramite.todosLosRequisitosCumplidos());
        
        // Titular
        if (tramite.getTitular() != null) {
            dto.setTitular(toTitularDTO(tramite.getTitular()));
        }

        return dto;
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<TramiteResponseDTO> toResponseDTOList(List<Tramite> tramites) {
        if (tramites == null) {
            return List.of();
        }

        return tramites.stream()
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
}
