 package com.example.sigelic.views;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.sigelic.model.Licencia;
import com.example.sigelic.security.Authorities;
import com.example.sigelic.service.LicenciaService;
import com.example.sigelic.views.dialog.VerLicenciaDialog;
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
 * Vista para gestión de licencias de conducir
 */
@Route(value = "licencias", layout = MainLayout.class)
@PageTitle("Licencias | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "AGENTE", "EXAMINADOR"})
public class LicenciasView extends VerticalLayout {

    private final LicenciaService licenciaService;
    private final AuthorityChecker authorityChecker;
    private Grid<Licencia> grid;
    private ListDataProvider<Licencia> dataProvider;
    private TextField searchField;

    public LicenciasView(LicenciaService licenciaService, AuthorityChecker authorityChecker) {
        this.licenciaService = licenciaService;
        this.authorityChecker = authorityChecker;
        addClassName("licencias-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
        refreshGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestión de Licencias");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addLicenseButton = new Button("Nueva Licencia", new Icon(VaadinIcon.PLUS));
        addLicenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addLicenseButton.addClickListener(e -> {
            // TODO: Implementar diálogo para nueva licencia
        });

        HorizontalLayout header = new HorizontalLayout(title);
        if (authorityChecker.has(Authorities.LICENCIA_EMITIR)) {
            header.add(addLicenseButton);
        }
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar licencias...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");
        
        searchField.addValueChangeListener(e -> {
            if (dataProvider != null) {
                dataProvider.setFilter(licencia -> {
                    String searchTerm = e.getValue().toLowerCase();
                    return (licencia.getNumeroLicencia() != null && 
                            licencia.getNumeroLicencia().toLowerCase().contains(searchTerm)) ||
                           (licencia.getTitular() != null && 
                            (licencia.getTitular().getNombre().toLowerCase().contains(searchTerm) ||
                             licencia.getTitular().getApellido().toLowerCase().contains(searchTerm))) ||
                           licencia.getClase().toString().toLowerCase().contains(searchTerm) ||
                           licencia.getEstado().getDescripcion().toLowerCase().contains(searchTerm);
                });
            }
        });

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>(Licencia.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Columnas con datos reales
        grid.addColumn(Licencia::getNumeroLicencia)
            .setHeader("Número").setSortable(true);
        grid.addColumn(licencia -> licencia.getTitular() != null ? 
                      licencia.getTitular().getNombre() + " " + licencia.getTitular().getApellido() : "")
            .setHeader("Titular").setSortable(true);
        grid.addColumn(licencia -> licencia.getClase().toString())
            .setHeader("Clase").setSortable(true);
        grid.addColumn(licencia -> licencia.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .setHeader("Vencimiento").setSortable(true);
        
        // Columna de estado con badge
        grid.addColumn(new ComponentRenderer<>(licencia -> {
            Span badge = new Span(licencia.getEstado().getDescripcion());
            switch (licencia.getEstado()) {
                case VIGENTE:
                    badge.getElement().getThemeList().add("badge success");
                    break;
                case VENCIDA:
                    badge.getElement().getThemeList().add("badge error");
                    break;
                case SUSPENDIDA:
                case INHABILITADA:
                    badge.getElement().getThemeList().add("badge contrast");
                    break;
                case DUPLICADA:
                    badge.getElement().getThemeList().add("badge");
                    break;
                default:
                    badge.getElement().getThemeList().add("badge");
            }
            return badge;
        })).setHeader("Estado").setSortable(true);

        // Columna de acciones
        grid.addColumn(new ComponentRenderer<>(licencia -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button verButton = new Button(new Icon(VaadinIcon.EYE));
            verButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            verButton.setTooltipText("Ver licencia");
            verButton.addClickListener(e -> abrirVerLicenciaDialog(licencia));

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.setTooltipText("Editar licencia");
            editButton.addClickListener(e -> {
                // TODO: Implementar edición de licencia
                showNotification("Funcionalidad en desarrollo", NotificationVariant.LUMO_CONTRAST);
            });

            actions.add(verButton);
            if (authorityChecker.has(Authorities.LICENCIA_GESTIONAR_ESTADO)) {
                actions.add(editButton);
            }
            return actions;
        })).setHeader("Acciones").setWidth("120px").setFlexGrow(0);

        add(grid);
    }

    private void abrirVerLicenciaDialog(Licencia licencia) {
        VerLicenciaDialog dialog = new VerLicenciaDialog(licencia);
        dialog.open();
    }

    private void refreshGrid() {
        try {
            List<Licencia> licencias = licenciaService.findAll();
            dataProvider = new ListDataProvider<>(licencias);
            grid.setDataProvider(dataProvider);
        } catch (Exception e) {
            e.printStackTrace(); // Imprimir stack trace completo
            showNotification("Error al cargar licencias: " + e.getClass().getSimpleName() + " - " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
