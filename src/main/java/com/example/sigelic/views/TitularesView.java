package com.example.sigelic.views;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.sigelic.model.Titular;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.views.dialog.EditarTitularDialog;
import com.example.sigelic.views.dialog.NuevoTitularDialog;
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
 * Vista para gestión de titulares
 */
@Route(value = "titulares", layout = MainLayout.class)
@PageTitle("Titulares | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR", "AGENTE"})
public class TitularesView extends VerticalLayout {

    private final TitularService titularService;
    private Grid<Titular> grid;
    private ListDataProvider<Titular> dataProvider;
    private TextField searchField;

    public TitularesView(TitularService titularService) {
        this.titularService = titularService;
        addClassName("titulares-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
        refreshGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestión de Titulares");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addTitularButton = new Button("Nuevo Titular", new Icon(VaadinIcon.PLUS));
        addTitularButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTitularButton.addClickListener(e -> openNuevoTitularDialog());

        HorizontalLayout header = new HorizontalLayout(title, addTitularButton);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar por nombre, apellido o DNI...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");
        
        searchField.addValueChangeListener(e -> {
            if (dataProvider != null) {
                dataProvider.setFilter(titular -> {
                    String searchTerm = e.getValue().toLowerCase().trim();
                    if (searchTerm.isEmpty()) {
                        return true;
                    }
                    
                    return titular.getNombre().toLowerCase().contains(searchTerm) ||
                           titular.getApellido().toLowerCase().contains(searchTerm) ||
                           titular.getDni().contains(searchTerm) ||
                           (titular.getNombre() + " " + titular.getApellido()).toLowerCase().contains(searchTerm);
                });
            }
        });

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>(Titular.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Columna DNI
        grid.addColumn(Titular::getDni)
            .setHeader("DNI")
            .setSortable(true)
            .setWidth("120px")
            .setFlexGrow(0);

        // Columna Nombre completo
        grid.addColumn(titular -> titular.getNombre() + " " + titular.getApellido())
            .setHeader("Nombre Completo")
            .setSortable(true);

        // Columna Edad
        grid.addColumn(new ComponentRenderer<>(titular -> {
            int edad = titular.getEdad();
            Span edadSpan = new Span(String.valueOf(edad) + " años");
            
            // Colorear según la edad para clases de licencia
            if (edad < 17) {
                edadSpan.getElement().getThemeList().add("badge error");
                edadSpan.getElement().setAttribute("title", "Menor de edad para cualquier licencia");
            } else if (edad < 21) {
                edadSpan.getElement().getThemeList().add("badge contrast");
                edadSpan.getElement().setAttribute("title", "Apto para clases A y B únicamente");
            } else {
                edadSpan.getElement().getThemeList().add("badge success");
                edadSpan.getElement().setAttribute("title", "Apto para todas las clases de licencia");
            }
            
            return edadSpan;
        }))
        .setHeader("Edad")
        .setSortable(true)
        .setWidth("100px")
        .setFlexGrow(0);

        // Columna Fecha de nacimiento
        grid.addColumn(titular -> titular.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .setHeader("Fecha Nacimiento")
            .setSortable(true)
            .setWidth("140px")
            .setFlexGrow(0);

        // Columna Domicilio
        grid.addColumn(Titular::getDomicilio)
            .setHeader("Domicilio")
            .setSortable(true);

        // Columna Email
        grid.addColumn(titular -> titular.getEmail() != null ? titular.getEmail() : "Sin email")
            .setHeader("Email")
            .setSortable(true);

        // Columna Teléfono
        grid.addColumn(titular -> titular.getTelefono() != null ? titular.getTelefono() : "Sin teléfono")
            .setHeader("Teléfono")
            .setSortable(true)
            .setWidth("120px")
            .setFlexGrow(0);

        // Columna Estado (inhabilitaciones)
        grid.addColumn(new ComponentRenderer<>(titular -> {
            boolean tieneInhabilitaciones = titular.tieneInhabilitacionesActivas();
            Span estadoSpan = new Span(tieneInhabilitaciones ? "Inhabilitado" : "Habilitado");
            
            if (tieneInhabilitaciones) {
                estadoSpan.getElement().getThemeList().add("badge error");
            } else {
                estadoSpan.getElement().getThemeList().add("badge success");
            }
            
            return estadoSpan;
        }))
        .setHeader("Estado")
        .setSortable(true)
        .setWidth("120px")
        .setFlexGrow(0);

        // Columna Acciones
        grid.addColumn(new ComponentRenderer<>(titular -> {
            Button viewButton = new Button(new Icon(VaadinIcon.EYE));
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            viewButton.getElement().setAttribute("aria-label", "Ver detalles");
            viewButton.getElement().setAttribute("title", "Ver detalles del titular");
            viewButton.addClickListener(e -> viewTitularDetails(titular));

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editButton.getElement().setAttribute("aria-label", "Editar");
            editButton.getElement().setAttribute("title", "Editar titular");
            editButton.addClickListener(e -> editTitular(titular));

            HorizontalLayout actions = new HorizontalLayout(viewButton, editButton);
            actions.setSpacing(false);
            return actions;
        }))
        .setHeader("Acciones")
        .setWidth("120px")
        .setFlexGrow(0);

        // Configurar selección
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addSelectionListener(selection -> {
            selection.getFirstSelectedItem().ifPresent(this::showTitularInfo);
        });

        add(grid);
    }

    private void refreshGrid() {
        try {
            List<Titular> titulares = titularService.findAllWithInhabilitaciones();
            dataProvider = new ListDataProvider<>(titulares);
            grid.setDataProvider(dataProvider);
            
            showNotification("Cargados " + titulares.size() + " titulares", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Error al cargar titulares: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void openNuevoTitularDialog() {
        NuevoTitularDialog dialog = new NuevoTitularDialog(titularService, unused -> refreshGrid());
        dialog.open();
    }

    private void viewTitularDetails(Titular titular) {
        // TODO: Implementar diálogo de detalles del titular
        showNotification("Ver detalles de: " + titular.getNombre() + " " + titular.getApellido(), 
                        NotificationVariant.LUMO_CONTRAST);
    }

    private void editTitular(Titular titular) {
        EditarTitularDialog dialog = new EditarTitularDialog(titular, titularService, unused -> refreshGrid());
        dialog.open();
    }

    private void showTitularInfo(Titular titular) {
        // Mostrar información adicional cuando se selecciona un titular
        String info = String.format("Seleccionado: %s %s (DNI: %s, Edad: %d años)", 
                                   titular.getNombre(), titular.getApellido(), 
                                   titular.getDni(), titular.getEdad());
        
        Notification notification = Notification.show(info);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.setDuration(3000);
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
