package com.example.sigelic.views;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.example.sigelic.model.CostoTramite;
import com.example.sigelic.service.CostoTramiteService;
import com.example.sigelic.views.dialog.EditarCostoDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

/**
 * Vista de administración de costos de trámites (parametrización, solo ADMINISTRADOR)
 */
@Route(value = "costos", layout = MainLayout.class)
@PageTitle("Costos de Trámites | SIGELIC")
@RolesAllowed({"SUPERADMIN", "ADMINISTRADOR"})
@Slf4j
public class CostosView extends VerticalLayout {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CostoTramiteService costoTramiteService;
    private Grid<CostoTramite> grid;
    private TextField searchField;
    private ListDataProvider<CostoTramite> dataProvider;

    public CostosView(CostoTramiteService costoTramiteService) {
        this.costoTramiteService = costoTramiteService;
        addClassName("costos-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
        loadCostos();
    }

    private void createHeader() {
        H2 title = new H2("Costos de Trámites");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Span subtitle = new Span("Las combinaciones de tipo de trámite y clase de licencia son fijas. "
                + "Para modificar un precio, edite el costo vigente de la fila correspondiente.");
        subtitle.getElement().getThemeList().add("badge contrast");

        HorizontalLayout header = new HorizontalLayout(title);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header, subtitle);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar por tipo de trámite, clase o descripción...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterCostos());

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>(CostoTramite.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(costo -> costo.getTipoTramite().getDescripcion())
            .setHeader("Tipo de Trámite")
            .setSortable(true);

        grid.addColumn(costo -> costo.getClaseLicencia().name() + " - " + costo.getClaseLicencia().getDescripcion())
            .setHeader("Clase")
            .setSortable(true);

        grid.addColumn(costo -> CURRENCY_FORMAT.format(costo.getCosto()))
            .setHeader("Costo")
            .setWidth("130px")
            .setFlexGrow(0);

        grid.addColumn(costo -> costo.getFechaVigenciaDesde() != null
                ? costo.getFechaVigenciaDesde().format(DATE_FORMATTER) : "-")
            .setHeader("Vigente desde")
            .setComparator(Comparator.comparing(CostoTramite::getFechaVigenciaDesde,
                    Comparator.nullsFirst(Comparator.naturalOrder())))
            .setSortable(true)
            .setWidth("130px")
            .setFlexGrow(0);

        grid.addColumn(costo -> costo.getFechaVigenciaHasta() != null
                ? costo.getFechaVigenciaHasta().format(DATE_FORMATTER) : "Sin límite")
            .setHeader("Vigente hasta")
            .setComparator(Comparator.comparing(CostoTramite::getFechaVigenciaHasta,
                    Comparator.nullsLast(Comparator.naturalOrder())))
            .setSortable(true)
            .setWidth("130px")
            .setFlexGrow(0);

        grid.addComponentColumn(this::createEstadoBadge)
            .setHeader("Estado")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.addComponentColumn(this::createAcciones)
            .setHeader("Acciones")
            .setAutoWidth(true)
            .setFlexGrow(0);

        add(grid);
    }

    private Span createEstadoBadge(CostoTramite costo) {
        boolean vigente = Boolean.TRUE.equals(costo.getActivo()) && costo.isVigente();
        Span badge;
        if (!Boolean.TRUE.equals(costo.getActivo())) {
            badge = new Span("Inactivo");
            badge.getElement().getThemeList().add("badge error");
        } else if (vigente) {
            badge = new Span("Vigente");
            badge.getElement().getThemeList().add("badge success");
        } else {
            badge = new Span("Fuera de vigencia");
            badge.getElement().getThemeList().add("badge contrast");
        }
        return badge;
    }

    private HorizontalLayout createAcciones(CostoTramite costo) {
        HorizontalLayout acciones = new HorizontalLayout();
        acciones.setSpacing(false);

        boolean vigente = Boolean.TRUE.equals(costo.getActivo()) && costo.isVigente();

        if (vigente) {
            Button editarBtn = new Button("Editar", new Icon(VaadinIcon.EDIT));
            editarBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editarBtn.setTooltipText("Actualizar el costo vigente de esta combinación");
            editarBtn.addClickListener(e -> {
                EditarCostoDialog dialog = new EditarCostoDialog(costoTramiteService, costo, unused -> loadCostos());
                dialog.open();
            });
            acciones.add(editarBtn);
        }

        if (Boolean.TRUE.equals(costo.getActivo())) {
            Button desactivarBtn = new Button("Desactivar", new Icon(VaadinIcon.BAN));
            desactivarBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            desactivarBtn.setTooltipText("Desactivar este costo");
            desactivarBtn.addClickListener(e -> confirmarDesactivacion(costo));
            acciones.add(desactivarBtn);
        }

        return acciones;
    }

    private void confirmarDesactivacion(CostoTramite costo) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar desactivación");
        dialog.setText("¿Desactivar el costo de " + costo.getTipoTramite().getDescripcion()
                + " clase " + costo.getClaseLicencia().name() + " ("
                + CURRENCY_FORMAT.format(costo.getCosto()) + ")? Dejará de considerarse al generar órdenes de pago.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Desactivar");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> desactivarCosto(costo));
        dialog.open();
    }

    private void desactivarCosto(CostoTramite costo) {
        try {
            costoTramiteService.desactivarCosto(costo.getId());
            Notification.show("Costo desactivado", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadCostos();
        } catch (Exception e) {
            log.error("Error al desactivar costo ID {}", costo.getId(), e);
            Notification.show("Error al desactivar el costo: " + e.getMessage(),
                    4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadCostos() {
        try {
            List<CostoTramite> costos = costoTramiteService.findAll();
            dataProvider = new ListDataProvider<>(costos);
            grid.setDataProvider(dataProvider);
            filterCostos();
        } catch (Exception e) {
            log.error("Error al cargar costos", e);
            Notification.show("Error al cargar los costos: " + e.getMessage(),
                    3000, Notification.Position.MIDDLE);
        }
    }

    private void filterCostos() {
        if (dataProvider == null) return;

        String filterText = searchField.getValue();
        if (filterText == null || filterText.trim().isEmpty()) {
            dataProvider.clearFilters();
        } else {
            String filter = filterText.toLowerCase().trim();
            dataProvider.setFilter(costo ->
                costo.getTipoTramite().getDescripcion().toLowerCase().contains(filter) ||
                costo.getClaseLicencia().name().toLowerCase().contains(filter) ||
                costo.getClaseLicencia().getDescripcion().toLowerCase().contains(filter) ||
                (costo.getDescripcion() != null && costo.getDescripcion().toLowerCase().contains(filter))
            );
        }
    }
}
