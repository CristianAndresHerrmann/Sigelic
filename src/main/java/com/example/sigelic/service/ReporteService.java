package com.example.sigelic.service;

import com.example.sigelic.model.*;
import com.example.sigelic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar reportes y estadísticas del sistema
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReporteService {

    private final TramiteRepository tramiteRepository;
    private final LicenciaRepository licenciaRepository;
    private final PagoRepository pagoRepository;
    private final ExamenTeoricoRepository examenTeoricoRepository;
    private final ExamenPracticoRepository examenPracticoRepository;
    private final AptoMedicoRepository aptoMedicoRepository;
    private final TurnoRepository turnoRepository;
    private final TitularRepository titularRepository;
    private final InhabilitacionRepository inhabilitacionRepository;

    /**
     * Genera reporte de trámites por período
     */
    public Map<String, Object> getReporteTramitesPorPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = new HashMap<>();
        
        // Contar trámites por tipo
        Map<String, Long> tramitesPorTipo = new HashMap<>();
        for (TipoTramite tipo : TipoTramite.values()) {
            Long count = tramiteRepository.countByTipoEnPeriodo(tipo, desde, hasta);
            tramitesPorTipo.put(tipo.getDescripcion(), count);
        }
        
        // Contar trámites por estado
        Map<String, Long> tramitesPorEstado = new HashMap<>();
        for (EstadoTramite estado : EstadoTramite.values()) {
            Long count = tramiteRepository.countByEstado(estado);
            tramitesPorEstado.put(estado.getDescripcion(), count);
        }
        
        reporte.put("tramitesPorTipo", tramitesPorTipo);
        reporte.put("tramitesPorEstado", tramitesPorEstado);
        reporte.put("fechaDesde", desde);
        reporte.put("fechaHasta", hasta);
        
        return reporte;
    }

    /**
     * Genera reporte de licencias emitidas por período
     */
    public Map<String, Object> getReporteLicenciasPorPeriodo(LocalDate desde, LocalDate hasta) {
        Map<String, Object> reporte = new HashMap<>();
        
        Long totalLicencias = licenciaRepository.countLicenciasEmitidasEnPeriodo(desde, hasta);
        
        // Próximas a vencer (próximos 30 días)
        LocalDate proximasVencer = LocalDate.now().plusDays(30);
        List<Licencia> licenciasProximasVencer = licenciaRepository.findLicenciasProximasAVencer(LocalDate.now(), proximasVencer);
        
        // Licencias vencidas
        List<Licencia> licenciasVencidas = licenciaRepository.findLicenciasVencidas(LocalDate.now());
        
        reporte.put("totalLicenciasEmitidas", totalLicencias);
        reporte.put("licenciasProximasVencer", licenciasProximasVencer.size());
        reporte.put("licenciasVencidas", licenciasVencidas.size());
        reporte.put("fechaDesde", desde);
        reporte.put("fechaHasta", hasta);
        
        return reporte;
    }

    /**
     * Genera reporte de recaudación por período
     */
    public Map<String, Object> getReporteRecaudacionPorPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = new HashMap<>();
        
        BigDecimal totalRecaudado = pagoRepository.sumMontoAcreditadoEnPeriodo(desde, hasta);
        List<Pago> pagosAcreditados = pagoRepository.findPagosAcreditadosEnPeriodo(desde, hasta);
        
        // Recaudación por medio de pago
        Map<String, Long> pagosPorMedio = new HashMap<>();
        for (MedioPago medio : MedioPago.values()) {
            Long count = pagoRepository.countByMedioEnPeriodo(medio, desde, hasta);
            pagosPorMedio.put(medio.getDescripcion(), count);
        }
        
        reporte.put("totalRecaudado", totalRecaudado != null ? totalRecaudado : BigDecimal.ZERO);
        reporte.put("cantidadPagos", pagosAcreditados.size());
        reporte.put("pagosPorMedio", pagosPorMedio);
        reporte.put("fechaDesde", desde);
        reporte.put("fechaHasta", hasta);
        
        return reporte;
    }

    /**
     * Genera reporte de exámenes por período
     */
    public Map<String, Object> getReporteExamenesPorPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = new HashMap<>();
        
        // Exámenes teóricos
        Long totalTeoricos = examenTeoricoRepository.countTotalEnPeriodo(desde, hasta);
        Long aprobadosTeoricos = examenTeoricoRepository.countAprobadosEnPeriodo(desde, hasta);
        Double puntajePromedio = examenTeoricoRepository.findPuntajePromedioEnPeriodo(desde, hasta);
        
        // Exámenes prácticos
        Long totalPracticos = examenPracticoRepository.countTotalEnPeriodo(desde, hasta);
        Long aprobadosPracticos = examenPracticoRepository.countAprobadosEnPeriodo(desde, hasta);
        Double promedioFaltasLeves = examenPracticoRepository.findPromedioFaltasLevesEnPeriodo(desde, hasta);
        
        // Aptos médicos
        Long totalAptos = aptoMedicoRepository.countTotalEnPeriodo(desde, hasta);
        Long aptosPositivos = aptoMedicoRepository.countAptosEnPeriodo(desde, hasta);
        
        Map<String, Object> examenesTeoricos = new HashMap<>();
        examenesTeoricos.put("total", totalTeoricos);
        examenesTeoricos.put("aprobados", aprobadosTeoricos);
        examenesTeoricos.put("porcentajeAprobacion", totalTeoricos > 0 ? (aprobadosTeoricos * 100.0 / totalTeoricos) : 0);
        examenesTeoricos.put("puntajePromedio", puntajePromedio != null ? puntajePromedio : 0);
        
        Map<String, Object> examenesPracticos = new HashMap<>();
        examenesPracticos.put("total", totalPracticos);
        examenesPracticos.put("aprobados", aprobadosPracticos);
        examenesPracticos.put("porcentajeAprobacion", totalPracticos > 0 ? (aprobadosPracticos * 100.0 / totalPracticos) : 0);
        examenesPracticos.put("promedioFaltasLeves", promedioFaltasLeves != null ? promedioFaltasLeves : 0);
        
        Map<String, Object> aptosMedicos = new HashMap<>();
        aptosMedicos.put("total", totalAptos);
        aptosMedicos.put("aptos", aptosPositivos);
        aptosMedicos.put("porcentajeAptos", totalAptos > 0 ? (aptosPositivos * 100.0 / totalAptos) : 0);
        
        reporte.put("examenesTeoricos", examenesTeoricos);
        reporte.put("examenesPracticos", examenesPracticos);
        reporte.put("aptosMedicos", aptosMedicos);
        reporte.put("fechaDesde", desde);
        reporte.put("fechaHasta", hasta);
        
        return reporte;
    }

    /**
     * Genera reporte de turnos por período
     */
    public Map<String, Object> getReporteTurnosPorPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = new HashMap<>();
        
        // Turnos por estado
        Map<String, Long> turnosPorEstado = new HashMap<>();
        for (EstadoTurno estado : EstadoTurno.values()) {
            Long count = turnoRepository.countByEstadoEnPeriodo(estado, desde, hasta);
            turnosPorEstado.put(estado.getDescripcion(), count);
        }
        
        List<Turno> turnosEnPeriodo = turnoRepository.findTurnosEnPeriodo(desde, hasta);
        
        reporte.put("turnosPorEstado", turnosPorEstado);
        reporte.put("totalTurnos", turnosEnPeriodo.size());
        reporte.put("fechaDesde", desde);
        reporte.put("fechaHasta", hasta);
        
        return reporte;
    }

    /**
     * Genera dashboard con estadísticas generales
     */
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Estadísticas de trámites
        Long tramitesIniciados = tramiteRepository.countByEstado(EstadoTramite.INICIADO);
        Long tramitesEnProceso = tramiteRepository.countByEstado(EstadoTramite.DOCS_OK) +
                                 tramiteRepository.countByEstado(EstadoTramite.APTO_MED) +
                                 tramiteRepository.countByEstado(EstadoTramite.EX_TEO_OK) +
                                 tramiteRepository.countByEstado(EstadoTramite.EX_PRA_OK) +
                                 tramiteRepository.countByEstado(EstadoTramite.PAGO_OK);
        Long tramitesEmitidos = tramiteRepository.countByEstado(EstadoTramite.EMITIDA);
        Long tramitesRechazados = tramiteRepository.countByEstado(EstadoTramite.RECHAZADA);
        
        // Licencias próximas a vencer (próximos 30 días)
        LocalDate hoy = LocalDate.now();
        List<Licencia> proximasVencer = licenciaRepository.findLicenciasProximasAVencer(hoy, hoy.plusDays(30));
        List<Licencia> vencidas = licenciaRepository.findLicenciasVencidas(hoy);
        
        // Inhabilitaciones activas
        List<Inhabilitacion> inhabilitacionesActivas = inhabilitacionRepository.findInhabilitacionesActivas();
        
        // Recaudación del mes actual
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMes = LocalDateTime.now();
        BigDecimal recaudacionMesActual = pagoRepository.sumMontoAcreditadoEnPeriodo(inicioMes, finMes);
        
        Map<String, Object> tramites = new HashMap<>();
        tramites.put("iniciados", tramitesIniciados);
        tramites.put("enProceso", tramitesEnProceso);
        tramites.put("emitidos", tramitesEmitidos);
        tramites.put("rechazados", tramitesRechazados);
        
        Map<String, Object> licencias = new HashMap<>();
        licencias.put("proximasVencer", proximasVencer.size());
        licencias.put("vencidas", vencidas.size());
        
        dashboard.put("tramites", tramites);
        dashboard.put("licencias", licencias);
        dashboard.put("inhabilitacionesActivas", inhabilitacionesActivas.size());
        dashboard.put("recaudacionMesActual", recaudacionMesActual != null ? recaudacionMesActual : BigDecimal.ZERO);
        dashboard.put("fechaGeneracion", LocalDateTime.now());
        
        return dashboard;
    }

    /**
     * Genera reporte de inhabilitaciones
     */
    public Map<String, Object> getReporteInhabilitaciones() {
        Map<String, Object> reporte = new HashMap<>();
        
        List<Inhabilitacion> inhabilitacionesActivas = inhabilitacionRepository.findInhabilitacionesActivas();
        List<Titular> titularesInhabilitados = titularRepository.findTitularesConInhabilitacionesActivas();
        
        // Inhabilitaciones por autoridad
        Map<String, Long> inhabilitacionesPorAutoridad = new HashMap<>();
        inhabilitacionesActivas.forEach(inh -> {
            String autoridad = inh.getAutoridad();
            inhabilitacionesPorAutoridad.merge(autoridad, 1L, Long::sum);
        });
        
        reporte.put("inhabilitacionesActivas", inhabilitacionesActivas.size());
        reporte.put("titularesInhabilitados", titularesInhabilitados.size());
        reporte.put("inhabilitacionesPorAutoridad", inhabilitacionesPorAutoridad);
        reporte.put("fechaGeneracion", LocalDateTime.now());
        
        return reporte;
    }

    /**
     * Genera reporte de rendimiento por examinador
     */
    public Map<String, Object> getReporteRendimientoExaminadores(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = new HashMap<>();
        
        List<ExamenTeorico> examenesTeoricosPeriodo = examenTeoricoRepository.findExamenesEnPeriodo(desde, hasta);
        List<ExamenPractico> examenesPracticosPeriodo = examenPracticoRepository.findExamenesEnPeriodo(desde, hasta);
        
        // Agrupar por examinador
        Map<String, Map<String, Object>> rendimientoPorExaminador = new HashMap<>();
        
        // Procesar exámenes teóricos
        examenesTeoricosPeriodo.forEach(examen -> {
            String examinador = examen.getExaminador();
            if (examinador != null) {
                rendimientoPorExaminador.computeIfAbsent(examinador, k -> new HashMap<>());
                Map<String, Object> stats = rendimientoPorExaminador.get(examinador);
                stats.merge("totalTeoricos", 1L, (a, b) -> (Long)a + (Long)b);
                if (examen.getAprobado()) {
                    stats.merge("aprobadosTeoricos", 1L, (a, b) -> (Long)a + (Long)b);
                }
            }
        });
        
        // Procesar exámenes prácticos
        examenesPracticosPeriodo.forEach(examen -> {
            String examinador = examen.getExaminador();
            if (examinador != null) {
                rendimientoPorExaminador.computeIfAbsent(examinador, k -> new HashMap<>());
                Map<String, Object> stats = rendimientoPorExaminador.get(examinador);
                stats.merge("totalPracticos", 1L, (a, b) -> (Long)a + (Long)b);
                if (examen.getAprobado()) {
                    stats.merge("aprobadosPracticos", 1L, (a, b) -> (Long)a + (Long)b);
                }
            }
        });
        
        reporte.put("rendimientoPorExaminador", rendimientoPorExaminador);
        reporte.put("fechaDesde", desde);
        reporte.put("fechaHasta", hasta);
        
        return reporte;
    }
}
