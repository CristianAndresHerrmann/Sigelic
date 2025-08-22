package com.example.sigelic.mapper;

import com.example.sigelic.dto.request.ExamenTeoricoRequestDTO;
import com.example.sigelic.dto.response.ExamenTeoricoResponseDTO;
import com.example.sigelic.model.ExamenTeorico;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades ExamenTeorico y DTOs
 */
@Component
public class ExamenTeoricoMapper {

    /**
     * Convierte ExamenTeoricoRequestDTO a entidad ExamenTeorico
     */
    public ExamenTeorico toEntity(ExamenTeoricoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        ExamenTeorico examen = new ExamenTeorico();
        examen.setFecha(dto.getFecha());
        examen.setCantidadPreguntas(dto.getCantidadPreguntas());
        examen.setRespuestasCorrectas(dto.getRespuestasCorrectas());
        examen.setExaminador(dto.getExaminador());
        examen.setObservaciones(dto.getObservaciones());

        return examen;
    }

    /**
     * Convierte entidad ExamenTeorico a ExamenTeoricoResponseDTO
     */
    public ExamenTeoricoResponseDTO toResponseDTO(ExamenTeorico examen) {
        if (examen == null) {
            return null;
        }

        ExamenTeoricoResponseDTO dto = new ExamenTeoricoResponseDTO();
        dto.setId(examen.getId());
        dto.setFecha(examen.getFecha());
        dto.setCantidadPreguntas(examen.getCantidadPreguntas());
        dto.setRespuestasCorrectas(examen.getRespuestasCorrectas());
        dto.setPuntaje(examen.getPuntaje());
        dto.setAprobado(examen.getAprobado());
        dto.setExaminador(examen.getExaminador());
        dto.setObservaciones(examen.getObservaciones());
        
        // Información del trámite
        if (examen.getTramite() != null) {
            dto.setTramiteId(examen.getTramite().getId());
            
            // Información del titular
            if (examen.getTramite().getTitular() != null) {
                dto.setTitularNombre(examen.getTramite().getTitular().getNombre());
                dto.setTitularApellido(examen.getTramite().getTitular().getApellido());
                dto.setTitularDni(examen.getTramite().getTitular().getDni());
            }
        }

        return dto;
    }

    /**
     * Convierte lista de entidades ExamenTeorico a lista de DTOs
     */
    public List<ExamenTeoricoResponseDTO> toResponseDTOList(List<ExamenTeorico> examenes) {
        if (examenes == null) {
            return null;
        }

        return examenes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte lista de DTOs a lista de entidades ExamenTeorico
     */
    public List<ExamenTeorico> toEntityList(List<ExamenTeoricoRequestDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
