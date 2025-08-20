package com.example.sigelic.views;

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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

/**
 * Vista para gestión de pagos y tasas
 */
@Route(value = "pagos", layout = MainLayout.class)
@PageTitle("Pagos | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "CAJERO", "AUDITOR"})
public class PagosView extends VerticalLayout {

    private Grid<String> grid; // Placeholder hasta implementar entidad Pago
    private TextField searchField;

    public PagosView() {
        addClassName("pagos-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
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
        searchField.setPlaceholder("Buscar pagos...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Placeholder columns
        grid.addColumn(item -> "P" + System.currentTimeMillis()).setHeader("Número");
        grid.addColumn(item -> "Tasa Renovación").setHeader("Concepto");
        grid.addColumn(item -> "$15.000").setHeader("Monto");
        grid.addColumn(item -> "Juan Pérez").setHeader("Pagador");
        grid.addColumn(item -> "2024-11-15").setHeader("Fecha");
        grid.addColumn(item -> {
            Span badge = new Span("Pagado");
            badge.getElement().getThemeList().add("badge success");
            return badge;
        }).setHeader("Estado");

        // Placeholder data
        grid.setItems("Item 1", "Item 2", "Item 3");

        add(grid);
    }
}
