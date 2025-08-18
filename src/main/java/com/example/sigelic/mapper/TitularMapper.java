package com.example.sigelic.mapper;

import com.example.sigelic.dto.request.TitularRequestDTO;
import com.example.sigelic.dto.response.TitularResponseDTO;
import com.example.sigelic.dto.response.InhabilitacionResponseDTO;
import com.example.sigelic.dto.response.LicenciaResponseDTO;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Inhabilitacion;
import com.example.sigelic.model.Licencia;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Titular y DTOs
 */
@Component
public class TitularMapper {

    /**
     * Convierte TitularRequestDTO a entidad Titular
     */
    public Titular toEntity(TitularRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Titular titular = new Titular();
        titular.setNombre(dto.getNombre());
        titular.setApellido(dto.getApellido());
        titular.setDni(dto.getDni());
        titular.setFechaNacimiento(dto.getFechaNacimiento());
        titular.setDomicilio(dto.getDomicilio());
        titular.setEmail(dto.getEmail());
        titular.setTelefono(dto.getTelefono());

        return titular;
    }

    /**
     * Actualiza una entidad Titular con datos del DTO
     */
    public void updateEntity(Titular titular, TitularRequestDTO dto) {
        if (titular == null || dto == null) {
            return;
        }

        titular.setNombre(dto.getNombre());
        titular.setApellido(dto.getApellido());
        titular.setDni(dto.getDni());
        titular.setFechaNacimiento(dto.getFechaNacimiento());
        titular.setDomicilio(dto.getDomicilio());
        titular.setEmail(dto.getEmail());
        titular.setTelefono(dto.getTelefono());
    }

    /**
     * Convierte entidad Titular a TitularResponseDTO
     */
    public TitularResponseDTO toResponseDTO(Titular titular) {
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

    /**
     * Convierte entidad Titular a TitularResponseDTO con detalles completos
     */
    public TitularResponseDTO toResponseDTOWithDetails(Titular titular) {
        if (titular == null) {
            return null;
        }

        TitularResponseDTO dto = toResponseDTO(titular);
        
        // Inhabilitaciones activas
        List<InhabilitacionResponseDTO> inhabilitacionesActivas = titular.getInhabilitaciones()
                .stream()
                .filter(Inhabilitacion::isActiva)
                .map(this::toInhabilitacionDTO)
                .collect(Collectors.toList());
        
        // Licencias vigentes
        List<LicenciaResponseDTO> licenciasVigentes = titular.getLicencias()
                .stream()
                .filter(Licencia::isVigente)
                .map(this::toLicenciaDTO)
                .collect(Collectors.toList());

        dto.setInhabilitacionesActivas(inhabilitacionesActivas);
        dto.setLicenciasVigentes(licenciasVigentes);

        return dto;
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<TitularResponseDTO> toResponseDTOList(List<Titular> titulares) {
        if (titulares == null) {
            return List.of();
        }

        return titulares.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private InhabilitacionResponseDTO toInhabilitacionDTO(Inhabilitacion inhabilitacion) {
        if (inhabilitacion == null) {
            return null;
        }

        return InhabilitacionResponseDTO.builder()
                .id(inhabilitacion.getId())
                .motivo(inhabilitacion.getMotivo())
                .fechaInicio(inhabilitacion.getFechaInicio())
                .fechaFin(inhabilitacion.getFechaFin())
                .autoridad(inhabilitacion.getAutoridad())
                .numeroExpediente(inhabilitacion.getNumeroExpediente())
                .activa(inhabilitacion.isActiva())
                .build();
    }

    private LicenciaResponseDTO toLicenciaDTO(Licencia licencia) {
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
}
