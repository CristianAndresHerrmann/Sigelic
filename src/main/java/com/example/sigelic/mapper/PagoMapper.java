package com.example.sigelic.mapper;

import com.example.sigelic.dto.request.PagoRequestDTO;
import com.example.sigelic.dto.response.PagoResponseDTO;
import com.example.sigelic.dto.response.TitularResponseDTO;
import com.example.sigelic.dto.response.TramiteResponseDTO;
import com.example.sigelic.model.Pago;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Pago y DTOs
 */
@Component
public class PagoMapper {

    /**
     * Convierte PagoRequestDTO a entidad Pago
     */
    public Pago toEntity(PagoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Pago pago = new Pago();
        pago.setMonto(dto.getMonto());
        pago.setMedio(dto.getMedio());
        pago.setNumeroComprobante(dto.getNumeroComprobante());
        pago.setCajero(dto.getCajero());
        pago.setObservaciones(dto.getObservaciones());

        return pago;
    }

    /**
     * Actualiza una entidad Pago con datos del DTO
     */
    public void updateEntity(Pago pago, PagoRequestDTO dto) {
        if (pago == null || dto == null) {
            return;
        }

        pago.setMonto(dto.getMonto());
        pago.setMedio(dto.getMedio());
        pago.setNumeroComprobante(dto.getNumeroComprobante());
        pago.setCajero(dto.getCajero());
        pago.setObservaciones(dto.getObservaciones());
    }

    /**
     * Convierte entidad Pago a PagoResponseDTO
     */
    public PagoResponseDTO toResponseDTO(Pago pago) {
        if (pago == null) {
            return null;
        }

        return PagoResponseDTO.builder()
                .id(pago.getId())
                .monto(pago.getMonto())
                .medio(pago.getMedio())
                .estado(pago.getEstado())
                .fecha(pago.getFecha())
                .fechaAcreditacion(pago.getFechaAcreditacion())
                .fechaVencimiento(pago.getFechaVencimiento())
                .numeroTransaccion(pago.getNumeroTransaccion())
                .numeroComprobante(pago.getNumeroComprobante())
                .observaciones(pago.getObservaciones())
                .cajero(pago.getCajero())
                .acreditado(pago.isAcreditado())
                .vencido(pago.isVencido())
                .build();
    }

    /**
     * Convierte entidad Pago a PagoResponseDTO con detalles completos
     */
    public PagoResponseDTO toResponseDTOWithDetails(Pago pago) {
        if (pago == null) {
            return null;
        }

        PagoResponseDTO dto = toResponseDTO(pago);

        return dto;
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<PagoResponseDTO> toResponseDTOList(List<Pago> pagos) {
        if (pagos == null) {
            return List.of();
        }

        return pagos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
