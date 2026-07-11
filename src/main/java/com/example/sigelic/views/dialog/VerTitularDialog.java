package com.example.sigelic.views.dialog;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.sigelic.model.Inhabilitacion;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.Titular;
import com.example.sigelic.model.Tramite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Diálogo de solo lectura con el detalle completo de un titular:
 * datos personales, inhabilitaciones, licencias y trámites (CU-005)
 */
public class VerTitularDialog extends Dialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Titular titular;
    private final List<Licencia> licencias;
    private final List<Tramite> tramites;

    public VerTitularDialog(Titular titular, List<Licencia> licencias, List<Tramite> tramites) {
        this.titular = titular;
        this.licencias = licencias;
        this.tramites = tramites;

        setHeaderTitle("Detalle del Titular");
        setModal(true);
        setWidth("640px");
        setMaxHeight("80%");
        setResizable(false);

        createContent();
        createFooter();
    }

    private void createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        content.add(createDatosPersonalesSection());
        content.add(createInhabilitacionesSection());
        content.add(createLicenciasSection());
        content.add(createTramitesSection());

        add(content);
    }

    private VerticalLayout createDatosPersonalesSection() {
        VerticalLayout section = createSection("Datos personales");

        Span nombre = new Span(titular.getApellido().toUpperCase() + ", " + titular.getNombre());
        nombre.getStyle().set("font-weight", "bold").set("font-size", "1.1em");

        section.add(nombre,
                createInfoField("DNI", titular.getDni()),
                createInfoField("Fecha de nacimiento", titular.getFechaNacimiento() != null
                        ? titular.getFechaNacimiento().format(DATE_FORMATTER) + " (" + titular.getEdad() + " años)"
                        : "N/A"),
                createInfoField("Domicilio", valueOrDash(titular.getDomicilio())),
                createInfoField("Email", valueOrDash(titular.getEmail())),
                createInfoField("Teléfono", valueOrDash(titular.getTelefono())));
        return section;
    }

    private VerticalLayout createInhabilitacionesSection() {
        VerticalLayout section = createSection("Inhabilitaciones");

        List<Inhabilitacion> inhabilitaciones = titular.getInhabilitaciones();
        if (inhabilitaciones == null || inhabilitaciones.isEmpty()) {
            section.add(createEmptyLabel("El titular no posee inhabilitaciones registradas"));
            return section;
        }

        for (Inhabilitacion inhabilitacion : inhabilitaciones) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setSpacing(true);

            Span badge = new Span(inhabilitacion.isActiva() ? "ACTIVA" : "FINALIZADA");
            badge.getElement().getThemeList().add(inhabilitacion.isActiva() ? "badge error" : "badge contrast");

            String vigencia = format(inhabilitacion.getFechaInicio()) + " - "
                    + (inhabilitacion.getFechaFin() != null ? format(inhabilitacion.getFechaFin()) : "sin fecha de fin");
            Span detalle = new Span(inhabilitacion.getMotivo() + " (" + inhabilitacion.getAutoridad() + ") · " + vigencia);
            detalle.getStyle().set("font-size", "0.9em");

            row.add(badge, detalle);
            section.add(row);
        }
        return section;
    }

    private VerticalLayout createLicenciasSection() {
        VerticalLayout section = createSection("Licencias");

        if (licencias == null || licencias.isEmpty()) {
            section.add(createEmptyLabel("El titular no posee licencias registradas"));
            return section;
        }

        for (Licencia licencia : licencias) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setSpacing(true);

            Span badge = new Span(licencia.getEstado().getDescripcion());
            badge.getElement().getThemeList().add(licencia.isVigente() ? "badge success" : "badge contrast");

            Span detalle = new Span("N° " + valueOrDash(licencia.getNumeroLicencia())
                    + " · Clase " + licencia.getClase().name()
                    + " · Vence: " + format(licencia.getFechaVencimiento()));
            detalle.getStyle().set("font-size", "0.9em");

            row.add(badge, detalle);
            section.add(row);
        }
        return section;
    }

    private VerticalLayout createTramitesSection() {
        VerticalLayout section = createSection("Trámites");

        if (tramites == null || tramites.isEmpty()) {
            section.add(createEmptyLabel("El titular no posee trámites registrados"));
            return section;
        }

        for (Tramite tramite : tramites) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setSpacing(true);

            Span badge = new Span(tramite.getEstado().getDescripcion());
            badge.getElement().getThemeList().add("badge");

            Span detalle = new Span("T" + tramite.getId() + " · " + tramite.getTipo().getDescripcion()
                    + " · Clase " + tramite.getClaseSolicitada().name()
                    + (tramite.getFechaCreacion() != null
                            ? " · " + tramite.getFechaCreacion().format(DATE_FORMATTER)
                            : ""));
            detalle.getStyle().set("font-size", "0.9em");

            row.add(badge, detalle);
            section.add(row);
        }
        return section;
    }

    private VerticalLayout createSection(String titulo) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("margin-bottom", "1rem");

        H4 header = new H4(titulo);
        header.getStyle().set("margin", "0 0 0.4rem 0");
        section.add(header);
        return section;
    }

    private HorizontalLayout createInfoField(String label, String value) {
        HorizontalLayout field = new HorizontalLayout();
        field.setSpacing(true);
        field.setAlignItems(FlexComponent.Alignment.CENTER);
        field.getStyle().set("margin", "2px 0");

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "0.9em")
                .set("min-width", "150px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-size", "0.9em");

        field.add(labelSpan, valueSpan);
        return field;
    }

    private Span createEmptyLabel(String texto) {
        Span label = new Span(texto);
        label.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.9em");
        return label;
    }

    private String format(java.time.LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : "N/A";
    }

    private String valueOrDash(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private void createFooter() {
        Button cerrarButton = new Button("Cerrar", new Icon(VaadinIcon.CLOSE), e -> close());
        cerrarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getFooter().add(cerrarButton);
    }
}
