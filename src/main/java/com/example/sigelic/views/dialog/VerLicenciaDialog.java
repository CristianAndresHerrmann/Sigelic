package com.example.sigelic.views.dialog;

import com.example.sigelic.model.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;

/**
 * Dialog que muestra la vista de una licencia de conducir
 * similar al diseño oficial argentino
 */
public class VerLicenciaDialog extends Dialog {

    private final Licencia licencia;

    public VerLicenciaDialog(Licencia licencia) {
        this.licencia = licencia;
        initializeDialog();
        createLicenciaCard();
    }

    private void initializeDialog() {
        setHeaderTitle("Licencia Nacional de Conducir");
        setWidth("800px");
        setModal(true);
        setDraggable(false);
        setResizable(false);
        getElement().getStyle().set("max-height", "90vh");
    }

    private void createLicenciaCard() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        // Frente del carnet
        Div licenciaCard = createLicenciaCardLayout();

        // Dorso del carnet (clase y descripción)
        Div dorsoCard = createDorsoCardLayout();

        // Botones de acción
        HorizontalLayout buttonLayout = createButtonLayout();

        mainLayout.add(licenciaCard, dorsoCard, buttonLayout);

        add(mainLayout);
    }

    private Div createLicenciaCardLayout() {
        Div card = new Div();
        card.getStyle()
                .set("background", "linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 50%, #90CAF9 100%)")
                .set("border", "2px solid #1976D2")
                .set("border-radius", "15px")
                .set("padding", "20px")
                .set("margin", "20px")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("height", "300px");

        // Header con título
        Div header = createHeader();
        
        // Contenido principal
        HorizontalLayout content = createMainContent();
        
        // Footer con información oficial
        Div footer = createFooter();

        // Patrón decorativo (simulando el diseño oficial)
        Div pattern = createDecorativePattern();

        card.add(pattern, header, content, footer);
        return card;
    }

    private Div createHeader() {
        Div header = new Div();
        header.getStyle()
                .set("position", "relative")
                .set("z-index", "10")
                .set("margin-bottom", "15px");

        H2 title = new H2("Licencia Nacional de Conducir");
        title.getStyle()
                .set("color", "#1565C0")
                .set("font-size", "18px")
                .set("font-weight", "bold")
                .set("margin", "0")
                .set("text-align", "center")
                .set("text-shadow", "1px 1px 2px rgba(255,255,255,0.8)");

        header.add(title);
        return header;
    }

    private HorizontalLayout createMainContent() {
        HorizontalLayout content = new HorizontalLayout();
        content.setSpacing(true);
        content.setSizeFull();
        content.setAlignItems(FlexComponent.Alignment.START);
        content.getStyle()
                .set("position", "relative")
                .set("z-index", "10");

        // Foto del titular (placeholder)
        Div photoDiv = createPhotoSection();
        
        // Información del titular
        VerticalLayout infoLayout = createInfoSection();

        content.add(photoDiv, infoLayout);
        content.setFlexGrow(0, photoDiv);
        content.setFlexGrow(1, infoLayout);

        return content;
    }

    private Div createPhotoSection() {
        Div photoDiv = new Div();
        photoDiv.getStyle()
                .set("width", "120px")
                .set("height", "140px")
                .set("background-color", "#F5F5F5")
                .set("border", "2px solid #1976D2")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-right", "20px");

        // Placeholder para la foto
        VerticalLayout placeholder = new VerticalLayout();
        placeholder.setSpacing(false);
        placeholder.setPadding(false);
        placeholder.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon personIcon = new Icon(VaadinIcon.USER);
        personIcon.setSize("40px");
        personIcon.setColor("#757575");

        Span photoText = new Span("FOTO");
        photoText.getStyle()
                .set("font-size", "10px")
                .set("color", "#757575")
                .set("font-weight", "bold");

        placeholder.add(personIcon, photoText);
        photoDiv.add(placeholder);

        return photoDiv;
    }

    private VerticalLayout createInfoSection() {
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);

        Titular titular = licencia.getTitular();

        // Nombre completo
        H3 nombreCompleto = new H3(titular.getApellido().toUpperCase() + ", " + titular.getNombre().toUpperCase());
        nombreCompleto.getStyle()
                .set("color", "#1565C0")
                .set("font-size", "16px")
                .set("font-weight", "bold")
                .set("margin", "0 0 10px 0");

        // Información personal
        VerticalLayout personalInfo = new VerticalLayout();
        personalInfo.setSpacing(false);
        personalInfo.setPadding(false);

        personalInfo.add(
            createInfoField("DNI:", titular.getDni()),
            createInfoField("FECHA NAC:", titular.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
            createInfoField("DOMICILIO:", titular.getDomicilio()),
            createInfoField("CLASE:", licencia.getClase().name()),
            createInfoField("N° LICENCIA:", licencia.getNumeroLicencia()),
            createInfoField("F. EMISIÓN:", licencia.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
            createInfoField("F. VENCIMIENTO:", licencia.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        );

        infoLayout.add(nombreCompleto, personalInfo);
        return infoLayout;
    }

    private HorizontalLayout createInfoField(String label, String value) {
        HorizontalLayout field = new HorizontalLayout();
        field.setSpacing(true);
        field.setAlignItems(FlexComponent.Alignment.CENTER);
        field.getStyle().set("margin", "2px 0");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("color", "#1565C0")
                .set("min-width", "100px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#333333");

        field.add(labelSpan, valueSpan);
        return field;
    }

    private Div createFooter() {
        Div footer = new Div();
        footer.getStyle()
                .set("position", "absolute")
                .set("bottom", "10px")
                .set("left", "20px")
                .set("right", "20px")
                .set("z-index", "10");

        HorizontalLayout footerContent = new HorizontalLayout();
        footerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        footerContent.setWidthFull();

        // Logo República Argentina (texto)
        Span republica = new Span("REPÚBLICA ARGENTINA");
        republica.getStyle()
                .set("font-size", "10px")
                .set("font-weight", "bold")
                .set("color", "#1565C0");

        // Estado de la licencia
        Span estado = new Span("ESTADO: " + licencia.getEstado().getDescripcion().toUpperCase());
        estado.getStyle()
                .set("font-size", "10px")
                .set("font-weight", "bold")
                .set("color", licencia.getEstado() == EstadoLicencia.VIGENTE ? "#4CAF50" : "#F44336");

        footerContent.add(republica, estado);
        footer.add(footerContent);

        return footer;
    }

    private Div createDecorativePattern() {
        Div pattern = new Div();
        pattern.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("right", "0")
                .set("width", "200px")
                .set("height", "200px")
                .set("opacity", "0.1")
                .set("background", "radial-gradient(circle, #FFD54F 20%, transparent 20%)")
                .set("background-size", "20px 20px")
                .set("z-index", "1");

        // Sol decorativo (simulando el diseño)
        Div sun = new Div();
        sun.getStyle()
                .set("position", "absolute")
                .set("top", "20px")
                .set("right", "30px")
                .set("width", "60px")
                .set("height", "60px")
                .set("border-radius", "50%")
                .set("background", "radial-gradient(circle, #FFD54F, #FFA726)")
                .set("opacity", "0.3")
                .set("z-index", "2");

        pattern.add(sun);
        return pattern;
    }

    /**
     * Dorso del carnet: clase con su descripción, datos secundarios y leyenda LNC
     */
    private Div createDorsoCardLayout() {
        Div card = new Div();
        card.getStyle()
                .set("background", "linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 50%, #90CAF9 100%)")
                .set("border", "2px solid #1976D2")
                .set("border-radius", "15px")
                .set("padding", "20px")
                .set("margin", "0 20px 20px 20px")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("height", "300px");

        // Fila principal: badge grande de la clase + descripción
        HorizontalLayout claseRow = new HorizontalLayout();
        claseRow.setAlignItems(FlexComponent.Alignment.CENTER);
        claseRow.setSpacing(true);
        claseRow.getStyle()
                .set("background", "rgba(255,255,255,0.6)")
                .set("border", "1px solid #1976D2")
                .set("border-radius", "8px")
                .set("padding", "10px 15px")
                .set("margin-bottom", "12px");

        Span claseBadge = new Span(licencia.getClase().name());
        claseBadge.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "48px")
                .set("height", "48px")
                .set("border", "2px solid #1565C0")
                .set("border-radius", "8px")
                .set("background", "#FFFFFF")
                .set("color", "#1565C0")
                .set("font-size", "24px")
                .set("font-weight", "bold");

        Span claseDescripcion = new Span(licencia.getClase().name() + " — " + licencia.getClase().getDescripcion());
        claseDescripcion.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "bold")
                .set("color", "#333333");

        claseRow.add(claseBadge, claseDescripcion);

        // Datos secundarios
        VerticalLayout datos = new VerticalLayout();
        datos.setSpacing(false);
        datos.setPadding(false);
        datos.add(
            createInfoField("EDAD MÍNIMA:", licencia.getClase().getEdadMinima() + " años"),
            createInfoField("N° LICENCIA:", licencia.getNumeroLicencia())
        );
        if (licencia.getObservaciones() != null && !licencia.getObservaciones().isBlank()) {
            datos.add(createInfoField("OBSERVACIONES:", licencia.getObservaciones()));
        }

        // Footer del dorso: leyenda LNC + código de barras simulado
        Div footer = new Div();
        footer.getStyle()
                .set("position", "absolute")
                .set("bottom", "15px")
                .set("left", "20px")
                .set("right", "20px");

        HorizontalLayout footerContent = new HorizontalLayout();
        footerContent.setWidthFull();
        footerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        footerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span lnc = new Span("LNC");
        lnc.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "bold")
                .set("color", "#1565C0")
                .set("letter-spacing", "2px");

        Div barcode = new Div();
        barcode.getStyle()
                .set("width", "260px")
                .set("height", "36px")
                .set("background", "repeating-linear-gradient(90deg, #333 0 2px, transparent 2px 5px)")
                .set("border", "1px solid #999")
                .set("background-color", "#FFFFFF");

        footerContent.add(lnc, barcode);
        footer.add(footerContent);

        Div content = new Div();
        content.getStyle().set("position", "relative").set("z-index", "10");
        content.add(claseRow, datos);

        card.add(createDecorativePattern(), content, footer);
        return card;
    }

    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);

        Button imprimirButton = new Button("Imprimir Licencia", new Icon(VaadinIcon.PRINT));
        imprimirButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        imprimirButton.addClickListener(e -> {
            // TODO: Implementar funcionalidad de impresión
            getUI().ifPresent(ui -> ui.getPage().executeJs("window.print();"));
        });

        Button cerrarButton = new Button("Cerrar", new Icon(VaadinIcon.CLOSE));
        cerrarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cerrarButton.addClickListener(e -> close());

        buttonLayout.add(imprimirButton, cerrarButton);
        return buttonLayout;
    }
}
