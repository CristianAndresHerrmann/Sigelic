package com.example.sigelic.service;

import com.example.sigelic.model.*;
import com.example.sigelic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ReporteService")
class ReporteServiceTest {

    @Mock
    private TramiteRepository tramiteRepository;
    @Mock
    private LicenciaRepository licenciaRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private ExamenTeoricoRepository examenTeoricoRepository;
    @Mock
    private ExamenPracticoRepository examenPracticoRepository;
    @Mock
    private AptoMedicoRepository aptoMedicoRepository;
    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private TitularRepository titularRepository;
    @Mock
    private InhabilitacionRepository inhabilitacionRepository;

    @InjectMocks
    private ReporteService reporteService;

    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private LocalDate fechaDesdeDate;
    private LocalDate fechaHastaDate;

    @BeforeEach
    void setUp() {
        fechaDesde = LocalDateTime.now().minusDays(30);
        fechaHasta = LocalDateTime.now();
        fechaDesdeDate = LocalDate.now().minusDays(30);
        fechaHastaDate = LocalDate.now();
    }

    @Nested
    @DisplayName("Reportes de trámites")
    class ReportesTramites {

        @Test
        @DisplayName("Debe generar reporte de trámites por período")
        void debeGenerarReporteTramitesPorPeriodo() {
            // Given
            when(tramiteRepository.countByTipoEnPeriodo(TipoTramite.EMISION, fechaDesde, fechaHasta)).thenReturn(10L);
            when(tramiteRepository.countByTipoEnPeriodo(TipoTramite.RENOVACION, fechaDesde, fechaHasta)).thenReturn(5L);
            when(tramiteRepository.countByTipoEnPeriodo(TipoTramite.DUPLICADO, fechaDesde, fechaHasta)).thenReturn(3L);
            when(tramiteRepository.countByTipoEnPeriodo(TipoTramite.CAMBIO_DOMICILIO, fechaDesde, fechaHasta)).thenReturn(2L);
            
            when(tramiteRepository.countByEstado(EstadoTramite.INICIADO)).thenReturn(8L);
            when(tramiteRepository.countByEstado(EstadoTramite.DOCS_OK)).thenReturn(5L);
            when(tramiteRepository.countByEstado(EstadoTramite.EMITIDA)).thenReturn(7L);

            // When
            Map<String, Object> reporte = reporteService.getReporteTramitesPorPeriodo(fechaDesde, fechaHasta);

            // Then
            assertThat(reporte).isNotNull();
            assertThat(reporte).containsKeys("tramitesPorTipo", "tramitesPorEstado", "fechaDesde", "fechaHasta");
            
            @SuppressWarnings("unchecked")
            Map<String, Long> tramitesPorTipo = (Map<String, Long>) reporte.get("tramitesPorTipo");
            assertThat(tramitesPorTipo).isNotEmpty();
            
            @SuppressWarnings("unchecked")
            Map<String, Long> tramitesPorEstado = (Map<String, Long>) reporte.get("tramitesPorEstado");
            assertThat(tramitesPorEstado).isNotEmpty();

            verify(tramiteRepository, times(4)).countByTipoEnPeriodo(any(TipoTramite.class), eq(fechaDesde), eq(fechaHasta));
        }
    }

    @Nested
    @DisplayName("Reportes de licencias")
    class ReportesLicencias {

        @Test
        @DisplayName("Debe generar reporte de licencias por período")
        void debeGenerarReporteLicenciasPorPeriodo() {
            // Given
            Licencia licencia1 = new Licencia();
            Licencia licencia2 = new Licencia();
            List<Licencia> licenciasProximasVencer = Arrays.asList(licencia1);
            List<Licencia> licenciasVencidas = Arrays.asList(licencia2);

            when(licenciaRepository.countLicenciasEmitidasEnPeriodo(fechaDesdeDate, fechaHastaDate)).thenReturn(15L);
            when(licenciaRepository.findLicenciasProximasAVencer(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(licenciasProximasVencer);
            when(licenciaRepository.findLicenciasVencidas(any(LocalDate.class))).thenReturn(licenciasVencidas);

            // When
            Map<String, Object> reporte = reporteService.getReporteLicenciasPorPeriodo(fechaDesdeDate, fechaHastaDate);

            // Then
            assertThat(reporte).isNotNull();
            assertThat(reporte.get("totalLicenciasEmitidas")).isEqualTo(15L);
            assertThat(reporte.get("licenciasProximasVencer")).isEqualTo(1);
            assertThat(reporte.get("licenciasVencidas")).isEqualTo(1);
            assertThat(reporte).containsKeys("fechaDesde", "fechaHasta");

            verify(licenciaRepository).countLicenciasEmitidasEnPeriodo(fechaDesdeDate, fechaHastaDate);
            verify(licenciaRepository).findLicenciasProximasAVencer(any(LocalDate.class), any(LocalDate.class));
            verify(licenciaRepository).findLicenciasVencidas(any(LocalDate.class));
        }
    }

    @Nested
    @DisplayName("Reportes de recaudación")
    class ReportesRecaudacion {

        @Test
        @DisplayName("Debe generar reporte de recaudación por período")
        void debeGenerarReporteRecaudacionPorPeriodo() {
            // Given
            BigDecimal totalRecaudado = new BigDecimal("50000.00");
            Pago pago1 = new Pago();
            Pago pago2 = new Pago();
            List<Pago> pagosAcreditados = Arrays.asList(pago1, pago2);

            when(pagoRepository.sumMontoAcreditadoEnPeriodo(fechaDesde, fechaHasta)).thenReturn(totalRecaudado);
            when(pagoRepository.findPagosAcreditadosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(pagosAcreditados);
            when(pagoRepository.countByMedioEnPeriodo(MedioPago.CAJA, fechaDesde, fechaHasta)).thenReturn(10L);
            when(pagoRepository.countByMedioEnPeriodo(MedioPago.PASARELA_ONLINE, fechaDesde, fechaHasta)).thenReturn(5L);
            when(pagoRepository.countByMedioEnPeriodo(MedioPago.TRANSFERENCIA, fechaDesde, fechaHasta)).thenReturn(3L);

            // When
            Map<String, Object> reporte = reporteService.getReporteRecaudacionPorPeriodo(fechaDesde, fechaHasta);

            // Then
            assertThat(reporte).isNotNull();
            assertThat(reporte.get("totalRecaudado")).isEqualTo(totalRecaudado);
            assertThat(reporte.get("cantidadPagos")).isEqualTo(2);
            assertThat(reporte).containsKeys("pagosPorMedio", "fechaDesde", "fechaHasta");

            @SuppressWarnings("unchecked")
            Map<String, Long> pagosPorMedio = (Map<String, Long>) reporte.get("pagosPorMedio");
            assertThat(pagosPorMedio).isNotEmpty();

            verify(pagoRepository).sumMontoAcreditadoEnPeriodo(fechaDesde, fechaHasta);
            verify(pagoRepository).findPagosAcreditadosEnPeriodo(fechaDesde, fechaHasta);
        }

        @Test
        @DisplayName("Debe manejar recaudación nula")
        void debeManejarRecaudacionNula() {
            // Given
            when(pagoRepository.sumMontoAcreditadoEnPeriodo(fechaDesde, fechaHasta)).thenReturn(null);
            when(pagoRepository.findPagosAcreditadosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(Arrays.asList());
            when(pagoRepository.countByMedioEnPeriodo(any(MedioPago.class), eq(fechaDesde), eq(fechaHasta))).thenReturn(0L);

            // When
            Map<String, Object> reporte = reporteService.getReporteRecaudacionPorPeriodo(fechaDesde, fechaHasta);

            // Then
            assertThat(reporte.get("totalRecaudado")).isEqualTo(BigDecimal.ZERO);
            assertThat(reporte.get("cantidadPagos")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Reportes de exámenes")
    class ReportesExamenes {

        @Test
        @DisplayName("Debe generar reporte de exámenes por período")
        void debeGenerarReporteExamenesPorPeriodo() {
            // Given
            when(examenTeoricoRepository.countTotalEnPeriodo(fechaDesde, fechaHasta)).thenReturn(20L);
            when(examenTeoricoRepository.countAprobadosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(15L);
            when(examenTeoricoRepository.findPuntajePromedioEnPeriodo(fechaDesde, fechaHasta)).thenReturn(78.5);

            when(examenPracticoRepository.countTotalEnPeriodo(fechaDesde, fechaHasta)).thenReturn(18L);
            when(examenPracticoRepository.countAprobadosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(12L);
            when(examenPracticoRepository.findPromedioFaltasLevesEnPeriodo(fechaDesde, fechaHasta)).thenReturn(2.3);

            when(aptoMedicoRepository.countTotalEnPeriodo(fechaDesde, fechaHasta)).thenReturn(25L);
            when(aptoMedicoRepository.countAptosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(23L);

            // When
            Map<String, Object> reporte = reporteService.getReporteExamenesPorPeriodo(fechaDesde, fechaHasta);

            // Then
            assertThat(reporte).isNotNull();
            assertThat(reporte).containsKeys("examenesTeoricos", "examenesPracticos", "aptosMedicos");

            @SuppressWarnings("unchecked")
            Map<String, Object> examenesTeoricos = (Map<String, Object>) reporte.get("examenesTeoricos");
            assertThat(examenesTeoricos.get("total")).isEqualTo(20L);
            assertThat(examenesTeoricos.get("aprobados")).isEqualTo(15L);
            assertThat(examenesTeoricos.get("porcentajeAprobacion")).isEqualTo(75.0);
            assertThat(examenesTeoricos.get("puntajePromedio")).isEqualTo(78.5);

            @SuppressWarnings("unchecked")
            Map<String, Object> examenesPracticos = (Map<String, Object>) reporte.get("examenesPracticos");
            assertThat(examenesPracticos.get("total")).isEqualTo(18L);
            assertThat(examenesPracticos.get("aprobados")).isEqualTo(12L);

            @SuppressWarnings("unchecked")
            Map<String, Object> aptosMedicos = (Map<String, Object>) reporte.get("aptosMedicos");
            assertThat(aptosMedicos.get("total")).isEqualTo(25L);
            assertThat(aptosMedicos.get("aptos")).isEqualTo(23L);

            verify(examenTeoricoRepository).countTotalEnPeriodo(fechaDesde, fechaHasta);
            verify(examenPracticoRepository).countTotalEnPeriodo(fechaDesde, fechaHasta);
            verify(aptoMedicoRepository).countTotalEnPeriodo(fechaDesde, fechaHasta);
        }

        @Test
        @DisplayName("Debe manejar valores nulos en estadísticas de exámenes")
        void debeManejarValoresNulosEnEstadisticasExamenes() {
            // Given
            when(examenTeoricoRepository.countTotalEnPeriodo(fechaDesde, fechaHasta)).thenReturn(0L);
            when(examenTeoricoRepository.countAprobadosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(0L);
            when(examenTeoricoRepository.findPuntajePromedioEnPeriodo(fechaDesde, fechaHasta)).thenReturn(null);

            when(examenPracticoRepository.countTotalEnPeriodo(fechaDesde, fechaHasta)).thenReturn(0L);
            when(examenPracticoRepository.countAprobadosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(0L);
            when(examenPracticoRepository.findPromedioFaltasLevesEnPeriodo(fechaDesde, fechaHasta)).thenReturn(null);

            when(aptoMedicoRepository.countTotalEnPeriodo(fechaDesde, fechaHasta)).thenReturn(0L);
            when(aptoMedicoRepository.countAptosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(0L);

            // When
            Map<String, Object> reporte = reporteService.getReporteExamenesPorPeriodo(fechaDesde, fechaHasta);

            // Then
            @SuppressWarnings("unchecked")
            Map<String, Object> examenesTeoricos = (Map<String, Object>) reporte.get("examenesTeoricos");
            assertThat(examenesTeoricos.get("porcentajeAprobacion")).isEqualTo(0.0);
            assertThat(examenesTeoricos.get("puntajePromedio")).isEqualTo(0.0);

            @SuppressWarnings("unchecked")
            Map<String, Object> examenesPracticos = (Map<String, Object>) reporte.get("examenesPracticos");
            assertThat(examenesPracticos.get("promedioFaltasLeves")).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Reportes de turnos")
    class ReportesTurnos {

        @Test
        @DisplayName("Debe generar reporte de turnos por período")
        void debeGenerarReporteTurnosPorPeriodo() {
            // Given
            Turno turno1 = new Turno();
            Turno turno2 = new Turno();
            List<Turno> turnos = Arrays.asList(turno1, turno2);

            when(turnoRepository.countByEstadoEnPeriodo(EstadoTurno.RESERVADO, fechaDesde, fechaHasta)).thenReturn(5L);
            when(turnoRepository.countByEstadoEnPeriodo(EstadoTurno.CONFIRMADO, fechaDesde, fechaHasta)).thenReturn(8L);
            when(turnoRepository.countByEstadoEnPeriodo(EstadoTurno.COMPLETADO, fechaDesde, fechaHasta)).thenReturn(12L);
            when(turnoRepository.countByEstadoEnPeriodo(EstadoTurno.CANCELADO, fechaDesde, fechaHasta)).thenReturn(3L);
            when(turnoRepository.countByEstadoEnPeriodo(EstadoTurno.AUSENTE, fechaDesde, fechaHasta)).thenReturn(2L);
            when(turnoRepository.findTurnosEnPeriodo(fechaDesde, fechaHasta)).thenReturn(turnos);

            // When
            Map<String, Object> reporte = reporteService.getReporteTurnosPorPeriodo(fechaDesde, fechaHasta);

            // Then
            assertThat(reporte).isNotNull();
            assertThat(reporte.get("totalTurnos")).isEqualTo(2);
            assertThat(reporte).containsKeys("turnosPorEstado", "fechaDesde", "fechaHasta");

            @SuppressWarnings("unchecked")
            Map<String, Long> turnosPorEstado = (Map<String, Long>) reporte.get("turnosPorEstado");
            assertThat(turnosPorEstado).isNotEmpty();

            verify(turnoRepository).findTurnosEnPeriodo(fechaDesde, fechaHasta);
        }
    }

    @Nested
    @DisplayName("Dashboard")
    class Dashboard {

        @Test
        @DisplayName("Debe generar dashboard con estadísticas generales")
        void debeGenerarDashboardConEstadisticasGenerales() {
            // Given
            Licencia licencia = new Licencia();
            Inhabilitacion inhabilitacion = new Inhabilitacion();
            BigDecimal recaudacionMes = new BigDecimal("25000.00");

            when(tramiteRepository.countByEstado(EstadoTramite.INICIADO)).thenReturn(10L);
            when(tramiteRepository.countByEstado(EstadoTramite.DOCS_OK)).thenReturn(5L);
            when(tramiteRepository.countByEstado(EstadoTramite.APTO_MED)).thenReturn(3L);
            when(tramiteRepository.countByEstado(EstadoTramite.EX_TEO_OK)).thenReturn(2L);
            when(tramiteRepository.countByEstado(EstadoTramite.EX_PRA_OK)).thenReturn(1L);
            when(tramiteRepository.countByEstado(EstadoTramite.PAGO_OK)).thenReturn(1L);
            when(tramiteRepository.countByEstado(EstadoTramite.EMITIDA)).thenReturn(20L);
            when(tramiteRepository.countByEstado(EstadoTramite.RECHAZADA)).thenReturn(2L);

            when(licenciaRepository.findLicenciasProximasAVencer(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Arrays.asList(licencia));
            when(licenciaRepository.findLicenciasVencidas(any(LocalDate.class))).thenReturn(Arrays.asList());
            when(inhabilitacionRepository.findInhabilitacionesActivas()).thenReturn(Arrays.asList(inhabilitacion));
            when(pagoRepository.sumMontoAcreditadoEnPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(recaudacionMes);

            // When
            Map<String, Object> dashboard = reporteService.getDashboard();

            // Then
            assertThat(dashboard).isNotNull();
            assertThat(dashboard).containsKeys("tramites", "licencias", "inhabilitacionesActivas", "recaudacionMesActual", "fechaGeneracion");

            @SuppressWarnings("unchecked")
            Map<String, Object> tramites = (Map<String, Object>) dashboard.get("tramites");
            assertThat(tramites.get("iniciados")).isEqualTo(10L);
            assertThat(tramites.get("enProceso")).isEqualTo(12L); // Suma de todos los estados en proceso
            assertThat(tramites.get("emitidos")).isEqualTo(20L);
            assertThat(tramites.get("rechazados")).isEqualTo(2L);

            @SuppressWarnings("unchecked")
            Map<String, Object> licencias = (Map<String, Object>) dashboard.get("licencias");
            assertThat(licencias.get("proximasVencer")).isEqualTo(1);
            assertThat(licencias.get("vencidas")).isEqualTo(0);

            assertThat(dashboard.get("inhabilitacionesActivas")).isEqualTo(1);
            assertThat(dashboard.get("recaudacionMesActual")).isEqualTo(recaudacionMes);
        }

        @Test
        @DisplayName("Debe manejar recaudación nula en dashboard")
        void debeManejarRecaudacionNulaEnDashboard() {
            // Given
            when(tramiteRepository.countByEstado(any(EstadoTramite.class))).thenReturn(0L);
            when(licenciaRepository.findLicenciasProximasAVencer(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Arrays.asList());
            when(licenciaRepository.findLicenciasVencidas(any(LocalDate.class))).thenReturn(Arrays.asList());
            when(inhabilitacionRepository.findInhabilitacionesActivas()).thenReturn(Arrays.asList());
            when(pagoRepository.sumMontoAcreditadoEnPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(null);

            // When
            Map<String, Object> dashboard = reporteService.getDashboard();

            // Then
            assertThat(dashboard.get("recaudacionMesActual")).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Reportes de inhabilitaciones")
    class ReportesInhabilitaciones {

        @Test
        @DisplayName("Debe generar reporte de inhabilitaciones")
        void debeGenerarReporteInhabilitaciones() {
            // Given
            Inhabilitacion inh1 = new Inhabilitacion();
            inh1.setAutoridad("Policía Provincial");
            Inhabilitacion inh2 = new Inhabilitacion();
            inh2.setAutoridad("Policía Provincial");
            Inhabilitacion inh3 = new Inhabilitacion();
            inh3.setAutoridad("Gendarmería");

            Titular titular = new Titular();

            when(inhabilitacionRepository.findInhabilitacionesActivas())
                    .thenReturn(Arrays.asList(inh1, inh2, inh3));
            when(titularRepository.findTitularesConInhabilitacionesActivas())
                    .thenReturn(Arrays.asList(titular));

            // When
            Map<String, Object> reporte = reporteService.getReporteInhabilitaciones();

            // Then
            assertThat(reporte).isNotNull();
            assertThat(reporte.get("inhabilitacionesActivas")).isEqualTo(3);
            assertThat(reporte.get("titularesInhabilitados")).isEqualTo(1);
            assertThat(reporte).containsKeys("inhabilitacionesPorAutoridad", "fechaGeneracion");

            @SuppressWarnings("unchecked")
            Map<String, Long> inhabilitacionesPorAutoridad = (Map<String, Long>) reporte.get("inhabilitacionesPorAutoridad");
            assertThat(inhabilitacionesPorAutoridad.get("Policía Provincial")).isEqualTo(2L);
            assertThat(inhabilitacionesPorAutoridad.get("Gendarmería")).isEqualTo(1L);

            verify(inhabilitacionRepository).findInhabilitacionesActivas();
            verify(titularRepository).findTitularesConInhabilitacionesActivas();
        }
    }

    @Nested
    @DisplayName("Reportes de rendimiento")
    class ReportesRendimiento {

        @Test
        @DisplayName("Debe generar reporte de rendimiento por examinadores")
        void debeGenerarReporteRendimientoPorExaminadores() {
            // Given
            ExamenTeorico exTeorico1 = new ExamenTeorico();
            exTeorico1.setExaminador("Dr. García");
            exTeorico1.setAprobado(true);

            ExamenTeorico exTeorico2 = new ExamenTeorico();
            exTeorico2.setExaminador("Dr. García");
            exTeorico2.setAprobado(false);

            ExamenPractico exPractico1 = new ExamenPractico();
            exPractico1.setExaminador("Insp. López");
            exPractico1.setAprobado(true);

            when(examenTeoricoRepository.findExamenesEnPeriodo(fechaDesde, fechaHasta))
                    .thenReturn(Arrays.asList(exTeorico1, exTeorico2));
            when(examenPracticoRepository.findExamenesEnPeriodo(fechaDesde, fechaHasta))
                    .thenReturn(Arrays.asList(exPractico1));

            // When
            Map<String, Object> reporte = reporteService.getReporteRendimientoExaminadores(fechaDesde, fechaHasta);

            // Then
            assertThat(reporte).isNotNull();
            assertThat(reporte).containsKeys("rendimientoPorExaminador", "fechaDesde", "fechaHasta");

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> rendimiento = (Map<String, Map<String, Object>>) reporte.get("rendimientoPorExaminador");
            assertThat(rendimiento).containsKey("Dr. García");
            assertThat(rendimiento).containsKey("Insp. López");

            Map<String, Object> statsGarcia = rendimiento.get("Dr. García");
            assertThat(statsGarcia.get("totalTeoricos")).isEqualTo(2L);
            assertThat(statsGarcia.get("aprobadosTeoricos")).isEqualTo(1L);

            Map<String, Object> statsLopez = rendimiento.get("Insp. López");
            assertThat(statsLopez.get("totalPracticos")).isEqualTo(1L);
            assertThat(statsLopez.get("aprobadosPracticos")).isEqualTo(1L);

            verify(examenTeoricoRepository).findExamenesEnPeriodo(fechaDesde, fechaHasta);
            verify(examenPracticoRepository).findExamenesEnPeriodo(fechaDesde, fechaHasta);
        }

        @Test
        @DisplayName("Debe manejar exámenes sin examinador")
        void debeManejarExamenesSinExaminador() {
            // Given
            ExamenTeorico exTeorico = new ExamenTeorico();
            exTeorico.setExaminador(null);

            when(examenTeoricoRepository.findExamenesEnPeriodo(fechaDesde, fechaHasta))
                    .thenReturn(Arrays.asList(exTeorico));
            when(examenPracticoRepository.findExamenesEnPeriodo(fechaDesde, fechaHasta))
                    .thenReturn(Arrays.asList());

            // When
            Map<String, Object> reporte = reporteService.getReporteRendimientoExaminadores(fechaDesde, fechaHasta);

            // Then
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> rendimiento = (Map<String, Map<String, Object>>) reporte.get("rendimientoPorExaminador");
            assertThat(rendimiento).isEmpty(); // No debe incluir exámenes sin examinador
        }
    }
}
