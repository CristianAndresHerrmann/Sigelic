package com.example.sigelic.views;

import com.example.sigelic.model.ExamenTeorico;
import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.service.ExamenService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

    private Grid<ExamenWrapper> grid;
    private TextField searchField;
    private ComboBox<String> tipoFilter;
    private ListDataProvider<ExamenWrapper> dataProvider;

    public ExamenesView(ExamenService examenService) {
        this.examenService = examenService;
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
        grid.addColumn(exam -> "T-" + exam.getId()).setHeader("ID").setWidth("100px");
        grid.addColumn(ExamenWrapper::getTipo).setHeader("Tipo").setWidth("120px");
        grid.addColumn(exam -> exam.getTramite().getId()).setHeader("Nº Trámite").setWidth("120px");
        grid.addColumn(exam -> exam.getTramite().getTitular().getNombre() + " " + 
                       exam.getTramite().getTitular().getApellido()).setHeader("Titular");
        grid.addColumn(exam -> exam.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
             .setHeader("Fecha").setWidth("150px");
        grid.addColumn(ExamenWrapper::getExaminador).setHeader("Examinador").setWidth("150px");
        grid.addColumn(exam -> {
            Span badge = new Span(exam.isAprobado() ? "Aprobado" : "Reprobado");
            badge.getElement().getThemeList().add("badge " + (exam.isAprobado() ? "success" : "error"));
            return badge;
        }).setHeader("Resultado").setWidth("120px");

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
}
