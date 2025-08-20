package com.example.sigelic.service;

import com.example.sigelic.model.*;
import com.example.sigelic.repository.PagoRepository;
import com.example.sigelic.repository.CostoTramiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar pagos de trámites
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CostoTramiteRepository costoTramiteRepository;
    private final ConfiguracionService configuracionService;

    /**
     * Busca un pago por ID
     */
    @Transactional(readOnly = true)
    public Optional<Pago> findById(Long id) {
        return pagoRepository.findById(id);
    }

    /**
     * Obtiene todos los pagos
     */
    @Transactional(readOnly = true)
    public List<Pago> findAll() {
        return pagoRepository.findAllWithDetails();
    }

    /**
     * Obtiene pagos por trámite
     */
    @Transactional(readOnly = true)
    public List<Pago> findByTramite(Tramite tramite) {
        return pagoRepository.findByTramite(tramite);
    }

    /**
     * Obtiene pagos por estado
     */
    @Transactional(readOnly = true)
    public List<Pago> findByEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado);
    }

    /**
     * Crea una orden de pago para un trámite
     */
    public Pago crearOrdenPago(Tramite tramite, MedioPago medio) {
        // Verificar que no exista una orden de pago pendiente
        Optional<Pago> pagoExistente = pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.PENDIENTE);
        if (pagoExistente.isPresent()) {
            throw new IllegalStateException("Ya existe una orden de pago pendiente para este trámite");
        }

        // Obtener el costo del trámite
        BigDecimal monto = obtenerCostoTramite(tramite.getTipo(), tramite.getClaseSolicitada());

        Pago pago = new Pago();
        pago.setTramite(tramite);
        pago.setMonto(monto);
        pago.setMedio(medio);
        pago.setEstado(EstadoPago.PENDIENTE);

        // Establecer fecha de vencimiento según configuración
        pago.setFechaVencimiento(calcularFechaVencimiento());

        // Generar número de transacción
        pago.setNumeroTransaccion(generarNumeroTransaccion());

        log.info("Creando orden de pago para trámite ID: {} - Monto: ${}", tramite.getId(), monto);
        
        return pagoRepository.save(pago);
    }

    /**
     * Acredita un pago
     */
    public Pago acreditarPago(Long pagoId, String numeroComprobante, String cajero) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + pagoId));

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden acreditar pagos en estado PENDIENTE");
        }

        if (pago.isVencido()) {
            throw new IllegalStateException("No se puede acreditar un pago vencido");
        }

        pago.acreditar();
        pago.setNumeroComprobante(numeroComprobante);
        pago.setCajero(cajero);

        log.info("Acreditando pago ID: {} - Comprobante: {}", pagoId, numeroComprobante);
        
        return pagoRepository.save(pago);
    }

    /**
     * Rechaza un pago
     */
    public Pago rechazarPago(Long pagoId, String motivo) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + pagoId));

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar pagos en estado PENDIENTE");
        }

        pago.rechazar(motivo);
        log.info("Rechazando pago ID: {} - Motivo: {}", pagoId, motivo);
        
        return pagoRepository.save(pago);
    }

    /**
     * Procesa pagos vencidos
     */
    public void procesarPagosVencidos() {
        LocalDateTime fechaLimite = LocalDateTime.now();
        List<Pago> pagosVencidos = pagoRepository.findPagosVencidos(fechaLimite);
        
        for (Pago pago : pagosVencidos) {
            pago.marcarVencido();
            pagoRepository.save(pago);
        }
        
        log.info("Procesados {} pagos vencidos", pagosVencidos.size());
    }

    /**
     * Busca un pago por número de transacción
     */
    @Transactional(readOnly = true)
    public Optional<Pago> findByNumeroTransaccion(String numeroTransaccion) {
        return pagoRepository.findByNumeroTransaccion(numeroTransaccion);
    }

    /**
     * Busca un pago por número de comprobante
     */
    @Transactional(readOnly = true)
    public Optional<Pago> findByNumeroComprobante(String numeroComprobante) {
        return pagoRepository.findByNumeroComprobante(numeroComprobante);
    }

    /**
     * Obtiene el monto total recaudado en un período
     */
    @Transactional(readOnly = true)
    public BigDecimal getRecaudacionEnPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        BigDecimal total = pagoRepository.sumMontoAcreditadoEnPeriodo(desde, hasta);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Obtiene pagos acreditados en un período
     */
    @Transactional(readOnly = true)
    public List<Pago> getPagosAcreditadosEnPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.findPagosAcreditadosEnPeriodo(desde, hasta);
    }

    /**
     * Obtiene estadísticas de pagos por medio en un período
     */
    @Transactional(readOnly = true)
    public Long getCountByMedioEnPeriodo(MedioPago medio, LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.countByMedioEnPeriodo(medio, desde, hasta);
    }

    /**
     * Verifica si un trámite tiene pagos pendientes
     */
    @Transactional(readOnly = true)
    public boolean tienePagosPendientes(Tramite tramite) {
        Optional<Pago> pagoPendiente = pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.PENDIENTE);
        return pagoPendiente.isPresent() && !pagoPendiente.get().isVencido();
    }

    /**
     * Verifica si un trámite tiene pagos acreditados
     */
    @Transactional(readOnly = true)
    public boolean tienePagosAcreditados(Tramite tramite) {
        Optional<Pago> pagoAcreditado = pagoRepository.findByTramiteAndEstado(tramite, EstadoPago.ACREDITADO);
        return pagoAcreditado.isPresent();
    }

    /**
     * Obtiene el costo de un trámite
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerCostoTramite(TipoTramite tipo, ClaseLicencia clase) {
        Optional<CostoTramite> costo = costoTramiteRepository.findCostoVigente(tipo, clase, LocalDate.now());
        
        if (costo.isPresent()) {
            return costo.get().getCosto();
        }
        
        // Si no hay costo configurado, usar valores por defecto
        return obtenerCostoPorDefecto(tipo, clase);
    }

    /**
     * Crea un pago manual (pago en caja)
     */
    public Pago crearPagoManual(Tramite tramite, BigDecimal monto, String numeroComprobante, String cajero) {
        Pago pago = new Pago();
        pago.setTramite(tramite);
        pago.setMonto(monto);
        pago.setMedio(MedioPago.CAJA);
        pago.setEstado(EstadoPago.ACREDITADO);
        pago.setNumeroComprobante(numeroComprobante);
        pago.setCajero(cajero);
        pago.setNumeroTransaccion(generarNumeroTransaccion());
        pago.setFechaAcreditacion(LocalDateTime.now());

        log.info("Creando pago manual para trámite ID: {} - Monto: ${}", tramite.getId(), monto);
        
        return pagoRepository.save(pago);
    }

    /**
     * Procesa un pago online
     */
    public Pago procesarPagoOnline(Long pagoId, String numeroTransaccionExterno, boolean exitoso) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + pagoId));

        if (pago.getMedio() != MedioPago.PASARELA_ONLINE) {
            throw new IllegalStateException("Este método solo aplica para pagos online");
        }

        if (exitoso) {
            pago.acreditar();
            pago.setObservaciones("Pago procesado por pasarela - Transacción: " + numeroTransaccionExterno);
            log.info("Pago online exitoso ID: {} - Transacción externa: {}", pagoId, numeroTransaccionExterno);
        } else {
            pago.rechazar("Pago rechazado por la pasarela de pagos");
            log.info("Pago online rechazado ID: {}", pagoId);
        }
        
        return pagoRepository.save(pago);
    }

    private String generarNumeroTransaccion() {
        return "TXN-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               "-" + String.format("%04d", (int)(Math.random() * 10000));
    }

    private BigDecimal obtenerCostoPorDefecto(TipoTramite tipo, ClaseLicencia clase) {
        // Valores por defecto - en una implementación real estos deberían venir de configuración
        switch (tipo) {
            case EMISION:
                return new BigDecimal("1500.00");
            case RENOVACION:
                return new BigDecimal("1200.00");
            case DUPLICADO:
                return new BigDecimal("800.00");
            case CAMBIO_DOMICILIO:
                return new BigDecimal("600.00");
            default:
                return new BigDecimal("1000.00");
        }
    }

    /**
     * Obtiene el total de pagos realizados en el día actual
     */
    @Transactional(readOnly = true)
    public double getTotalPagosDiarios() {
        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDelDia = inicioDelDia.plusDays(1).minusSeconds(1);
        
        BigDecimal total = pagoRepository.sumMontoByFechaPagoBetween(inicioDelDia, finDelDia);
        return total != null ? total.doubleValue() : 0.0;
    }

    /**
     * Calcula la fecha de vencimiento para una orden de pago
     * basada en la configuración del sistema
     */
    private LocalDateTime calcularFechaVencimiento() {
        try {
            // Obtener horas de vencimiento desde configuración (por defecto 48 horas)
            String horasStr = configuracionService.getValor("pagos.vencimiento_horas", "48");
            int horas = Integer.parseInt(horasStr);
            return LocalDateTime.now().plusHours(horas);
        } catch (NumberFormatException e) {
            log.warn("Error al parsear configuración de vencimiento, usando valor por defecto: {}", e.getMessage());
            return LocalDateTime.now().plusHours(48);
        }
    }
}
