package com.example.sigelic.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests de ExportService")
class ExportServiceTest {

    private ExportService exportService;
    private List<String> headers;
    private List<List<String>> data;

    @BeforeEach
    void setUp() {
        exportService = new ExportService();
        headers = List.of("Nombre", "DNI");
        data = List.of(
                List.of("Juan Pérez", "12345678"),
                List.of("María, \"la jefa\"", "87654321")
        );
    }

    @Test
    @DisplayName("Debe exportar a CSV incluyendo título, headers y filas escapando comillas")
    void debeExportarACsv() {
        byte[] resultado = exportService.exportToCsv("Reporte de Titulares", headers, data);

        String csv = new String(resultado);
        assertThat(csv).contains("Reporte de Titulares");
        assertThat(csv).contains("Nombre,DNI");
        assertThat(csv).contains("\"Juan Pérez\",\"12345678\"");
        assertThat(csv).contains("\"María, \"\"la jefa\"\"\",\"87654321\"");
    }

    @Test
    @DisplayName("Debe exportar a CSV con datos vacíos sin fallar")
    void debeExportarACsvConDatosVacios() {
        byte[] resultado = exportService.exportToCsv("Reporte Vacío", headers, List.of());

        assertThat(resultado).isNotEmpty();
        assertThat(new String(resultado)).contains("Reporte Vacío");
    }

    @Test
    @DisplayName("Debe exportar a CSV con celdas nulas sin fallar")
    void debeExportarACsvConCeldasNulas() {
        List<List<String>> dataConNulos = List.of(java.util.Arrays.asList("Juan", null));

        byte[] resultado = exportService.exportToCsv("Reporte", headers, dataConNulos);

        assertThat(new String(resultado)).contains("\"Juan\",\"\"");
    }

    @Test
    @DisplayName("Debe exportar a Excel generando un archivo no vacío")
    void debeExportarAExcel() {
        byte[] resultado = exportService.exportToExcel("Reporte de Titulares", headers, data);

        assertThat(resultado).isNotEmpty();
    }

    @Test
    @DisplayName("Debe exportar a PDF generando un archivo no vacío")
    void debeExportarAPdf() {
        byte[] resultado = exportService.exportToPdf("Reporte de Titulares", headers, data);

        assertThat(resultado).isNotEmpty();
    }

    @Test
    @DisplayName("Debe exportar a PDF sin datos mostrando mensaje de vacío")
    void debeExportarAPdfSinDatos() {
        byte[] resultado = exportService.exportToPdf("Reporte Vacío", headers, List.of());

        assertThat(resultado).isNotEmpty();
    }

    @Test
    @DisplayName("Debe convertir un mapa de estadísticas a filas con claves legibles")
    void debeConvertirStatsATabla() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTramites", 42);
        stats.put("fechaGeneracion", LocalDateTime.of(2026, 7, 12, 10, 30));
        stats.put("observaciones", null);

        List<List<String>> tabla = exportService.convertStatsToTable(stats);

        assertThat(tabla).hasSize(3);
        assertThat(tabla.get(0)).containsExactly("Total Tramites", "42");
        assertThat(tabla.get(1).get(0)).isEqualTo("Fecha Generacion");
        assertThat(tabla.get(2)).containsExactly("Observaciones", "");
    }
}
