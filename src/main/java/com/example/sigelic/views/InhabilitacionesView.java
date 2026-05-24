package com.example.sigelic.views;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.sigelic.model.Inhabilitacion;
import com.example.sigelic.repository.InhabilitacionRepository;
import com.example.sigelic.security.Authorities;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.views.dialog.RegistrarInhabilitacionDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "inhabilitaciones", layout = MainLayout.class)
@PageTitle("Inhabilitaciones | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "AUDITOR"})
public class InhabilitacionesView extends VerticalLayout {

    private final InhabilitacionRepository inhabilitacionRepository;
    private final TitularService titularService;
    private final AuthorityChecker authorityChecker;

    private Grid<Inhabilitacion> grid;
    private ListDataProvider<Inhabilitacion> dataProvider;
    private TextField searchField;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InhabilitacionesView(InhabilitacionRepository inhabilitacionRepository, TitularService titularService, AuthorityChecker authorityChecker) {
        this.inhabilitacionRepository = inhabilitacionRepository;
        this.titularService = titularService;
        this.authorityChecker = authorityChecker;

        addClassName("inhabilitaciones-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
        refreshGrid();
    }

    private void createHeader() {
        H2 title = new H2("Registro de Inhabilitaciones");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addBtn = new Button("Registrar Inhabilitación", new Icon(VaadinIcon.PLUS));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openRegistrarInhabilitacionDialog());

        HorizontalLayout header = new HorizontalLayout(title);
        if (authorityChecker.has(Authorities.INHABILITACION_GESTIONAR)) {
            header.add(addBtn);
        }
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar por nombre, apellido, DNI o expediente...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("450px");
        searchField.addValueChangeListener(e -> applyFilters());

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>(Inhabilitacion.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(i -> "INH" + String.format("%05d", i.getId())).setHeader("ID").setWidth("100px").setFlexGrow(0);
        grid.addColumn(i -> i.getTitular() != null ? 
                i.getTitular().getNombre() + " " + i.getTitular().getApellido() + " (" + i.getTitular().getDni() + ")" : "")
            .setHeader("Titular").setSortable(true).setWidth("220px");
        grid.addColumn(Inhabilitacion::getMotivo).setHeader("Motivo").setSortable(true).setWidth("200px");
        grid.addColumn(i -> i.getFechaInicio().format(DATE_FORMATTER)).setHeader("Inicio").setSortable(true).setWidth("120px").setFlexGrow(0);
        grid.addColumn(i -> i.getFechaFin() != null ? i.getFechaFin().format(DATE_FORMATTER) : "Permanente").setHeader("Fin").setSortable(true).setWidth("120px").setFlexGrow(0);
        grid.addColumn(Inhabilitacion::getAutoridad).setHeader("Autoridad").setSortable(true).setWidth("180px");
        grid.addColumn(Inhabilitacion::getNumeroExpediente).setHeader("Expediente").setSortable(true).setWidth("150px");

        grid.addColumn(new ComponentRenderer<>(i -> {
            boolean activa = i.isActiva();
            Span badge = new Span(activa ? "Activa" : "Vencida");
            if (activa) {
                badge.getElement().getThemeList().add("badge error");
            } else {
                badge.getElement().getThemeList().add("badge success");
            }
            return badge;
        })).setHeader("Estado").setWidth("110px").setFlexGrow(0);

        add(grid);
    }

    private void applyFilters() {
        if (dataProvider == null) return;

        String searchTerm = searchField.getValue();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            dataProvider.clearFilters();
        } else {
            String term = searchTerm.toLowerCase().trim();
            dataProvider.setFilter(i -> 
                (i.getTitular() != null && 
                 (i.getTitular().getNombre().toLowerCase().contains(term) ||
                  i.getTitular().getApellido().toLowerCase().contains(term) ||
                  i.getTitular().getDni().contains(term))) ||
                (i.getMotivo() != null && i.getMotivo().toLowerCase().contains(term)) ||
                (i.getNumeroExpediente() != null && i.getNumeroExpediente().toLowerCase().contains(term)) ||
                (i.getAutoridad() != null && i.getAutoridad().toLowerCase().contains(term))
            );
        }
    }

    private void refreshGrid() {
        try {
            List<Inhabilitacion> inhabilitaciones = inhabilitacionRepository.findAllWithTitular();
            dataProvider = new ListDataProvider<>(inhabilitaciones);
            grid.setDataProvider(dataProvider);
            applyFilters();
        } catch (Exception e) {
            showNotification("Error al cargar inhabilitaciones: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void openRegistrarInhabilitacionDialog() {
        RegistrarInhabilitacionDialog dialog = new RegistrarInhabilitacionDialog(titularService, unused -> refreshGrid());
        dialog.open();
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
