package com.example.sigelic.views;

import com.example.sigelic.service.ExportService;
import com.example.sigelic.service.ReporteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vista para generación de reportes
 */
@Route(value = "reportes", layout = MainLayout.class)
@PageTitle("Reportes | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "AUDITOR"})
@Slf4j
public class ReportesView extends VerticalLayout {

    private final ReporteService reporteService;
    private final ExportService exportService;

    private ComboBox<TipoReporte> tipoReporteCombo;
    private DatePicker fechaDesde;
    private DatePicker fechaHasta;
    private Grid<Map<String, String>> grid;
    private Button exportButton;
    private Button exportExcelButton;

    private List<String> currentHeaders = new ArrayList<>();
    private List<List<String>> currentData = new ArrayList<>();
    private String currentTitle = "";

    public enum TipoReporte {
        PAGOS("Pagos Recibidos"),
        TRAMITES("Trámites por Estado"),
        LICENCIAS("Licencias Emitidas"),
        EXAMENES("Exámenes Realizados"),
        TURNOS("Turnos"),
        RECAUDACION("Recaudación"),
        INHABILITACIONES("Inhabilitaciones"),
        RENDIMIENTO("Rendimiento Examinadores"),
        DASHBOARD("Dashboard General");

        private final String descripcion;

        TipoReporte(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

    public ReportesView(ReporteService reporteService, ExportService exportService) {
        this.reporteService = reporteService;
        this.exportService = exportService;
        
        addClassName("reportes-view");
        setSizeFull();

        createHeader();
        createFilters();
        createGrid();
        
        // Configurar exportación inicialmente deshabilitada
        updateExportButtons(false);
    }

    private void createHeader() {
        H2 title = new H2("Reportes");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);
        add(title);
    }

    private void createFilters() {
        H3 filtersTitle = new H3("Filtros de Reporte");
        filtersTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Top.MEDIUM);

        tipoReporteCombo = new ComboBox<>("Tipo de Reporte");
        tipoReporteCombo.setItems(TipoReporte.values());
        tipoReporteCombo.setItemLabelGenerator(TipoReporte::getDescripcion);
        tipoReporteCombo.setWidthFull();

        fechaDesde = new DatePicker("Fecha Desde");
        fechaDesde.setValue(LocalDate.now().minusMonths(1));

        fechaHasta = new DatePicker("Fecha Hasta");
        fechaHasta.setValue(LocalDate.now());

        Button generateButton = new Button("Generar Reporte", new Icon(VaadinIcon.CHART));
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generateButton.addClickListener(e -> generateReport());

        exportButton = new Button("Exportar CSV", new Icon(VaadinIcon.FILE_TEXT));
        exportButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        exportButton.addClickListener(e -> exportToCsv());

        exportExcelButton = new Button("Exportar Excel", new Icon(VaadinIcon.FILE_TABLE));
        exportExcelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        exportExcelButton.addClickListener(e -> exportToExcel());

        HorizontalLayout filterRow1 = new HorizontalLayout(tipoReporteCombo);
        filterRow1.setWidthFull();
        
        HorizontalLayout filterRow2 = new HorizontalLayout(fechaDesde, fechaHasta, generateButton);
        filterRow2.setAlignItems(Alignment.END);
        filterRow2.setWidthFull();

        HorizontalLayout exportRow = new HorizontalLayout(exportButton, exportExcelButton);

        add(filtersTitle, filterRow1, filterRow2, exportRow);
    }

    private void createGrid() {
        H3 resultsTitle = new H3("Resultados");
        resultsTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Top.MEDIUM);

        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        add(resultsTitle, grid);
    }

    private void generateReport() {
        TipoReporte tipoReporte = tipoReporteCombo.getValue();
        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();

        if (tipoReporte == null) {
            showErrorNotification("Por favor seleccione un tipo de reporte");
            return;
        }

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            showErrorNotification("La fecha desde no puede ser posterior a la fecha hasta");
            return;
        }

        try {
            LocalDateTime fechaDesdeTime = desde != null ? desde.atStartOfDay() : LocalDateTime.now().minusMonths(1);
            LocalDateTime fechaHastaTime = hasta != null ? hasta.atTime(LocalTime.MAX) : LocalDateTime.now();

            switch (tipoReporte) {
                case PAGOS -> generatePagosReport(fechaDesdeTime, fechaHastaTime);
                case TRAMITES -> generateTramitesReport(fechaDesdeTime, fechaHastaTime);
                case LICENCIAS -> generateLicenciasReport(fechaDesdeTime.toLocalDate(), fechaHastaTime.toLocalDate());
                case EXAMENES -> generateExamenesReport(fechaDesdeTime, fechaHastaTime);
                case TURNOS -> generateTurnosReport(fechaDesdeTime, fechaHastaTime);
                case RECAUDACION -> generateRecaudacionReport(fechaDesdeTime, fechaHastaTime);
                case INHABILITACIONES -> generateInhabilitacionesReport();
                case RENDIMIENTO -> generateRendimientoReport(fechaDesdeTime, fechaHastaTime);
                case DASHBOARD -> generateDashboardReport();
            }

            updateExportButtons(true);
            showSuccessNotification("Reporte generado exitosamente");

        } catch (Exception e) {
            log.error("Error generando reporte: ", e);
            showErrorNotification("Error generando el reporte: " + e.getMessage());
        }
    }

    private void generatePagosReport(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = reporteService.getReporteRecaudacionPorPeriodo(desde, hasta);
        
        currentTitle = "Reporte de Pagos";
        currentHeaders = List.of("Concepto", "Valor");
        currentData = new ArrayList<>();
        
        currentData.add(List.of("Total Recaudado", reporte.get("totalRecaudado").toString()));
        currentData.add(List.of("Cantidad de Pagos", reporte.get("cantidadPagos").toString()));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> pagosPorMedio = (Map<String, Long>) reporte.get("pagosPorMedio");
        currentData.add(List.of("--- Pagos por Medio ---", ""));
        pagosPorMedio.forEach((medio, cantidad) -> 
            currentData.add(List.of(medio, cantidad.toString())));

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void generateTramitesReport(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = reporteService.getReporteTramitesPorPeriodo(desde, hasta);
        
        currentTitle = "Reporte de Trámites";
        currentHeaders = List.of("Estado/Tipo", "Cantidad");
        currentData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Long> tramitesPorEstado = (Map<String, Long>) reporte.get("tramitesPorEstado");
        currentData.add(List.of("--- Por Estado ---", ""));
        tramitesPorEstado.forEach((estado, cantidad) -> 
            currentData.add(List.of(estado, cantidad.toString())));

        @SuppressWarnings("unchecked")
        Map<String, Long> tramitesPorTipo = (Map<String, Long>) reporte.get("tramitesPorTipo");
        currentData.add(List.of("--- Por Tipo ---", ""));
        tramitesPorTipo.forEach((tipo, cantidad) -> 
            currentData.add(List.of(tipo, cantidad.toString())));

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void generateLicenciasReport(LocalDate desde, LocalDate hasta) {
        Map<String, Object> reporte = reporteService.getReporteLicenciasPorPeriodo(desde, hasta);
        
        currentTitle = "Reporte de Licencias";
        currentHeaders = List.of("Concepto", "Cantidad");
        currentData = new ArrayList<>();
        
        currentData.add(List.of("Total Licencias Emitidas", reporte.get("totalLicenciasEmitidas").toString()));
        currentData.add(List.of("Próximas a Vencer", reporte.get("licenciasProximasVencer").toString()));
        currentData.add(List.of("Vencidas", reporte.get("licenciasVencidas").toString()));

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void generateExamenesReport(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = reporteService.getReporteExamenesPorPeriodo(desde, hasta);
        
        currentTitle = "Reporte de Exámenes";
        currentHeaders = List.of("Tipo", "Total", "Aprobados", "% Aprobación");
        currentData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> teoricos = (Map<String, Object>) reporte.get("examenesTeoricos");
        currentData.add(List.of("Teóricos", 
            teoricos.get("total").toString(),
            teoricos.get("aprobados").toString(),
            String.format("%.1f%%", teoricos.get("porcentajeAprobacion"))));

        @SuppressWarnings("unchecked")
        Map<String, Object> practicos = (Map<String, Object>) reporte.get("examenesPracticos");
        currentData.add(List.of("Prácticos", 
            practicos.get("total").toString(),
            practicos.get("aprobados").toString(),
            String.format("%.1f%%", practicos.get("porcentajeAprobacion"))));

        @SuppressWarnings("unchecked")
        Map<String, Object> aptos = (Map<String, Object>) reporte.get("aptosMedicos");
        currentData.add(List.of("Aptos Médicos", 
            aptos.get("total").toString(),
            aptos.get("aptos").toString(),
            String.format("%.1f%%", aptos.get("porcentajeAptos"))));

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void generateTurnosReport(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = reporteService.getReporteTurnosPorPeriodo(desde, hasta);
        
        currentTitle = "Reporte de Turnos";
        currentHeaders = List.of("Estado", "Cantidad");
        currentData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Long> turnosPorEstado = (Map<String, Long>) reporte.get("turnosPorEstado");
        turnosPorEstado.forEach((estado, cantidad) -> 
            currentData.add(List.of(estado, cantidad.toString())));

        currentData.add(List.of("Total Turnos", reporte.get("totalTurnos").toString()));

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void generateRecaudacionReport(LocalDateTime desde, LocalDateTime hasta) {
        generatePagosReport(desde, hasta); // Mismo reporte que pagos
        currentTitle = "Reporte de Recaudación";
    }

    private void generateInhabilitacionesReport() {
        Map<String, Object> reporte = reporteService.getReporteInhabilitaciones();
        
        currentTitle = "Reporte de Inhabilitaciones";
        currentHeaders = List.of("Concepto", "Cantidad");
        currentData = new ArrayList<>();
        
        currentData.add(List.of("Inhabilitaciones Activas", reporte.get("inhabilitacionesActivas").toString()));
        currentData.add(List.of("Titulares Inhabilitados", reporte.get("titularesInhabilitados").toString()));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> porAutoridad = (Map<String, Long>) reporte.get("inhabilitacionesPorAutoridad");
        currentData.add(List.of("--- Por Autoridad ---", ""));
        porAutoridad.forEach((autoridad, cantidad) -> 
            currentData.add(List.of(autoridad, cantidad.toString())));

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void generateRendimientoReport(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Object> reporte = reporteService.getReporteRendimientoExaminadores(desde, hasta);
        
        currentTitle = "Reporte de Rendimiento";
        currentHeaders = List.of("Examinador", "Tipo", "Total", "Aprobados");
        currentData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> rendimiento = (Map<String, Map<String, Object>>) reporte.get("rendimientoPorExaminador");
        
        rendimiento.forEach((examinador, stats) -> {
            Long totalTeoricos = (Long) stats.getOrDefault("totalTeoricos", 0L);
            Long aprobadosTeoricos = (Long) stats.getOrDefault("aprobadosTeoricos", 0L);
            if (totalTeoricos > 0) {
                currentData.add(List.of(examinador, "Teóricos", totalTeoricos.toString(), aprobadosTeoricos.toString()));
            }
            
            Long totalPracticos = (Long) stats.getOrDefault("totalPracticos", 0L);
            Long aprobadosPracticos = (Long) stats.getOrDefault("aprobadosPracticos", 0L);
            if (totalPracticos > 0) {
                currentData.add(List.of(examinador, "Prácticos", totalPracticos.toString(), aprobadosPracticos.toString()));
            }
        });

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void generateDashboardReport() {
        Map<String, Object> dashboard = reporteService.getDashboard();
        
        currentTitle = "Dashboard General";
        currentHeaders = List.of("Concepto", "Valor");
        currentData = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> tramites = (Map<String, Object>) dashboard.get("tramites");
        currentData.add(List.of("--- Trámites ---", ""));
        tramites.forEach((tipo, cantidad) -> 
            currentData.add(List.of(tipo, cantidad.toString())));

        @SuppressWarnings("unchecked")
        Map<String, Object> licencias = (Map<String, Object>) dashboard.get("licencias");
        currentData.add(List.of("--- Licencias ---", ""));
        licencias.forEach((tipo, cantidad) -> 
            currentData.add(List.of(tipo, cantidad.toString())));

        currentData.add(List.of("Inhabilitaciones Activas", dashboard.get("inhabilitacionesActivas").toString()));
        currentData.add(List.of("Recaudación Mes Actual", dashboard.get("recaudacionMesActual").toString()));

        setupGrid(currentHeaders);
        populateGrid(currentData);
    }

    private void setupGrid(List<String> headers) {
        grid.removeAllColumns();
        
        for (int i = 0; i < headers.size(); i++) {
            final int index = i;
            grid.addColumn(item -> item.get("col" + index))
                .setHeader(headers.get(index))
                .setAutoWidth(true);
        }
    }

    private void populateGrid(List<List<String>> data) {
        List<Map<String, String>> gridData = data.stream()
            .map(row -> {
                Map<String, String> item = new HashMap<>();
                for (int i = 0; i < row.size(); i++) {
                    item.put("col" + i, row.get(i));
                }
                return item;
            })
            .collect(Collectors.toList());
        
        grid.setItems(gridData);
    }

    private void updateExportButtons(boolean enabled) {
        exportButton.setEnabled(enabled);
        exportExcelButton.setEnabled(enabled);
    }

    private void exportToCsv() {
        try {
            byte[] csvData = exportService.exportToCsv(currentTitle, currentHeaders, currentData);
            
            StreamResource resource = new StreamResource(
                currentTitle.replace(" ", "_") + ".csv",
                () -> new ByteArrayInputStream(csvData));
            
            resource.setContentType("text/csv");
            resource.setCacheTime(0);
            
            Anchor downloadAnchor = new Anchor(resource, "");
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.getElement().getStyle().set("display", "none");
            
            getElement().appendChild(downloadAnchor.getElement());
            downloadAnchor.getElement().executeJs("this.click()");
            getElement().removeChild(downloadAnchor.getElement());
            
            showSuccessNotification("Archivo CSV generado exitosamente");
            
        } catch (Exception e) {
            log.error("Error exportando a CSV: ", e);
            showErrorNotification("Error exportando a CSV: " + e.getMessage());
        }
    }

    private void exportToExcel() {
        try {
            byte[] excelData = exportService.exportToExcel(currentTitle, currentHeaders, currentData);
            
            StreamResource resource = new StreamResource(
                currentTitle.replace(" ", "_") + ".xlsx",
                () -> new ByteArrayInputStream(excelData));
            
            resource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resource.setCacheTime(0);
            
            Anchor downloadAnchor = new Anchor(resource, "");
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.getElement().getStyle().set("display", "none");
            
            getElement().appendChild(downloadAnchor.getElement());
            downloadAnchor.getElement().executeJs("this.click()");
            getElement().removeChild(downloadAnchor.getElement());
            
            showSuccessNotification("Archivo Excel generado exitosamente");
            
        } catch (Exception e) {
            log.error("Error exportando a Excel: ", e);
            showErrorNotification("Error exportando a Excel: " + e.getMessage());
        }
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
