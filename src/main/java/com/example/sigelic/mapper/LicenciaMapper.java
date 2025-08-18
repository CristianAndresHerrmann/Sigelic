package com.example.sigelic.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.sigelic.dto.response.LicenciaResponseDTO;
import com.example.sigelic.dto.response.TitularResponseDTO;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.Titular;

/**
 * Mapper para convertir entre entidades Licencia y DTOs
 * Note: Las licencias no se crean directamente via API, sino que se generan
 * automáticamente cuando se completa exitosamente un trámite de licencia
 */
@Component
public class LicenciaMapper {

    /**
     * Convierte entidad Licencia a LicenciaResponseDTO
     */
    public LicenciaResponseDTO toResponseDTO(Licencia licencia) {
        if (licencia == null) {
            return null;
        }

        return LicenciaResponseDTO.builder()
                .id(licencia.getId())
                .clase(licencia.getClase())
                .fechaEmision(licencia.getFechaEmision())
                .fechaVencimiento(licencia.getFechaVencimiento())
                .estado(licencia.getEstado())
                .numeroLicencia(licencia.getNumeroLicencia())
                .observaciones(licencia.getObservaciones())
                .vigente(licencia.isVigente())
                .vencida(licencia.isVencida())
                .diasHastaVencimiento(licencia.getDiasHastaVencimiento())
                .build();
    }

    /**
     * Convierte entidad Licencia a LicenciaResponseDTO con detalles completos
     */
    public LicenciaResponseDTO toResponseDTOWithDetails(Licencia licencia) {
        if (licencia == null) {
            return null;
        }

        LicenciaResponseDTO dto = toResponseDTO(licencia);
        
        // Titular - usar campos individuales
        if (licencia.getTitular() != null) {
            dto.setTitularNombre(licencia.getTitular().getNombre() + " " + licencia.getTitular().getApellido());
            dto.setTitularDni(licencia.getTitular().getDni());
        }

        return dto;
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<LicenciaResponseDTO> toResponseDTOList(List<Licencia> licencias) {
        if (licencias == null) {
            return List.of();
        }

        return licencias.stream()
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
