package com.example.sigelic.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.sigelic.config.TestSecurityConfig;
import com.example.sigelic.service.ReporteService;

@WebMvcTest(ReporteController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests de ReporteController")
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    private Map<String, Object> dashboard;

    @BeforeEach
    void setUp() {
        dashboard = Map.of("vigentesActuales", 100, "tramitesActivos", 20);
    }

    @Test
    @DisplayName("Debe obtener el dashboard principal")
    void debeObtenerDashboard() throws Exception {
        when(reporteService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/api/reportes/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigentesActuales").value(100));
    }

    @Test
    @DisplayName("Debe validar el conteo de licencias vencidas")
    void debeValidarLicenciasVencidas() throws Exception {
        when(reporteService.validarConteoLicenciasVencidas()).thenReturn(Map.of("totalVencidas", 5));

        mockMvc.perform(get("/api/reportes/validar/licencias-vencidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVencidas").value(5));
    }

    @Test
    @DisplayName("Debe validar el conteo de licencias vigentes")
    void debeValidarLicenciasVigentes() throws Exception {
        when(reporteService.validarConteoLicenciasVigentes()).thenReturn(Map.of("totalVigentes", 80));

        mockMvc.perform(get("/api/reportes/validar/licencias-vigentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVigentes").value(80));
    }

    @Test
    @DisplayName("Debe obtener el reporte de licencias por período parseando las fechas")
    void debeObtenerReporteLicenciasPorPeriodo() throws Exception {
        when(reporteService.getReporteLicenciasPorPeriodo(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Map.of("totalLicencias", 30));

        mockMvc.perform(get("/api/reportes/licencias")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLicencias").value(30));
    }

    @Test
    @DisplayName("Debe obtener el reporte de trámites por período parseando las fechas")
    void debeObtenerReporteTramitesPorPeriodo() throws Exception {
        when(reporteService.getReporteTramitesPorPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Map.of("totalTramites", 15));

        mockMvc.perform(get("/api/reportes/tramites")
                        .param("desde", "2026-01-01T00:00:00")
                        .param("hasta", "2026-06-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTramites").value(15));
    }

    @Test
    @DisplayName("Debe obtener el resumen comparativo de licencias combinando dashboard y reporte de 30 días")
    void debeObtenerResumenLicencias() throws Exception {
        when(reporteService.getDashboard()).thenReturn(dashboard);
        when(reporteService.getReporteLicenciasPorPeriodo(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Map.of("totalLicencias", 10));

        mockMvc.perform(get("/api/reportes/resumen-licencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard.vigentesActuales").value(100))
                .andExpect(jsonPath("$.reporteUltimos30Dias.totalLicencias").value(10))
                .andExpect(jsonPath("$.explicacion").exists());
    }
}
