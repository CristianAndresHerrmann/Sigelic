package com.example.sigelic.views.dialog;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import com.example.sigelic.model.ExamenPractico;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * Diálogo para registrar examen práctico
 */
public class RegistrarExamenPracticoDialog extends Dialog {

    private final TramiteService tramiteService;
    private final Tramite tramite;
    private final Consumer<Void> onSuccess;

    // Componentes del formulario
    private TextField examinadorField;
    private DateTimePicker fechaExamenPicker;
    private IntegerField faltasLevesField;
    private IntegerField faltasGravesField;
    private TextField vehiculoUtilizadoField;
    private TextField pistaUtilizadaField;
    private TextArea observacionesField;
    private TextField resultadoField;

    private Binder<ExamenPractico> binder;

    public RegistrarExamenPracticoDialog(TramiteService tramiteService, Tramite tramite, Consumer<Void> onSuccess) {
        this.tramiteService = tramiteService;
        this.tramite = tramite;
        this.onSuccess = onSuccess;

        setHeaderTitle("Registrar Examen Práctico");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("600px");
        setMaxHeight("80vh");

        createForm();
        createButtons();
        
        binder = new Binder<>(ExamenPractico.class);
        bindFields();
        
        // Establecer valores por defecto
        fechaExamenPicker.setValue(LocalDateTime.now());
        faltasLevesField.setValue(0);
        faltasGravesField.setValue(0);
    }

    private void createForm() {
        H3 title = new H3("Examen Práctico - Trámite T" + String.format("%06d", tramite.getId()));
        title.getStyle().set("margin-top", "0");

        // Información del titular
        Span titularInfo = new Span("Titular: " + tramite.getTitular().getNombre() + 
                                    " " + tramite.getTitular().getApellido() + 
                                    " - DNI: " + tramite.getTitular().getDni());
        titularInfo.getStyle().set("font-weight", "bold");

        Span claseLicencia = new Span("Clase solicitada: " + tramite.getClaseSolicitada());
        claseLicencia.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout infoLayout = new VerticalLayout(title, titularInfo, claseLicencia);
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);

        add(infoLayout);
        add(new Hr());

        // Formulario
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("320px", 2)
        );

        // Datos del examen
        examinadorField = new TextField("Examinador");
        examinadorField.setRequiredIndicatorVisible(true);
        examinadorField.setPlaceholder("Nombre del examinador");

        fechaExamenPicker = new DateTimePicker("Fecha y hora del examen");
        fechaExamenPicker.setRequiredIndicatorVisible(true);

        faltasLevesField = new IntegerField("Faltas leves");
        faltasLevesField.setRequiredIndicatorVisible(true);
        faltasLevesField.setMin(0);
        faltasLevesField.setMax(20);

        faltasGravesField = new IntegerField("Faltas graves");
        faltasGravesField.setRequiredIndicatorVisible(true);
        faltasGravesField.setMin(0);
        faltasGravesField.setMax(10);

        vehiculoUtilizadoField = new TextField("Vehículo utilizado");
        vehiculoUtilizadoField.setPlaceholder("Ej: Automóvil, Motocicleta, Camión...");
        vehiculoUtilizadoField.setMaxLength(50);

        pistaUtilizadaField = new TextField("Pista utilizada");
        pistaUtilizadaField.setPlaceholder("Ej: Pista A, Circuito Principal...");
        pistaUtilizadaField.setMaxLength(50);

        // Campo de resultado (solo lectura, calculado automáticamente)
        resultadoField = new TextField("Resultado");
        resultadoField.setReadOnly(true);
        resultadoField.setPlaceholder("APROBADO/REPROBADO");

        observacionesField = new TextArea("Observaciones");
        observacionesField.setPlaceholder("Observaciones adicionales del examen...");
        observacionesField.setMaxLength(500);

        // Listener para calcular resultado automáticamente
        var calculateResult = (Runnable) this::calculateResult;
        faltasLevesField.addValueChangeListener(e -> calculateResult.run());
        faltasGravesField.addValueChangeListener(e -> calculateResult.run());

        // Agregar campos al layout
        formLayout.add(examinadorField, fechaExamenPicker);
        formLayout.add(faltasLevesField, faltasGravesField);
        formLayout.add(vehiculoUtilizadoField, pistaUtilizadaField);
        formLayout.add(resultadoField, 2);
        formLayout.add(observacionesField, 2);

        add(formLayout);

        // Información sobre aprobación
        Span infoAprobacion = new Span("Nota: Para aprobar no debe tener faltas graves y máximo 3 faltas leves");
        infoAprobacion.getStyle().set("font-size", "var(--lumo-font-size-s)");
        infoAprobacion.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        add(infoAprobacion);
    }
    
    private void calculateResult() {
        Integer faltasLeves = faltasLevesField.getValue();
        Integer faltasGraves = faltasGravesField.getValue();
        
        if (faltasLeves != null && faltasGraves != null) {
            // Lógica: Sin faltas graves y máximo 3 faltas leves
            boolean aprobado = faltasGraves == 0 && faltasLeves <= 3;
            
            if (aprobado) {
                resultadoField.setValue("APROBADO");
                resultadoField.getStyle().set("color", "var(--lumo-success-color)");
                resultadoField.getStyle().set("font-weight", "bold");
            } else {
                resultadoField.setValue("REPROBADO");
                resultadoField.getStyle().set("color", "var(--lumo-error-color)");
                resultadoField.getStyle().set("font-weight", "bold");
            }
        } else {
            resultadoField.clear();
            resultadoField.getStyle().remove("color");
            resultadoField.getStyle().remove("font-weight");
        }
    }

    private void createButtons() {
        Button registrarButton = new Button("Registrar Examen");
        registrarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registrarButton.addClickListener(e -> registrarExamen());

        Button rechazarButton = new Button("Rechazar Examen");
        rechazarButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        rechazarButton.addClickListener(e -> rechazarExamen());

        Button cancelButton = new Button("Cancelar");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(
            registrarButton, rechazarButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);

        getFooter().add(buttonLayout);
    }

    private void bindFields() {
        binder.forField(examinadorField)
            .asRequired("El examinador es obligatorio")
            .bind(ExamenPractico::getExaminador, ExamenPractico::setExaminador);

        binder.forField(fechaExamenPicker)
            .asRequired("La fecha del examen es obligatoria")
            .bind(ExamenPractico::getFecha, ExamenPractico::setFecha);

        binder.forField(faltasLevesField)
            .asRequired("Las faltas leves son obligatorias")
            .withValidator(faltas -> faltas != null && faltas >= 0, 
                          "Las faltas leves no pueden ser negativas")
            .bind(ExamenPractico::getFaltasLeves, ExamenPractico::setFaltasLeves);

        binder.forField(faltasGravesField)
            .asRequired("Las faltas graves son obligatorias")
            .withValidator(faltas -> faltas != null && faltas >= 0, 
                          "Las faltas graves no pueden ser negativas")
            .bind(ExamenPractico::getFaltasGraves, ExamenPractico::setFaltasGraves);

        binder.forField(vehiculoUtilizadoField)
            .bind(ExamenPractico::getVehiculoUtilizado, ExamenPractico::setVehiculoUtilizado);

        binder.forField(pistaUtilizadaField)
            .bind(ExamenPractico::getPistaUtilizada, ExamenPractico::setPistaUtilizada);

        binder.forField(observacionesField)
            .bind(ExamenPractico::getObservaciones, ExamenPractico::setObservaciones);
    }

    private void registrarExamen() {
        try {
            ExamenPractico examen = new ExamenPractico();
            binder.writeBean(examen);

            // La entidad ya tiene @PrePersist que calcula automáticamente aprobado basado en faltas
            boolean aprobado = examen.calcularAprobacion();

            tramiteService.registrarExamenPractico(tramite.getId(), examen);
            
            String mensaje = aprobado ? 
                "Examen práctico registrado como APROBADO (Faltas leves: " + examen.getFaltasLeves() + 
                ", Faltas graves: " + examen.getFaltasGraves() + ")" : 
                "Examen práctico registrado como REPROBADO (Faltas leves: " + examen.getFaltasLeves() + 
                ", Faltas graves: " + examen.getFaltasGraves() + ") - Se permite reintento";
            
            showNotification(mensaje, aprobado ? 
                           NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_CONTRAST);
            
            onSuccess.accept(null);
            close();

        } catch (ValidationException e) {
            showNotification("Por favor complete todos los campos obligatorios correctamente", 
                           NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("Error al registrar el examen: " + e.getMessage(), 
                           NotificationVariant.LUMO_ERROR);
        }
    }

    private void rechazarExamen() {
        try {
            String motivo = observacionesField.getValue();
            if (motivo == null || motivo.trim().isEmpty()) {
                motivo = "Examen práctico rechazado";
            }

            tramiteService.rechazarExamenPractico(tramite.getId(), motivo);
            
            showNotification("Examen práctico RECHAZADO - El trámite permite reintento", 
                           NotificationVariant.LUMO_ERROR);
            
            onSuccess.accept(null);
            close();

        } catch (Exception e) {
            showNotification("Error al rechazar el examen: " + e.getMessage(), 
                           NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setDuration(4000);
    }
}
