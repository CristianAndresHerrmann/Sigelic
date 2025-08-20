package com.example.sigelic.views;

import com.example.sigelic.model.Pago;
import com.example.sigelic.model.EstadoPago;
import com.example.sigelic.service.PagoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Vista para gestión de pagos y tasas
 */
@Route(value = "pagos", layout = MainLayout.class)
@PageTitle("Pagos | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "CAJERO", "AUDITOR"})
@Slf4j
public class PagosView extends VerticalLayout {

    private final PagoService pagoService;
    private Grid<Pago> grid;
    private TextField searchField;
    private ListDataProvider<Pago> dataProvider;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));

    public PagosView(PagoService pagoService) {
        this.pagoService = pagoService;
        addClassName("pagos-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
        loadPagos();
    }

    private void createHeader() {
        H2 title = new H2("Gestión de Pagos");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addPagoButton = new Button("Registrar Pago", new Icon(VaadinIcon.PLUS));
        addPagoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addPagoButton.addClickListener(e -> {
            // TODO: Implementar diálogo para registrar pago
        });

        HorizontalLayout header = new HorizontalLayout(title, addPagoButton);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar pagos por trámite, comprobante o transacción...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterPagos());

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>(Pago.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Columna número de pago
        grid.addColumn(pago -> "P" + pago.getId())
            .setHeader("Número")
            .setWidth("100px")
            .setFlexGrow(0);

        // Columna trámite
        grid.addColumn(pago -> pago.getTramite() != null ? 
            "T" + pago.getTramite().getId() + " - " + 
            pago.getTramite().getTipo().getDescripcion() : "N/A")
            .setHeader("Trámite")
            .setWidth("200px");

        // Columna monto
        grid.addColumn(pago -> CURRENCY_FORMAT.format(pago.getMonto()))
            .setHeader("Monto")
            .setWidth("120px")
            .setFlexGrow(0);

        // Columna titular/pagador
        grid.addColumn(pago -> pago.getTramite() != null && 
            pago.getTramite().getTitular() != null ? 
            pago.getTramite().getTitular().getNombre() + " " + 
            pago.getTramite().getTitular().getApellido() : "N/A")
            .setHeader("Pagador")
            .setWidth("200px");

        // Columna fecha
        grid.addColumn(pago -> pago.getFecha() != null ? 
            pago.getFecha().format(DATE_FORMATTER) : "N/A")
            .setHeader("Fecha")
            .setWidth("150px")
            .setFlexGrow(0);

        // Columna medio de pago
        grid.addColumn(pago -> pago.getMedio().getDescripcion())
            .setHeader("Medio")
            .setWidth("120px")
            .setFlexGrow(0);

        // Columna estado con badge
        grid.addComponentColumn(this::createEstadoBadge)
            .setHeader("Estado")
            .setWidth("120px")
            .setFlexGrow(0);

        // Columna número de comprobante
        grid.addColumn(Pago::getNumeroComprobante)
            .setHeader("Comprobante")
            .setWidth("150px")
            .setFlexGrow(0);

        add(grid);
    }

    private Span createEstadoBadge(Pago pago) {
        Span badge = new Span(pago.getEstado().getDescripcion());
        
        switch (pago.getEstado()) {
            case ACREDITADO:
                badge.getElement().getThemeList().add("badge success");
                break;
            case PENDIENTE:
                badge.getElement().getThemeList().add("badge");
                break;
            case RECHAZADO:
                badge.getElement().getThemeList().add("badge error");
                break;
            case VENCIDO:
                badge.getElement().getThemeList().add("badge contrast");
                break;
        }
        
        return badge;
    }

    private void loadPagos() {
        try {
            log.info("Cargando lista de pagos");
            List<Pago> pagos = pagoService.findAll();
            dataProvider = new ListDataProvider<>(pagos);
            grid.setDataProvider(dataProvider);
            log.info("Cargados {} pagos exitosamente", pagos.size());
        } catch (Exception e) {
            log.error("Error al cargar pagos", e);
            Notification.show("Error al cargar los pagos: " + e.getMessage(), 
                3000, Notification.Position.MIDDLE);
        }
    }

    private void filterPagos() {
        if (dataProvider == null) return;
        
        String filterText = searchField.getValue();
        if (filterText == null || filterText.trim().isEmpty()) {
            dataProvider.clearFilters();
        } else {
            String filter = filterText.toLowerCase().trim();
            dataProvider.setFilter(pago -> 
                (pago.getTramite() != null && 
                 String.valueOf(pago.getTramite().getId()).contains(filter)) ||
                (pago.getNumeroComprobante() != null && 
                 pago.getNumeroComprobante().toLowerCase().contains(filter)) ||
                (pago.getNumeroTransaccion() != null && 
                 pago.getNumeroTransaccion().toLowerCase().contains(filter)) ||
                (pago.getTramite() != null && pago.getTramite().getTitular() != null &&
                 (pago.getTramite().getTitular().getNombre().toLowerCase().contains(filter) ||
                  pago.getTramite().getTitular().getApellido().toLowerCase().contains(filter)))
            );
        }
    }
}
