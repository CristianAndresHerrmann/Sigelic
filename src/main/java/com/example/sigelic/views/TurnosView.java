package com.example.sigelic.views;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.sigelic.model.EstadoTurno;
import com.example.sigelic.model.TipoTurno;
import com.example.sigelic.model.Turno;
import com.example.sigelic.repository.RecursoRepository;
import com.example.sigelic.security.Authorities;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.service.TurnoService;
import com.example.sigelic.views.dialog.ReservarTurnoDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "turnos", layout = MainLayout.class)
@PageTitle("Turnos | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "AGENTE"})
public class TurnosView extends VerticalLayout {

    private final TurnoService turnoService;
    private final TitularService titularService;
    private final RecursoRepository recursoRepository;
    private final AuthorityChecker authorityChecker;

    private Grid<Turno> grid;
    private ListDataProvider<Turno> dataProvider;
    
    private TextField searchField;
    private ComboBox<TipoTurno> tipoFilter;
    private ComboBox<EstadoTurno> estadoFilter;

    public TurnosView(TurnoService turnoService, TitularService titularService, RecursoRepository recursoRepository, AuthorityChecker authorityChecker) {
        this.turnoService = turnoService;
        this.titularService = titularService;
        this.recursoRepository = recursoRepository;
        this.authorityChecker = authorityChecker;
        
        addClassName("turnos-view");
        setSizeFull();

        createHeader();
        createFilters();
        createGrid();
        refreshGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestión de Turnos");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addTurnoButton = new Button("Reservar Turno", new Icon(VaadinIcon.PLUS));
        addTurnoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTurnoButton.addClickListener(e -> openReservarTurnoDialog());

        HorizontalLayout header = new HorizontalLayout(title);
        if (authorityChecker.has(Authorities.TURNO_ASIGNAR)) {
            header.add(addTurnoButton);
        }
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createFilters() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar por nombre, apellido o DNI...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("350px");
        searchField.addValueChangeListener(e -> applyFilters());

        tipoFilter = new ComboBox<>("Tipo de Turno");
        tipoFilter.setItems(TipoTurno.values());
        tipoFilter.setItemLabelGenerator(TipoTurno::getDescripcion);
        tipoFilter.setClearButtonVisible(true);
        tipoFilter.setWidth("220px");
        tipoFilter.addValueChangeListener(e -> applyFilters());

        estadoFilter = new ComboBox<>("Estado");
        estadoFilter.setItems(EstadoTurno.values());
        estadoFilter.setItemLabelGenerator(EstadoTurno::getDescripcion);
        estadoFilter.setClearButtonVisible(true);
        estadoFilter.setWidth("180px");
        estadoFilter.addValueChangeListener(e -> applyFilters());

        HorizontalLayout filters = new HorizontalLayout(searchField, tipoFilter, estadoFilter);
        filters.setAlignItems(Alignment.END);
        filters.addClassName(LumoUtility.Margin.Bottom.SMALL);
        add(filters);
    }

    private void createGrid() {
        grid = new Grid<>(Turno.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(turno -> "TR" + String.format("%06d", turno.getId())).setHeader("ID").setWidth("90px").setFlexGrow(0);
        grid.addColumn(turno -> turno.getTipo().getDescripcion()).setHeader("Tipo").setSortable(true);
        grid.addColumn(turno -> turno.getTitular() != null ? 
                turno.getTitular().getNombre() + " " + turno.getTitular().getApellido() + " (" + turno.getTitular().getDni() + ")" : "")
            .setHeader("Titular").setSortable(true);
        grid.addColumn(turno -> turno.getInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setHeader("Fecha/Hora").setSortable(true);
        grid.addColumn(turno -> turno.getRecurso() != null ? turno.getRecurso().getNombre() : "").setHeader("Recurso").setSortable(true);
        grid.addColumn(turno -> turno.getProfesionalAsignado() != null ? turno.getProfesionalAsignado() : "Sin asignar").setHeader("Profesional");

        grid.addColumn(new ComponentRenderer<>(turno -> {
            Span badge = new Span(turno.getEstado().getDescripcion());
            switch (turno.getEstado()) {
                case RESERVADO:
                    badge.getElement().getThemeList().add("badge");
                    break;
                case CONFIRMADO:
                    badge.getElement().getThemeList().add("badge success");
                    break;
                case COMPLETADO:
                    badge.getElement().getThemeList().add("badge success primary");
                    break;
                case CANCELADO:
                    badge.getElement().getThemeList().add("badge error");
                    break;
                case AUSENTE:
                    badge.getElement().getThemeList().add("badge contrast");
                    break;
            }
            return badge;
        })).setHeader("Estado").setSortable(true).setWidth("130px").setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(this::createActionsColumn)).setHeader("Acciones").setWidth("320px").setFlexGrow(0);

        add(grid);
    }

    private HorizontalLayout createActionsColumn(Turno turno) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        if (turno.getEstado() == EstadoTurno.RESERVADO && authorityChecker.has(Authorities.TURNO_REPROGRAMAR)) {
            Button confirmarBtn = new Button(new Icon(VaadinIcon.CHECK));
            confirmarBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            confirmarBtn.setTooltipText("Confirmar turno");
            confirmarBtn.addClickListener(e -> confirmarTurno(turno));
            actions.add(confirmarBtn);
        }

        if ((turno.getEstado() == EstadoTurno.RESERVADO || turno.getEstado() == EstadoTurno.CONFIRMADO) 
                && authorityChecker.has(Authorities.TURNO_REPROGRAMAR)) {
            
            Button absentBtn = new Button(new Icon(VaadinIcon.USER));
            absentBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
            absentBtn.setTooltipText("Marcar ausente");
            absentBtn.addClickListener(e -> marcarAusente(turno));
            
            Button completeBtn = new Button(new Icon(VaadinIcon.SIGN_IN));
            completeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            completeBtn.setTooltipText("Completar turno");
            completeBtn.addClickListener(e -> abrirCompletarDialog(turno));

            actions.add(absentBtn, completeBtn);
        }

        if (turno.getEstado() != EstadoTurno.COMPLETADO && turno.getEstado() != EstadoTurno.CANCELADO 
                && authorityChecker.has(Authorities.TURNO_CANCELAR)) {
            Button cancelBtn = new Button(new Icon(VaadinIcon.CLOSE));
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            cancelBtn.setTooltipText("Cancelar turno");
            cancelBtn.addClickListener(e -> abrirCancelarDialog(turno));
            actions.add(cancelBtn);
        }

        if (turno.getEstado() != EstadoTurno.CANCELADO && authorityChecker.has(Authorities.TURNO_REPROGRAMAR)) {
            Button asignarProfBtn = new Button(new Icon(VaadinIcon.DOCTOR));
            asignarProfBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            asignarProfBtn.setTooltipText("Asignar profesional");
            asignarProfBtn.addClickListener(e -> abrirAsignarProfesionalDialog(turno));
            actions.add(asignarProfBtn);
        }

        return actions;
    }

    private void applyFilters() {
        if (dataProvider == null) return;

        String searchTerm = searchField.getValue();
        TipoTurno selectedTipo = tipoFilter.getValue();
        EstadoTurno selectedEstado = estadoFilter.getValue();

        dataProvider.setFilter(turno -> {
            boolean matchesSearch = true;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String term = searchTerm.toLowerCase().trim();
                matchesSearch = (turno.getTitular() != null && 
                                (turno.getTitular().getNombre().toLowerCase().contains(term) ||
                                 turno.getTitular().getApellido().toLowerCase().contains(term) ||
                                 turno.getTitular().getDni().contains(term)));
            }

            boolean matchesTipo = selectedTipo == null || turno.getTipo() == selectedTipo;
            boolean matchesEstado = selectedEstado == null || turno.getEstado() == selectedEstado;

            return matchesSearch && matchesTipo && matchesEstado;
        });
    }

    private void refreshGrid() {
        try {
            // Obtener turnos de las últimas semanas y próximas semanas (o todos si son pocos)
            // Para simplificar, traemos los turnos en un rango de 30 días
            java.time.LocalDateTime desde = java.time.LocalDateTime.now().minusDays(30);
            java.time.LocalDateTime hasta = java.time.LocalDateTime.now().plusDays(30);
            List<Turno> turnos = turnoService.findTurnosEnPeriodo(desde, hasta);
            
            dataProvider = new ListDataProvider<>(turnos);
            grid.setDataProvider(dataProvider);
            applyFilters();
        } catch (Exception e) {
            showNotification("Error al cargar turnos: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void openReservarTurnoDialog() {
        ReservarTurnoDialog dialog = new ReservarTurnoDialog(turnoService, titularService, recursoRepository, unused -> refreshGrid());
        dialog.open();
    }

    private void confirmarTurno(Turno turno) {
        try {
            turnoService.confirmarTurno(turno.getId());
            showNotification("Turno confirmado exitosamente", NotificationVariant.LUMO_SUCCESS);
            refreshGrid();
        } catch (Exception e) {
            showNotification("Error al confirmar turno: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void marcarAusente(Turno turno) {
        try {
            turnoService.marcarAusente(turno.getId());
            showNotification("Turno marcado como ausente", NotificationVariant.LUMO_SUCCESS);
            refreshGrid();
        } catch (Exception e) {
            showNotification("Error al marcar ausente: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void abrirCompletarDialog(Turno turno) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Completar Turno TR" + String.format("%06d", turno.getId()));
        dialog.setWidth("400px");

        TextArea obsField = new TextArea("Observaciones de finalización");
        obsField.setWidthFull();
        obsField.setMaxLength(500);

        Button confirmarBtn = new Button("Completar", e -> {
            try {
                turnoService.completarTurno(turno.getId(), obsField.getValue());
                showNotification("Turno completado correctamente", NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                showNotification("Error al completar turno: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(obsField);
        dialog.getFooter().add(confirmarBtn, cancelarBtn);
        dialog.open();
    }

    private void abrirCancelarDialog(Turno turno) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cancelar Turno TR" + String.format("%06d", turno.getId()));
        dialog.setWidth("400px");

        TextArea motivoField = new TextArea("Motivo de la cancelación");
        motivoField.setWidthFull();
        motivoField.setMaxLength(500);
        motivoField.setRequired(true);

        Button confirmarBtn = new Button("Confirmar Cancelación", e -> {
            String motivo = motivoField.getValue();
            if (motivo == null || motivo.trim().isEmpty()) {
                showNotification("El motivo es obligatorio", NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                turnoService.cancelarTurno(turno.getId(), motivo);
                showNotification("Turno cancelado correctamente", NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                showNotification("Error al cancelar turno: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelarBtn = new Button("Volver", e -> dialog.close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(motivoField);
        dialog.getFooter().add(confirmarBtn, cancelarBtn);
        dialog.open();
    }

    private void abrirAsignarProfesionalDialog(Turno turno) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Asignar Profesional");
        dialog.setWidth("400px");

        TextField profField = new TextField("Nombre del profesional");
        profField.setWidthFull();
        if (turno.getProfesionalAsignado() != null) {
            profField.setValue(turno.getProfesionalAsignado());
        }

        Button confirmarBtn = new Button("Asignar", e -> {
            try {
                turnoService.asignarProfesional(turno.getId(), profField.getValue());
                showNotification("Profesional asignado correctamente", NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                showNotification("Error: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(profField);
        dialog.getFooter().add(confirmarBtn, cancelarBtn);
        dialog.open();
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
