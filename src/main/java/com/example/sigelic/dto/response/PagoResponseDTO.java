package com.example.sigelic.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.example.sigelic.model.MedioPago;
import com.example.sigelic.model.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Pago
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoResponseDTO {
    
    private Long id;
    private BigDecimal monto;
    private MedioPago medio;
    private EstadoPago estado;
    private LocalDateTime fecha;
    private LocalDateTime fechaAcreditacion;
    private LocalDateTime fechaVencimiento;
    private String numeroTransaccion;
    private String numeroComprobante;
    private String observaciones;
    private String cajero;
    private boolean acreditado;
    private boolean vencido;
}
