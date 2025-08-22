package com.example.sigelic.views;

import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.service.ExamenService;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista para gestión de exámenes médicos y prácticos
 */
@Route(value = "examenes", layout = MainLayout.class)
@PageTitle("Exámenes | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "MEDICO", "EXAMINADOR"})
public class ExamenesView extends VerticalLayout {

    private final ExamenService examenService;
    private final TramiteService tramiteService;

    private Grid<ExamenWrapper> grid;
    private TextField searchField;
    private ComboBox<String> tipoFilter;
    private ListDataProvider<ExamenWrapper> dataProvider;

    public ExamenesView(ExamenService examenService, TramiteService tramiteService) {
        this.examenService = examenService;
        this.tramiteService = tramiteService;
        addClassName("examenes-view");
        setSizeFull();

        createHeader();
        createFilters();
        createGrid();
        loadData();
    }

    private void createHeader() {
        H2 title = new H2("Gestión de Exámenes");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addExamenButton = new Button("Nuevo Examen", new Icon(VaadinIcon.PLUS));
        addExamenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addExamenButton.addClickListener(e -> {
            // TODO: Implementar diálogo para nuevo examen
        });

        HorizontalLayout header = new HorizontalLayout(title, addExamenButton);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createFilters() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar exámenes...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");

        tipoFilter = new ComboBox<>("Tipo");
        tipoFilter.setItems("Todos", "Teórico", "Práctico");
        tipoFilter.setValue("Todos");
        tipoFilter.setWidth("150px");

        HorizontalLayout filters = new HorizontalLayout(searchField, tipoFilter);
        filters.setAlignItems(Alignment.END);
        filters.setWidthFull();

        add(filters);
    }

    private void createGrid() {
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Columnas del grid
        grid.addColumn(exam -> (exam.getTipo().equals("Teórico") ? "T-" : "P-") + exam.getId()).setHeader("ID").setWidth("100px");
        grid.addColumn(ExamenWrapper::getTipo).setHeader("Tipo").setWidth("120px");
        grid.addColumn(exam -> exam.getTramite().getId()).setHeader("Nº Trámite").setWidth("120px");
        grid.addColumn(exam -> exam.getTramite().getTitular().getNombre() + " " + 
                       exam.getTramite().getTitular().getApellido()).setHeader("Titular");
        grid.addColumn(exam -> exam.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
             .setHeader("Fecha").setWidth("150px");
        grid.addColumn(ExamenWrapper::getExaminador).setHeader("Examinador").setWidth("150px");
        grid.addColumn(exam -> exam.isAprobado() ? "Aprobado" : "Reprobado").setHeader("Resultado").setWidth("120px");

        // Columna de acciones
        grid.addComponentColumn(exam -> {
            HorizontalLayout acciones = new HorizontalLayout();
            acciones.setSpacing(true);

            // Botón para ver detalles
            Button verBtn = new Button("Ver", new Icon(VaadinIcon.EYE));
            verBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            verBtn.setTooltipText("Ver detalles del examen");
            verBtn.addClickListener(e -> verDetalleExamen(exam));
            acciones.add(verBtn);

            // Solo para exámenes teóricos reprobados: permitir reintento
            if ("Teórico".equals(exam.getTipo()) && !exam.isAprobado() && 
                exam.getTramite().getEstado() == com.example.sigelic.model.EstadoTramite.EX_TEO_RECHAZADO) {
                
                Button reintentoBtn = new Button("Reintento", new Icon(VaadinIcon.REFRESH));
                reintentoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                reintentoBtn.setTooltipText("Permitir reintento del examen teórico");
                reintentoBtn.addClickListener(e -> permitirReintentoExamenTeorico(exam));
                acciones.add(reintentoBtn);
            }

            return acciones;
        }).setHeader("Acciones").setWidth("150px");

        // Configurar data provider
        dataProvider = new ListDataProvider<>(new ArrayList<>());
        grid.setDataProvider(dataProvider);

        searchField.addValueChangeListener(e -> updateFilters());
        tipoFilter.addValueChangeListener(e -> updateFilters());

        add(grid);
    }

    private void updateFilters() {
        String searchTerm = searchField.getValue();
        String tipoSelected = tipoFilter.getValue();

        SerializablePredicate<ExamenWrapper> filter = exam -> {
            boolean matchesSearch = searchTerm == null || searchTerm.isEmpty() ||
                String.valueOf(exam.getId()).contains(searchTerm) ||
                (exam.getTramite().getTitular().getNombre() + " " + 
                 exam.getTramite().getTitular().getApellido()).toLowerCase().contains(searchTerm.toLowerCase()) ||
                (exam.getExaminador() != null && exam.getExaminador().toLowerCase().contains(searchTerm.toLowerCase()));

            boolean matchesTipo = "Todos".equals(tipoSelected) || exam.getTipo().equals(tipoSelected);

            return matchesSearch && matchesTipo;
        };

        dataProvider.setFilter(filter);
    }

    private void loadData() {
        List<ExamenWrapper> examenes = new ArrayList<>();

        // Cargar exámenes teóricos
        List<ExamenTeorico> teoricos = examenService.findAllTeoricos();
        for (ExamenTeorico teorico : teoricos) {
            examenes.add(new ExamenWrapper(teorico));
        }

        // Cargar exámenes prácticos
        List<ExamenPractico> practicos = examenService.findAllPracticos();
        for (ExamenPractico practico : practicos) {
            examenes.add(new ExamenWrapper(practico));
        }

        // Ordenar por fecha descendente
        examenes.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));

        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(examenes);
        dataProvider.refreshAll();
    }

    /**
     * Clase wrapper para unificar ExamenTeorico y ExamenPractico en el grid
     */
    public static class ExamenWrapper {
        private final ExamenTeorico examenTeorico;
        private final ExamenPractico examenPractico;
        private final String tipo;

        public ExamenWrapper(ExamenTeorico examenTeorico) {
            this.examenTeorico = examenTeorico;
            this.examenPractico = null;
            this.tipo = "Teórico";
        }

        public ExamenWrapper(ExamenPractico examenPractico) {
            this.examenTeorico = null;
            this.examenPractico = examenPractico;
            this.tipo = "Práctico";
        }

        public Long getId() {
            return examenTeorico != null ? examenTeorico.getId() : examenPractico.getId();
        }

        public String getTipo() {
            return tipo;
        }

        public com.example.sigelic.model.Tramite getTramite() {
            return examenTeorico != null ? examenTeorico.getTramite() : examenPractico.getTramite();
        }

        public java.time.LocalDateTime getFecha() {
            return examenTeorico != null ? examenTeorico.getFecha() : examenPractico.getFecha();
        }

        public String getExaminador() {
            String examinador = examenTeorico != null ? examenTeorico.getExaminador() : examenPractico.getExaminador();
            return examinador != null ? examinador : "No asignado";
        }

        public boolean isAprobado() {
            return examenTeorico != null ? examenTeorico.getAprobado() : examenPractico.getAprobado();
        }

        public String getDetalle() {
            if (examenTeorico != null) {
                return "Puntaje: " + examenTeorico.getPuntaje() + "/100";
            } else {
                return "Faltas leves: " + examenPractico.getFaltasLeves() + 
                       ", Faltas graves: " + examenPractico.getFaltasGraves();
            }
        }
    }

    private void verDetalleExamen(ExamenWrapper exam) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Detalle del Examen " + exam.getTipo());
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.add(new Span("ID: " + exam.getId()));
        content.add(new Span("Tipo: " + exam.getTipo()));
        content.add(new Span("Trámite: T" + String.format("%06d", exam.getTramite().getId())));
        content.add(new Span("Titular: " + exam.getTramite().getTitular().getNombre() + 
                            " " + exam.getTramite().getTitular().getApellido()));
        content.add(new Span("Fecha: " + exam.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        content.add(new Span("Examinador: " + exam.getExaminador()));
        content.add(new Span("Resultado: " + (exam.isAprobado() ? "Aprobado" : "Reprobado")));
        content.add(new Span("Detalle: " + exam.getDetalle()));

        if (exam.examenTeorico != null && exam.examenTeorico.getObservaciones() != null) {
            content.add(new Span("Observaciones: " + exam.examenTeorico.getObservaciones()));
        }

        dialog.add(content);

        Button cerrarBtn = new Button("Cerrar", e -> dialog.close());
        cerrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cerrarBtn);

        dialog.open();
    }

    private void permitirReintentoExamenTeorico(ExamenWrapper exam) {
        try {
            String motivo = "Reintento autorizado para examen teórico";
            tramiteService.permitirReintento(exam.getTramite().getId(), motivo);
            
            showNotification("Reintento autorizado. El trámite puede realizar un nuevo examen teórico.", 
                           NotificationVariant.LUMO_SUCCESS);
            
            loadData(); // Recargar datos
        } catch (Exception e) {
            showNotification("Error al autorizar reintento: " + e.getMessage(), 
                           NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setDuration(4000);
    }
}
