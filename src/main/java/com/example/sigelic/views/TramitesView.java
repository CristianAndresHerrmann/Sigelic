package com.example.sigelic.views;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.service.TramiteService;
import com.example.sigelic.views.dialog.NuevoTramiteDialog;
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

/**
 * Vista para gestión de trámites
 */
@Route(value = "tramites", layout = MainLayout.class)
@PageTitle("Trámites | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "AGENTE", "EXAMINADOR", "CAJERO"})
public class TramitesView extends VerticalLayout {

    private final TramiteService tramiteService;
    private final TitularService titularService;
    private Grid<Tramite> grid;
    private ListDataProvider<Tramite> dataProvider;
    private TextField searchField;

    public TramitesView(TramiteService tramiteService, TitularService titularService) {
        this.tramiteService = tramiteService;
        this.titularService = titularService;
        addClassName("tramites-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
        refreshGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestión de Trámites");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addTramiteButton = new Button("Nuevo Trámite", new Icon(VaadinIcon.PLUS));
        addTramiteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTramiteButton.addClickListener(e -> openNuevoTramiteDialog());

        HorizontalLayout header = new HorizontalLayout(title, addTramiteButton);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar trámites...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");
        
        searchField.addValueChangeListener(e -> {
            if (dataProvider != null) {
                dataProvider.setFilter(tramite -> {
                    String searchTerm = e.getValue().toLowerCase();
                    String numeroTramite = "T" + String.format("%06d", tramite.getId());
                    return numeroTramite.toLowerCase().contains(searchTerm) ||
                           (tramite.getTitular() != null && 
                            (tramite.getTitular().getNombre().toLowerCase().contains(searchTerm) ||
                             tramite.getTitular().getApellido().toLowerCase().contains(searchTerm))) ||
                           tramite.getTipo().toString().toLowerCase().contains(searchTerm) ||
                           tramite.getEstado().getDescripcion().toLowerCase().contains(searchTerm);
                });
            }
        });

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>(Tramite.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Columnas con datos reales
        grid.addColumn(tramite -> "T" + String.format("%06d", tramite.getId()))
            .setHeader("Número").setSortable(true);
        grid.addColumn(tramite -> tramite.getTipo().toString()).setHeader("Tipo").setSortable(true);
        grid.addColumn(tramite -> tramite.getTitular() != null ? 
                      tramite.getTitular().getNombre() + " " + tramite.getTitular().getApellido() : "")
            .setHeader("Solicitante").setSortable(true);
        grid.addColumn(tramite -> tramite.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .setHeader("Fecha Inicio").setSortable(true);
        
        // Columna de estado con badge
        grid.addColumn(new ComponentRenderer<>(tramite -> {
            Span badge = new Span(tramite.getEstado().getDescripcion());
            switch (tramite.getEstado()) {
                case INICIADO:
                    badge.getElement().getThemeList().add("badge");
                    break;
                case DOCS_OK:
                case APTO_MED:
                case EX_TEO_OK:
                case EX_PRA_OK:
                case PAGO_OK:
                    badge.getElement().getThemeList().add("badge contrast");
                    break;
                case EMITIDA:
                    badge.getElement().getThemeList().add("badge success");
                    break;
                case RECHAZADA:
                    badge.getElement().getThemeList().add("badge error");
                    break;
                default:
                    badge.getElement().getThemeList().add("badge");
            }
            return badge;
        })).setHeader("Estado").setSortable(true);

        add(grid);
    }

    private void refreshGrid() {
        try {
            List<Tramite> tramites = tramiteService.findAll();
            dataProvider = new ListDataProvider<>(tramites);
            grid.setDataProvider(dataProvider);
        } catch (Exception e) {
            showNotification("Error al cargar trámites: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }

    private void openNuevoTramiteDialog() {
        NuevoTramiteDialog dialog = new NuevoTramiteDialog(tramiteService, titularService, unused -> refreshGrid());
        dialog.open();
    }
}
