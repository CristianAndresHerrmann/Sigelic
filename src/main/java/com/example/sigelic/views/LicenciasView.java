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
import com.vaadin.flow.component.dialog.Dialog;
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

/**
 * Vista para gestión de licencias de conducir
 */
@Route(value = "licencias", layout = MainLayout.class)
@PageTitle("Licencias | SIGELIC")
@RolesAllowed({"SUPERADMIN", "ADMINISTRADOR", "SUPERVISOR", "AGENTE", "EXAMINADOR", "AUDITOR"})
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

        Button actualizarVencidasBtn = new Button("Actualizar Vencidas", new Icon(VaadinIcon.REFRESH));
        actualizarVencidasBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        actualizarVencidasBtn.addClickListener(e -> ejecutarActualizarVencidas());

        HorizontalLayout header = new HorizontalLayout(title);
        
        HorizontalLayout actions = new HorizontalLayout();
        if (authorityChecker.has(Authorities.PROCESO_VENCIMIENTOS_EJECUTAR)) {
            actions.add(actualizarVencidasBtn);
        }
        header.add(actions);
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
            actions.add(verButton);

            if (licencia.getEstado() == com.example.sigelic.model.EstadoLicencia.VIGENTE 
                    && authorityChecker.has(Authorities.LICENCIA_GESTIONAR_ESTADO)) {
                
                Button suspenderBtn = new Button("Suspender", new Icon(VaadinIcon.CLOSE));
                suspenderBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                suspenderBtn.setTooltipText("Suspender licencia");
                suspenderBtn.addClickListener(e -> abrirSuspenderDialog(licencia));
                
                Button inhabilitarBtn = new Button("Inhabilitar", new Icon(VaadinIcon.BAN));
                inhabilitarBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                inhabilitarBtn.setTooltipText("Inhabilitar licencia");
                inhabilitarBtn.addClickListener(e -> abrirInhabilitarDialog(licencia));
                
                actions.add(suspenderBtn, inhabilitarBtn);
            }
            return actions;
        })).setHeader("Acciones").setWidth("320px").setFlexGrow(0);

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
            e.printStackTrace();
            showNotification("Error al cargar licencias: " + e.getClass().getSimpleName() + " - " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void abrirSuspenderDialog(Licencia licencia) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Suspender Licencia " + licencia.getNumeroLicencia());
        dialog.setWidth("400px");
        
        TextArea motivoField = new TextArea("Motivo de la suspensión");
        motivoField.setWidthFull();
        motivoField.setMaxLength(500);
        motivoField.setRequired(true);
        
        Button confirmarBtn = new Button("Confirmar", e -> {
            String motivo = motivoField.getValue();
            if (motivo == null || motivo.trim().isEmpty()) {
                showNotification("El motivo es obligatorio", NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                licenciaService.suspenderLicencia(licencia.getId(), motivo);
                showNotification("Licencia suspendida exitosamente", NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                showNotification("Error: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        
        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        dialog.add(motivoField);
        dialog.getFooter().add(confirmarBtn, cancelarBtn);
        dialog.open();
    }

    private void abrirInhabilitarDialog(Licencia licencia) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Inhabilitar Licencia " + licencia.getNumeroLicencia());
        dialog.setWidth("400px");
        
        TextArea motivoField = new TextArea("Motivo de la inhabilitación");
        motivoField.setWidthFull();
        motivoField.setMaxLength(500);
        motivoField.setRequired(true);
        
        Button confirmarBtn = new Button("Confirmar", e -> {
            String motivo = motivoField.getValue();
            if (motivo == null || motivo.trim().isEmpty()) {
                showNotification("El motivo es obligatorio", NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                licenciaService.inhabilitarLicencia(licencia.getId(), motivo);
                showNotification("Licencia inhabilitada exitosamente", NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                showNotification("Error: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        
        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        dialog.add(motivoField);
        dialog.getFooter().add(confirmarBtn, cancelarBtn);
        dialog.open();
    }

    private void ejecutarActualizarVencidas() {
        try {
            licenciaService.actualizarLicenciasVencidas();
            showNotification("Licencias vencidas actualizadas correctamente", NotificationVariant.LUMO_SUCCESS);
            refreshGrid();
        } catch (Exception e) {
            showNotification("Error al actualizar vencimientos: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
