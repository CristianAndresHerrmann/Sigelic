package com.example.sigelic.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Vista para generación de reportes
 */
@Route(value = "reportes", layout = MainLayout.class)
@PageTitle("Reportes | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "AUDITOR"})
public class ReportesView extends VerticalLayout {

    private ComboBox<String> tipoReporteCombo;
    private DatePicker fechaDesde;
    private DatePicker fechaHasta;
    private Grid<String> grid;

    public ReportesView() {
        addClassName("reportes-view");
        setSizeFull();

        createHeader();
        createFilters();
        createGrid();
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
        tipoReporteCombo.setItems(Arrays.asList(
            "Licencias Emitidas",
            "Trámites por Estado",
            "Exámenes Realizados",
            "Pagos Recibidos",
            "Usuarios Activos",
            "Licencias Vencidas",
            "Estadísticas Generales"
        ));
        tipoReporteCombo.setWidthFull();

        fechaDesde = new DatePicker("Fecha Desde");
        fechaDesde.setValue(LocalDate.now().minusMonths(1));

        fechaHasta = new DatePicker("Fecha Hasta");
        fechaHasta.setValue(LocalDate.now());

        Button generateButton = new Button("Generar Reporte", new Icon(VaadinIcon.CHART));
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generateButton.addClickListener(e -> generateReport());

        Button exportButton = new Button("Exportar PDF", new Icon(VaadinIcon.FILE_TEXT));
        exportButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        exportButton.addClickListener(e -> exportToPdf());

        Button exportExcelButton = new Button("Exportar Excel", new Icon(VaadinIcon.FILE_TABLE));
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

        // Placeholder columns (cambiarán según el tipo de reporte)
        grid.addColumn(item -> "Dato 1").setHeader("Columna 1");
        grid.addColumn(item -> "Dato 2").setHeader("Columna 2");
        grid.addColumn(item -> "Dato 3").setHeader("Columna 3");
        grid.addColumn(item -> "Dato 4").setHeader("Columna 4");

        add(resultsTitle, grid);
    }

    private void generateReport() {
        String tipoReporte = tipoReporteCombo.getValue();
        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();

        if (tipoReporte == null) {
            // TODO: Mostrar notificación de error
            return;
        }

        // TODO: Implementar generación de reportes según el tipo seleccionado
        // Por ahora, datos de ejemplo
        grid.setItems("Reporte 1", "Reporte 2", "Reporte 3");
    }

    private void exportToPdf() {
        // TODO: Implementar exportación a PDF
    }

    private void exportToExcel() {
        // TODO: Implementar exportación a Excel
    }
}
