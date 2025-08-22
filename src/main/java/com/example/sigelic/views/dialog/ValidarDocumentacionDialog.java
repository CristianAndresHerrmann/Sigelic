package com.example.sigelic.views.dialog;

import java.util.function.Consumer;

import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Diálogo para validar documentación de un trámite
 */
public class ValidarDocumentacionDialog extends Dialog {

    private final TramiteService tramiteService;
    private final Tramite tramite;
    private final Consumer<Void> onSuccess;

    private TextField agenteResponsableField;
    private TextArea observacionesField;

    public ValidarDocumentacionDialog(TramiteService tramiteService, Tramite tramite, Consumer<Void> onSuccess) {
        this.tramiteService = tramiteService;
        this.tramite = tramite;
        this.onSuccess = onSuccess;

        setHeaderTitle("Validar Documentación");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("500px");

        createForm();
        createButtons();
    }

    private void createForm() {
        H3 title = new H3("Validación de Documentación");
        title.getStyle().set("margin-top", "0");

        // Información del trámite
        Span tramiteInfo = new Span("Trámite: T" + String.format("%06d", tramite.getId()) + 
                                    " - " + tramite.getTipo().getDescripcion());
        tramiteInfo.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("display", "block")
                .set("margin-bottom", "var(--lumo-space-m)");

        // Información del titular
        Span titularInfo = new Span("Titular: " + tramite.getTitular().getNombre() + 
                                    " " + tramite.getTitular().getApellido() + 
                                    " - DNI: " + tramite.getTitular().getDni());
        titularInfo.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("display", "block")
                .set("margin-bottom", "var(--lumo-space-m)");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // Campo para agente responsable
        agenteResponsableField = new TextField("Agente Responsable");
        agenteResponsableField.setPlaceholder("Nombre del agente que valida...");
        agenteResponsableField.setValue("Sistema"); // Valor por defecto
        agenteResponsableField.setWidthFull();

        // Campo para observaciones
        observacionesField = new TextArea("Observaciones de Validación");
        observacionesField.setPlaceholder("Observaciones sobre la documentación presentada (opcional)...");
        observacionesField.setMaxLength(500);
        observacionesField.setWidthFull();

        formLayout.add(agenteResponsableField, observacionesField);

        // Lista de documentación requerida según el tipo de trámite
        Span documentacionRequerida = createDocumentacionRequerida();

        VerticalLayout content = new VerticalLayout(
                title, tramiteInfo, titularInfo, 
                documentacionRequerida, formLayout
        );
        content.setPadding(false);
        content.setSpacing(true);

        add(content);
    }

    private Span createDocumentacionRequerida() {
        String documentacion = getDocumentacionRequerida(tramite.getTipo());
        
        Span span = new Span("Documentación requerida: " + documentacion);
        span.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("border-left", "3px solid var(--lumo-primary-color)")
                .set("display", "block")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-s)");
        
        return span;
    }

    private String getDocumentacionRequerida(com.example.sigelic.model.TipoTramite tipo) {
        return switch (tipo) {
            case EMISION -> "DNI original, certificado de nacimiento, comprobante de domicilio";
            case RENOVACION -> "DNI original, licencia vencida, comprobante de domicilio";
            case DUPLICADO -> "DNI original, denuncia policial por pérdida/robo, comprobante de domicilio";
            case CAMBIO_DOMICILIO -> "DNI original, licencia vigente, nuevo comprobante de domicilio";
            default -> "Documentación según tipo de trámite";
        };
    }

    private void createButtons() {
        Button cancelButton = new Button("Cancelar", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button rechazarButton = new Button("Rechazar", e -> rechazarDocumentacion());
        rechazarButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button validarButton = new Button("Validar Documentación", e -> validarDocumentacion());
        validarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, rechazarButton, validarButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        getFooter().add(buttonLayout);
    }

    private void validarDocumentacion() {
        try {
            String agente = agenteResponsableField.getValue();
            if (agente == null || agente.trim().isEmpty()) {
                showNotification("El agente responsable es obligatorio", NotificationVariant.LUMO_ERROR);
                return;
            }

            tramiteService.validarDocumentacion(tramite.getId(), agente.trim());
            
            showNotification("Documentación validada exitosamente", NotificationVariant.LUMO_SUCCESS);
            onSuccess.accept(null);
            close();

        } catch (IllegalStateException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("Error al validar documentación: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void rechazarDocumentacion() {
        try {
            String observaciones = observacionesField.getValue();
            if (observaciones == null || observaciones.trim().isEmpty()) {
                showNotification("Las observaciones son obligatorias para rechazar", NotificationVariant.LUMO_ERROR);
                return;
            }

            tramiteService.rechazarTramite(tramite.getId(), "Documentación rechazada: " + observaciones.trim());
            
            showNotification("Trámite rechazado por documentación incompleta", NotificationVariant.LUMO_ERROR);
            onSuccess.accept(null);
            close();

        } catch (Exception e) {
            showNotification("Error al rechazar trámite: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
