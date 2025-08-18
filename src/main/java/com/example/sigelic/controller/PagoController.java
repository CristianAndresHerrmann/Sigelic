package com.example.sigelic.controller;

import com.example.sigelic.dto.request.PagoRequestDTO;
import com.example.sigelic.dto.response.PagoResponseDTO;
import com.example.sigelic.mapper.PagoMapper;
import com.example.sigelic.model.Pago;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.model.EstadoPago;
import com.example.sigelic.service.PagoService;
import com.example.sigelic.service.TramiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestión de pagos
 */
@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private TramiteService tramiteService;

    @Autowired
    private PagoMapper pagoMapper;

    /**
     * Obtiene un pago por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtenerPagoPorId(@PathVariable Long id) {
        Optional<Pago> pagoOpt = pagoService.findById(id);
        if (pagoOpt.isPresent()) {
            PagoResponseDTO dto = pagoMapper.toResponseDTOWithDetails(pagoOpt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Crea una nueva orden de pago
     */
    @PostMapping
    public ResponseEntity<PagoResponseDTO> crearOrdenPago(@Valid @RequestBody PagoRequestDTO pagoRequest) {
        // Obtener trámite
        Optional<Tramite> tramiteOpt = tramiteService.findById(pagoRequest.getTramiteId());
        if (!tramiteOpt.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Crear orden de pago
        Pago pago;
        if (pagoRequest.getMonto() != null) {
            // Pago manual con monto específico
            pago = pagoService.crearPagoManual(
                tramiteOpt.get(),
                pagoRequest.getMonto(),
                pagoRequest.getNumeroComprobante(),
                pagoRequest.getCajero()
            );
        } else {
            // Orden de pago automática
            pago = pagoService.crearOrdenPago(
                tramiteOpt.get(),
                pagoRequest.getMedio()
            );
        }

        PagoResponseDTO dto = pagoMapper.toResponseDTO(pago);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Acredita un pago
     */
    @PatchMapping("/{id}/acreditar")
    public ResponseEntity<PagoResponseDTO> acreditarPago(
            @PathVariable Long id,
            @RequestParam(required = false) String numeroComprobante,
            @RequestParam(required = false) String cajero) {
        
        try {
            Pago pago = pagoService.acreditarPago(id, numeroComprobante, cajero);
            PagoResponseDTO dto = pagoMapper.toResponseDTO(pago);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Rechaza un pago
     */
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<PagoResponseDTO> rechazarPago(
            @PathVariable Long id,
            @RequestParam String motivo) {
        
        try {
            Pago pago = pagoService.rechazarPago(id, motivo);
            PagoResponseDTO dto = pagoMapper.toResponseDTO(pago);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtiene pagos de un trámite
     */
    @GetMapping("/tramite/{tramiteId}")
    public ResponseEntity<List<PagoResponseDTO>> obtenerPagosPorTramite(@PathVariable Long tramiteId) {
        Optional<Tramite> tramiteOpt = tramiteService.findById(tramiteId);
        if (tramiteOpt.isPresent()) {
            List<Pago> pagos = pagoService.findByTramite(tramiteOpt.get());
            List<PagoResponseDTO> dtos = pagoMapper.toResponseDTOList(pagos);
            return ResponseEntity.ok(dtos);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene pagos por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PagoResponseDTO>> obtenerPagosPorEstado(@PathVariable EstadoPago estado) {
        List<Pago> pagos = pagoService.findByEstado(estado);
        List<PagoResponseDTO> dtos = pagoMapper.toResponseDTOList(pagos);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene pagos por fecha
     */
    @GetMapping("/fecha")
    public ResponseEntity<List<PagoResponseDTO>> obtenerPagosPorFecha(
            @RequestParam LocalDateTime fechaDesde,
            @RequestParam LocalDateTime fechaHasta) {
        
        List<Pago> pagos = pagoService.getPagosAcreditadosEnPeriodo(fechaDesde, fechaHasta);
        List<PagoResponseDTO> dtos = pagoMapper.toResponseDTOList(pagos);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene pagos vencidos
     */
    @GetMapping("/vencidos")
    public ResponseEntity<List<PagoResponseDTO>> obtenerPagosVencidos() {
        List<Pago> pagos = pagoService.findByEstado(EstadoPago.VENCIDO);
        List<PagoResponseDTO> dtos = pagoMapper.toResponseDTOList(pagos);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene el resumen de pagos del mes actual
     */
    @GetMapping("/resumen/mes-actual")
    public ResponseEntity<?> obtenerResumenPagosMesActual() {
        // Este endpoint podría devolver estadísticas de pagos del mes
        return ResponseEntity.ok().build();
    }

    /**
     * Procesa pagos vencidos masivamente
     */
    @PostMapping("/procesar-vencidos")
    public ResponseEntity<Void> procesarPagosVencidos() {
        pagoService.procesarPagosVencidos();
        return ResponseEntity.ok().build();
    }
}
