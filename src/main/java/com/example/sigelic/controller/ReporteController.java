package com.example.sigelic.controller;

import com.example.sigelic.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para generar reportes y validaciones
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    /**
     * Obtiene el dashboard principal con estadísticas generales
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(reporteService.getDashboard());
    }

    /**
     * Valida el conteo de licencias vencidas
     */
    @GetMapping("/validar/licencias-vencidas")
    public ResponseEntity<Map<String, Object>> validarLicenciasVencidas() {
        return ResponseEntity.ok(reporteService.validarConteoLicenciasVencidas());
    }

    /**
     * Valida el conteo de licencias vigentes
     */
    @GetMapping("/validar/licencias-vigentes")
    public ResponseEntity<Map<String, Object>> validarLicenciasVigentes() {
        return ResponseEntity.ok(reporteService.validarConteoLicenciasVigentes());
    }

    /**
     * Obtiene reporte de licencias por período
     */
    @GetMapping("/licencias")
    public ResponseEntity<Map<String, Object>> getReporteLicencias(
            @RequestParam("desde") String desde,
            @RequestParam("hasta") String hasta) {
        LocalDate fechaDesde = LocalDate.parse(desde);
        LocalDate fechaHasta = LocalDate.parse(hasta);
        return ResponseEntity.ok(reporteService.getReporteLicenciasPorPeriodo(fechaDesde, fechaHasta));
    }

    /**
     * Obtiene reporte de trámites por período
     */
    @GetMapping("/tramites")
    public ResponseEntity<Map<String, Object>> getReporteTramites(
            @RequestParam("desde") String desde,
            @RequestParam("hasta") String hasta) {
        LocalDateTime fechaDesde = LocalDateTime.parse(desde);
        LocalDateTime fechaHasta = LocalDateTime.parse(hasta);
        return ResponseEntity.ok(reporteService.getReporteTramitesPorPeriodo(fechaDesde, fechaHasta));
    }

    /**
     * Obtiene resumen comparativo de licencias
     * Útil para entender la diferencia entre licencias emitidas vs vigentes
     */
    @GetMapping("/resumen-licencias")
    public ResponseEntity<Map<String, Object>> getResumenLicencias() {
        Map<String, Object> resumen = new HashMap<>();
        
        // Dashboard general
        Map<String, Object> dashboard = reporteService.getDashboard();
        
        // Reporte de últimos 30 días
        LocalDate hace30Dias = LocalDate.now().minusDays(30);
        LocalDate hoy = LocalDate.now();
        Map<String, Object> reporteUltimos30Dias = reporteService.getReporteLicenciasPorPeriodo(hace30Dias, hoy);
        
        resumen.put("dashboard", dashboard);
        resumen.put("reporteUltimos30Dias", reporteUltimos30Dias);
        resumen.put("explicacion", Map.of(
            "dashboard", "Contiene estadísticas del estado actual de todas las licencias",
            "reporteUltimos30Dias", "Contiene solo las licencias EMITIDAS en los últimos 30 días, pero incluye estado actual de todas las licencias",
            "diferencia", "Si hay diferencia entre 'vigentesActuales' del dashboard y 'totalLicenciasEmitidas' del reporte, es porque hay licencias vigentes emitidas antes del período consultado"
        ));
        
        return ResponseEntity.ok(resumen);
    }
}
