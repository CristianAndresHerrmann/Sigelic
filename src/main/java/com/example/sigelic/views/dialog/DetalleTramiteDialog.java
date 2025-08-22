package com.example.sigelic.views.dialog;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.example.sigelic.dto.response.AptoMedicoResponseDTO;
import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Diálogo para mostrar el detalle completo de un trámite
 */
public class DetalleTramiteDialog extends Dialog {

    private final TramiteService tramiteService;
    private final Tramite tramite;

    public DetalleTramiteDialog(TramiteService tramiteService, Tramite tramite) {
        this.tramiteService = tramiteService;
        this.tramite = tramite;

        setHeaderTitle("Detalle del Trámite");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("700px");
        setMaxHeight("90vh");

        createContent();
        createButtons();
    }

    private void createContent() {
        // Título con número de trámite
        H3 title = new H3("Trámite T" + String.format("%06d", tramite.getId()));
        title.getStyle().set("margin-top", "0");

        // Badge de estado
        Span estadoBadge = createEstadoBadge(tramite.getEstado());

        HorizontalLayout titleLayout = new HorizontalLayout(title, estadoBadge);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleLayout.setWidthFull();

        // Información básica del trámite
        VerticalLayout infoBasica = createInfoBasica();
        
        // Información del titular
        VerticalLayout infoTitular = createInfoTitular();

        // Progreso del trámite
        VerticalLayout progresoTramite = createProgresoTramite();

        // Información del apto médico si existe
        VerticalLayout infoAptoMedico = createInfoAptoMedico();

        VerticalLayout content = new VerticalLayout(
                titleLayout,
                new Hr(),
                infoBasica,
                new Hr(),
                infoTitular,
                new Hr(),
                progresoTramite
        );

        if (infoAptoMedico.getComponentCount() > 0) {
            content.add(new Hr(), infoAptoMedico);
        }

        content.setPadding(false);
        content.setSpacing(true);

        add(content);
    }

    private VerticalLayout createInfoBasica() {
        H4 titulo = new H4("Información Básica");
        titulo.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        Div tipoDiv = createInfoItem("Tipo de Trámite", tramite.getTipo().getDescripcion());
        Div claseDiv = createInfoItem("Clase Solicitada", 
                tramite.getClaseSolicitada() != null ? 
                tramite.getClaseSolicitada().name() + " - " + tramite.getClaseSolicitada().getDescripcion() : 
                "No especificada");
        Div fechaDiv = createInfoItem("Fecha de Inicio", 
                tramite.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        
        Div observacionesDiv = null;
        if (tramite.getObservaciones() != null && !tramite.getObservaciones().trim().isEmpty()) {
            observacionesDiv = createInfoItem("Observaciones", tramite.getObservaciones());
        }

        VerticalLayout layout = new VerticalLayout(titulo, tipoDiv, claseDiv, fechaDiv);
        if (observacionesDiv != null) {
            layout.add(observacionesDiv);
        }
        layout.setPadding(false);
        layout.setSpacing(false);

        return layout;
    }

    private VerticalLayout createInfoTitular() {
        H4 titulo = new H4("Titular");
        titulo.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        Div nombreDiv = createInfoItem("Nombre Completo", 
                tramite.getTitular().getNombre() + " " + tramite.getTitular().getApellido());
        Div dniDiv = createInfoItem("DNI", tramite.getTitular().getDni());
        Div fechaNacDiv = createInfoItem("Fecha de Nacimiento", 
                tramite.getTitular().getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Div domicilioDiv = createInfoItem("Domicilio", tramite.getTitular().getDomicilio());

        VerticalLayout layout = new VerticalLayout(titulo, nombreDiv, dniDiv, fechaNacDiv, domicilioDiv);
        layout.setPadding(false);
        layout.setSpacing(false);

        return layout;
    }

    private VerticalLayout createProgresoTramite() {
        H4 titulo = new H4("Progreso del Trámite");
        titulo.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        VerticalLayout layout = new VerticalLayout(titulo);
        layout.setPadding(false);
        layout.setSpacing(false);

        // Lista de pasos según el tipo de trámite
        layout.add(createPasoProgreso("Documentación", 
                tramite.getEstado().ordinal() >= EstadoTramite.DOCS_OK.ordinal(), 
                tramite.getEstado() == EstadoTramite.DOCS_OK));

        if (tramite.requiereAptoMedico()) {
            layout.add(createPasoProgreso("Apto Médico", 
                    tramite.getEstado().ordinal() >= EstadoTramite.APTO_MED.ordinal(),
                    tramite.getEstado() == EstadoTramite.APTO_MED));
        }

        if (tramite.requiereExamenTeorico()) {
            layout.add(createPasoProgreso("Examen Teórico", 
                    tramite.getEstado().ordinal() >= EstadoTramite.EX_TEO_OK.ordinal(),
                    tramite.getEstado() == EstadoTramite.EX_TEO_OK));
        }

        if (tramite.requiereExamenPractico()) {
            layout.add(createPasoProgreso("Examen Práctico", 
                    tramite.getEstado().ordinal() >= EstadoTramite.EX_PRA_OK.ordinal(),
                    tramite.getEstado() == EstadoTramite.EX_PRA_OK));
        }

        layout.add(createPasoProgreso("Pago", 
                tramite.getEstado().ordinal() >= EstadoTramite.PAGO_OK.ordinal(),
                tramite.getEstado() == EstadoTramite.PAGO_OK));

        layout.add(createPasoProgreso("Licencia Emitida", 
                tramite.getEstado() == EstadoTramite.EMITIDA,
                tramite.getEstado() == EstadoTramite.EMITIDA));

        return layout;
    }

    private VerticalLayout createInfoAptoMedico() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        if (tramite.requiereAptoMedico() && tramite.getEstado().ordinal() >= EstadoTramite.APTO_MED.ordinal()) {
            H4 titulo = new H4("Apto Médico");
            titulo.getStyle().set("margin-bottom", "var(--lumo-space-s)");
            layout.add(titulo);

            try {
                Optional<AptoMedicoResponseDTO> aptoOpt = tramiteService.obtenerAptoMedico(tramite.getId());
                if (aptoOpt.isPresent()) {
                    AptoMedicoResponseDTO apto = aptoOpt.get();
                    
                    layout.add(createInfoItem("Médico Examinador", apto.getProfesional()));
                    layout.add(createInfoItem("Fecha del Examen", 
                            apto.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                    layout.add(createInfoItem("Resultado", apto.getApto() ? "APTO" : "NO APTO"));
                    layout.add(createInfoItem("Válido hasta", 
                            apto.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
                    layout.add(createInfoItem("Estado", apto.isVigente() ? "VIGENTE" : "VENCIDO"));
                    
                    if (apto.getObservaciones() != null && !apto.getObservaciones().trim().isEmpty()) {
                        layout.add(createInfoItem("Observaciones", apto.getObservaciones()));
                    }
                    
                    if (apto.getRestricciones() != null && !apto.getRestricciones().trim().isEmpty()) {
                        layout.add(createInfoItem("Restricciones", apto.getRestricciones()));
                    }
                } else {
                    layout.add(new Span("No se encontró información del apto médico"));
                }
            } catch (Exception e) {
                layout.add(new Span("Error al cargar información del apto médico"));
            }
        }

        return layout;
    }

    private Div createInfoItem(String label, String value) {
        Div container = new Div();
        container.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle().set("font-weight", "bold");

        Span valueSpan = new Span(value != null ? value : "No especificado");
        
        container.add(labelSpan, valueSpan);
        return container;
    }

    private HorizontalLayout createPasoProgreso(String nombre, boolean completado, boolean actual) {
        Icon icon;
        String color;
        
        if (completado) {
            icon = new Icon(VaadinIcon.CHECK_CIRCLE);
            color = "var(--lumo-success-color)";
        } else if (actual) {
            icon = new Icon(VaadinIcon.CLOCK);
            color = "var(--lumo-primary-color)";
        } else {
            icon = new Icon(VaadinIcon.CIRCLE_THIN);
            color = "var(--lumo-disabled-text-color)";
        }
        
        icon.getStyle().set("color", color);
        
        Span nombreSpan = new Span(nombre);
        if (actual) {
            nombreSpan.getStyle().set("font-weight", "bold");
        }
        
        HorizontalLayout layout = new HorizontalLayout(icon, nombreSpan);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);
        layout.getStyle().set("margin-bottom", "var(--lumo-space-xs)");
        
        return layout;
    }

    private Span createEstadoBadge(EstadoTramite estado) {
        Span badge = new Span(estado.getDescripcion());
        
        switch (estado) {
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
    }

    private void createButtons() {
        Button closeButton = new Button("Cerrar", e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        getFooter().add(buttonLayout);
    }
}
