package com.example.sigelic.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import com.example.sigelic.model.MedioPago;

import java.math.BigDecimal;

/**
 * DTO de request para crear orden de pago
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoRequestDTO {
    
    @NotNull(message = "El trámite es obligatorio")
    private Long tramiteId;
    
    @NotNull(message = "El medio de pago es obligatorio")
    private MedioPago medio;
    
    // Para pagos manuales
    private BigDecimal monto;
    private String numeroComprobante;
    private String cajero;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
