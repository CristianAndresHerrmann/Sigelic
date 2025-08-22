package com.example.sigelic.views;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.LicenciaService;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.service.TramiteService;
import com.example.sigelic.views.dialog.DetalleTramiteDialog;
import com.example.sigelic.views.dialog.NuevoTramiteDialog;
import com.example.sigelic.views.dialog.RegistrarAptoMedicoDialog;
import com.example.sigelic.views.dialog.ValidarDocumentacionDialog;
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
    private final LicenciaService licenciaService;
    private Grid<Tramite> grid;
    private ListDataProvider<Tramite> dataProvider;
    private TextField searchField;

    public TramitesView(TramiteService tramiteService, TitularService titularService, LicenciaService licenciaService) {
        this.tramiteService = tramiteService;
        this.titularService = titularService;
        this.licenciaService = licenciaService;
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
                case DOCS_RECHAZADAS:
                case APTO_MED_RECHAZADO:
                case EX_TEO_RECHAZADO:
                case EX_PRA_RECHAZADO:
                    badge.getElement().getThemeList().add("badge error");
                    break;
                default:
                    badge.getElement().getThemeList().add("badge");
            }
            return badge;
        })).setHeader("Estado").setSortable(true);

        // Columna de acciones
        grid.addColumn(new ComponentRenderer<>(tramite -> {
            HorizontalLayout acciones = new HorizontalLayout();
            acciones.setSpacing(true);

            // Botón para validar documentación (solo si está en INICIADO)
            if (tramite.getEstado() == EstadoTramite.INICIADO) {
                Button validarDocsBtn = new Button("Validar Docs", new Icon(VaadinIcon.CHECK));
                validarDocsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                validarDocsBtn.setTooltipText("Validar documentación presentada");
                validarDocsBtn.addClickListener(e -> abrirDialogoValidarDocumentacion(tramite));
                acciones.add(validarDocsBtn);
            }

            // Botón para apto médico (solo si está en DOCS_OK y requiere apto médico)
            if (tramite.getEstado() == EstadoTramite.DOCS_OK && tramite.requiereAptoMedico()) {
                Button aptoMedicoBtn = new Button("Apto Médico", new Icon(VaadinIcon.STETHOSCOPE));
                aptoMedicoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                aptoMedicoBtn.setTooltipText("Registrar examen médico");
                aptoMedicoBtn.addClickListener(e -> abrirDialogoAptoMedico(tramite));
                acciones.add(aptoMedicoBtn);
            }

            // Botón para examen teórico (si está en APTO_MED y requiere examen teórico)
            if (tramite.getEstado() == EstadoTramite.APTO_MED && tramite.requiereExamenTeorico()) {
                Button examenTeoricoBtn = new Button("Ex. Teórico", new Icon(VaadinIcon.BOOK));
                examenTeoricoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                examenTeoricoBtn.setTooltipText("Registrar examen teórico");
                examenTeoricoBtn.addClickListener(e -> 
                    showNotification("Examen teórico - Por implementar", NotificationVariant.LUMO_CONTRAST));
                acciones.add(examenTeoricoBtn);
            }

            // Botón para examen práctico (si está en EX_TEO_OK y requiere examen práctico)
            if (tramite.getEstado() == EstadoTramite.EX_TEO_OK && tramite.requiereExamenPractico()) {
                Button examenPracticoBtn = new Button("Ex. Práctico", new Icon(VaadinIcon.CAR));
                examenPracticoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                examenPracticoBtn.setTooltipText("Registrar examen práctico");
                examenPracticoBtn.addClickListener(e -> 
                    showNotification("Examen práctico - Por implementar", NotificationVariant.LUMO_CONTRAST));
                acciones.add(examenPracticoBtn);
            }

            // Botón para pago (si está en EX_PRA_OK o cualquier estado que permita pago)
            if ((tramite.getEstado() == EstadoTramite.EX_PRA_OK) || 
                (tramite.getEstado() == EstadoTramite.APTO_MED && !tramite.requiereExamenTeorico() && !tramite.requiereExamenPractico()) ||
                (tramite.getEstado() == EstadoTramite.EX_TEO_OK && !tramite.requiereExamenPractico())) {
                Button pagoBtn = new Button("Pago", new Icon(VaadinIcon.CREDIT_CARD));
                pagoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                pagoBtn.setTooltipText("Registrar pago");
                pagoBtn.addClickListener(e -> 
                    showNotification("Registro de pago - Por implementar", NotificationVariant.LUMO_CONTRAST));
                acciones.add(pagoBtn);
            }

            // Botón para emitir licencia (si está en PAGO_OK)
            if (tramite.getEstado() == EstadoTramite.PAGO_OK) {
                Button emitirBtn = new Button("Emitir", new Icon(VaadinIcon.DIPLOMA));
                emitirBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                emitirBtn.setTooltipText("Emitir licencia");
                emitirBtn.addClickListener(e -> 
                    showNotification("Emisión de licencia - Por implementar", NotificationVariant.LUMO_CONTRAST));
                acciones.add(emitirBtn);
            }

            // Botones para reintentos (si el trámite está rechazado pero permite reintento)
            if (tramite.getEstado().esRechazo() && tramite.getEstado().permiteReintento()) {
                Button reintentoBtn = new Button("Permitir Reintento", new Icon(VaadinIcon.REFRESH));
                reintentoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                reintentoBtn.setTooltipText("Permitir reintento del trámite");
                reintentoBtn.addClickListener(e -> permitirReintento(tramite));
                acciones.add(reintentoBtn);
            }

            // Botón para ver detalles (siempre disponible)
            Button detalleBtn = new Button("Ver", new Icon(VaadinIcon.EYE));
            detalleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            detalleBtn.setTooltipText("Ver detalle completo del trámite");
            detalleBtn.addClickListener(e -> verDetalleTramite(tramite));
            acciones.add(detalleBtn);

            return acciones;
        })).setHeader("Acciones").setWidth("250px");

        add(grid);
    }

    private void abrirDialogoAptoMedico(Tramite tramite) {
        RegistrarAptoMedicoDialog dialog = new RegistrarAptoMedicoDialog(tramiteService, tramite, unused -> refreshGrid());
        dialog.open();
    }

    private void abrirDialogoValidarDocumentacion(Tramite tramite) {
        ValidarDocumentacionDialog dialog = new ValidarDocumentacionDialog(tramiteService, tramite, unused -> refreshGrid());
        dialog.open();
    }

    private void validarDocumentacion(Tramite tramite) {
        try {
            tramiteService.validarDocumentacion(tramite.getId());
            showNotification("Documentación validada exitosamente", NotificationVariant.LUMO_SUCCESS);
            refreshGrid();
        } catch (Exception e) {
            showNotification("Error al validar documentación: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void verDetalleTramite(Tramite tramite) {
        DetalleTramiteDialog dialog = new DetalleTramiteDialog(tramiteService, tramite);
        dialog.open();
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
        NuevoTramiteDialog dialog = new NuevoTramiteDialog(tramiteService, titularService, licenciaService, unused -> refreshGrid());
        dialog.open();
    }

    private void permitirReintento(Tramite tramite) {
        try {
            String motivo = "Reintento autorizado por el sistema";
            tramiteService.permitirReintento(tramite.getId(), motivo);
            showNotification("Reintento autorizado exitosamente", NotificationVariant.LUMO_SUCCESS);
            refreshGrid();
        } catch (Exception e) {
            showNotification("Error al autorizar reintento: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }
}
