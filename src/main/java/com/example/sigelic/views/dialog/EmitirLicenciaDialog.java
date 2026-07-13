package com.example.sigelic.views.dialog;

import com.example.sigelic.model.*;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

/**
 * Dialog para emitir una licencia de conducir
 */
@Slf4j
public class EmitirLicenciaDialog extends Dialog {

    private final TramiteService tramiteService;
    private final Tramite tramite;
    private final Runnable onSuccess;

    // Componentes del formulario
    private final TextArea observaciones;
    private final Button emitirButton;
    private final Button cancelarButton;

    public EmitirLicenciaDialog(TramiteService tramiteService, Tramite tramite, Runnable onSuccess) {
        this.tramiteService = tramiteService;
        this.tramite = tramite;
        this.onSuccess = onSuccess;

        // Inicializar componentes
        this.observaciones = new TextArea("Observaciones");
        this.emitirButton = new Button("Emitir Licencia", new Icon(VaadinIcon.CHECK_CIRCLE));
        this.cancelarButton = new Button("Cancelar", new Icon(VaadinIcon.CLOSE));

        initializeDialog();
        createLayout();
        configureEventListeners();
    }

    private void initializeDialog() {
        setHeaderTitle("Emisión de Licencia de Conducir");
        setWidth("500px");
        setHeight("600px");
        setModal(true);
        setDraggable(false);
        setResizable(false);
    }

    private void createLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        // Información del trámite
        Div infoSection = createInfoSection();
        
        // Verificación de requisitos
        Div requisitosSection = createRequisitosSection();
        
        // Formulario
        FormLayout formLayout = createFormSection();
        
        // Botones
        HorizontalLayout buttonLayout = createButtonLayout();

        mainLayout.add(infoSection, requisitosSection, formLayout, buttonLayout);
        add(mainLayout);
    }

    private Div createInfoSection() {
        Div infoDiv = new Div();
        infoDiv.addClassName("info-section");
        infoDiv.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)");

        H3 title = new H3("Información del Trámite");
        title.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");

        Titular titular = tramite.getTitular();
        
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        
        infoLayout.add(
            createInfoRow("Titular:", titular.getNombre() + " " + titular.getApellido()),
            createInfoRow("DNI:", titular.getDni()),
            createInfoRow("Fecha de Nacimiento:", titular.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
            createInfoRow("Clase Solicitada:", tramite.getClaseSolicitada().getDescripcion()),
            createInfoRow("Tipo de Trámite:", tramite.getTipo().getDescripcion()),
            createInfoRow("Estado Actual:", tramite.getEstado().getDescripcion())
        );

        infoDiv.add(title, infoLayout);
        return infoDiv;
    }

    private HorizontalLayout createInfoRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "bold").set("min-width", "150px");
        
        Span valueSpan = new Span(value);
        
        row.add(labelSpan, valueSpan);
        return row;
    }

    private Div createRequisitosSection() {
        Div requisitosDiv = new Div();
        requisitosDiv.addClassName("requisitos-section");
        
        H3 title = new H3("Verificación de Requisitos");
        title.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");

        VerticalLayout requisitosLayout = new VerticalLayout();
        requisitosLayout.setSpacing(false);
        requisitosLayout.setPadding(false);

        // Verificar cada requisito
        requisitosLayout.add(
            createRequisitoRow("Documentación validada", tramite.getDocumentacionValidada()),
            createRequisitoRow("Apto médico vigente", tramite.getAptoMedicoVigente()),
            createRequisitoRow("Examen teórico aprobado", tramite.getExamenTeoricoAprobado()),
            createRequisitoRow("Examen práctico aprobado", tramite.getExamenPracticoAprobado()),
            createRequisitoRow("Pago acreditado", tramite.getPagoAcreditado())
        );

        // Estado general
        boolean todosRequisitos = tramite.todosLosRequisitosCumplidos();
        HorizontalLayout estadoGeneral = new HorizontalLayout();
        estadoGeneral.setSpacing(true);
        estadoGeneral.setAlignItems(FlexComponent.Alignment.CENTER);
        estadoGeneral.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", todosRequisitos ? "var(--lumo-success-color-10pct)" : "var(--lumo-error-color-10pct)");

        Icon estadoIcon = todosRequisitos ? 
            new Icon(VaadinIcon.CHECK_CIRCLE) : 
            new Icon(VaadinIcon.WARNING);
        estadoIcon.setColor(todosRequisitos ? "var(--lumo-success-color)" : "var(--lumo-error-color)");

        Span estadoTexto = new Span(todosRequisitos ? 
            "✓ Todos los requisitos cumplidos - Listo para emitir" : 
            "✗ Faltan requisitos por cumplir");
        estadoTexto.getStyle().set("font-weight", "bold");

        estadoGeneral.add(estadoIcon, estadoTexto);
        requisitosLayout.add(estadoGeneral);

        requisitosDiv.add(title, requisitosLayout);
        return requisitosDiv;
    }

    private HorizontalLayout createRequisitoRow(String descripcion, boolean cumplido) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon icon = cumplido ? new Icon(VaadinIcon.CHECK) : new Icon(VaadinIcon.CLOSE);
        icon.setColor(cumplido ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
        icon.setSize("16px");

        Span texto = new Span(descripcion);
        if (!cumplido) {
            texto.getStyle().set("color", "var(--lumo-error-text-color)");
        }

        row.add(icon, texto);
        return row;
    }

    private FormLayout createFormSection() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Configurar observaciones
        observaciones.setPlaceholder("Observaciones adicionales para la emisión de la licencia (opcional)");
        observaciones.setHeight("100px");

        formLayout.add(observaciones);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();

        // Configurar botón emitir
        emitirButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        emitirButton.setEnabled(tramite.todosLosRequisitosCumplidos());

        // Configurar botón cancelar
        cancelarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(cancelarButton, emitirButton);
        return buttonLayout;
    }

    private void configureEventListeners() {
        emitirButton.addClickListener(event -> emitirLicencia());
        cancelarButton.addClickListener(event -> close());
    }

    private void emitirLicencia() {
        try {
            if (!tramite.todosLosRequisitosCumplidos()) {
                showNotification("No se puede emitir la licencia. Faltan requisitos por cumplir.", 
                               NotificationVariant.LUMO_ERROR);
                return;
            }

            if (tramite.getEstado() == EstadoTramite.EMITIDA) {
                showNotification("Este trámite ya tiene una licencia emitida.", 
                               NotificationVariant.LUMO_ERROR);
                return;
            }

            // Deshabilitar botón durante el proceso
            emitirButton.setEnabled(false);
            emitirButton.setText("Emitiendo...");

            // Emitir licencia
            Licencia licenciaEmitida = tramiteService.emitirLicencia(tramite.getId());

            // Agregar observaciones si las hay
            if (observaciones.getValue() != null && !observaciones.getValue().trim().isEmpty()) {
                licenciaEmitida.setObservaciones(observaciones.getValue().trim());
            }

            showNotification("✓ Licencia emitida exitosamente. Número: " + licenciaEmitida.getNumeroLicencia(), 
                           NotificationVariant.LUMO_SUCCESS);

            log.info("Licencia emitida exitosamente para trámite ID: {} - Número: {}", 
                    tramite.getId(), licenciaEmitida.getNumeroLicencia());

            // Cerrar dialog y actualizar vista padre
            close();
            if (onSuccess != null) {
                onSuccess.run();
            }

            // Mostrar el carnet de la licencia recién emitida (frente y dorso)
            new VerLicenciaDialog(licenciaEmitida).open();

        } catch (IllegalStateException e) {
            showNotification("Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            log.warn("Error al emitir licencia para trámite ID {}: {}", tramite.getId(), e.getMessage());
        } catch (Exception e) {
            showNotification("Error inesperado al emitir la licencia. Intente nuevamente.", 
                           NotificationVariant.LUMO_ERROR);
            log.error("Error inesperado al emitir licencia para trámite ID {}: {}", tramite.getId(), e.getMessage(), e);
        } finally {
            // Rehabilitar botón
            emitirButton.setEnabled(tramite.todosLosRequisitosCumplidos());
            emitirButton.setText("Emitir Licencia");
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 4000);
        notification.addThemeVariants(variant);
        notification.open();
    }
}
