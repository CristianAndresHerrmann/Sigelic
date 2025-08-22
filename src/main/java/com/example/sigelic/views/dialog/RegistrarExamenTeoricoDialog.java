package com.example.sigelic.views.dialog;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import com.example.sigelic.model.ExamenTeorico;
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
 * Diálogo para registrar examen teórico
 */
public class RegistrarExamenTeoricoDialog extends Dialog {

    private final TramiteService tramiteService;
    private final Tramite tramite;
    private final Consumer<Void> onSuccess;

    // Componentes del formulario
    private TextField examinadorField;
    private DateTimePicker fechaExamenPicker;
    private IntegerField cantidadPreguntasField;
    private IntegerField respuestasCorrectasField;
    private TextArea observacionesField;
    private TextField puntajeField;
    private TextField resultadoField;

    private Binder<ExamenTeorico> binder;

    public RegistrarExamenTeoricoDialog(TramiteService tramiteService, Tramite tramite, Consumer<Void> onSuccess) {
        this.tramiteService = tramiteService;
        this.tramite = tramite;
        this.onSuccess = onSuccess;

        setHeaderTitle("Registrar Examen Teórico");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("600px");
        setMaxHeight("80vh");

        createForm();
        createButtons();
        
        binder = new Binder<>(ExamenTeorico.class);
        bindFields();
        
        // Establecer valores por defecto
        fechaExamenPicker.setValue(LocalDateTime.now());
        cantidadPreguntasField.setValue(30); // 30 preguntas por defecto
    }

    private void createForm() {
        H3 title = new H3("Examen Teórico - Trámite T" + String.format("%06d", tramite.getId()));
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

        cantidadPreguntasField = new IntegerField("Cantidad de preguntas");
        cantidadPreguntasField.setRequiredIndicatorVisible(true);
        cantidadPreguntasField.setMin(1);
        cantidadPreguntasField.setMax(100);

        respuestasCorrectasField = new IntegerField("Respuestas correctas");
        respuestasCorrectasField.setRequiredIndicatorVisible(true);
        respuestasCorrectasField.setMin(0);
        
        // Campo de puntaje (solo lectura, calculado automáticamente)
        puntajeField = new TextField("Puntaje");
        puntajeField.setReadOnly(true);
        puntajeField.setPlaceholder("Se calcula automáticamente");

        // Campo de resultado (solo lectura, calculado automáticamente)
        resultadoField = new TextField("Resultado");
        resultadoField.setReadOnly(true);
        resultadoField.setPlaceholder("APROBADO/REPROBADO");

        observacionesField = new TextArea("Observaciones");
        observacionesField.setPlaceholder("Observaciones adicionales del examen...");
        observacionesField.setMaxLength(500);

        // Listener para calcular puntaje automáticamente
        var calculateScore = (Runnable) this::calculateScore;
        cantidadPreguntasField.addValueChangeListener(e -> calculateScore.run());
        respuestasCorrectasField.addValueChangeListener(e -> calculateScore.run());

        // Agregar campos al layout
        formLayout.add(examinadorField, fechaExamenPicker);
        formLayout.add(cantidadPreguntasField, respuestasCorrectasField);
        formLayout.add(puntajeField, resultadoField);
        formLayout.add(observacionesField, 2);

        add(formLayout);

        // Información sobre aprobación
        Span infoAprobacion = new Span("Nota: Se requiere 80% de respuestas correctas para aprobar (puntaje ≥ 80)");
        infoAprobacion.getStyle().set("font-size", "var(--lumo-font-size-s)");
        infoAprobacion.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        add(infoAprobacion);
    }
    
    private void calculateScore() {
        Integer totalPreguntas = cantidadPreguntasField.getValue();
        Integer respuestasCorrectas = respuestasCorrectasField.getValue();
        
        if (totalPreguntas != null && respuestasCorrectas != null && totalPreguntas > 0) {
            // Calcular puntaje: (respuestas correctas / total preguntas) * 100 - solo parte entera
            int puntaje = (respuestasCorrectas * 100) / totalPreguntas;
            
            puntajeField.setValue(puntaje + "%");
            
            // Determinar resultado automáticamente
            if (puntaje >= 80) {
                resultadoField.setValue("APROBADO");
                resultadoField.getStyle().set("color", "var(--lumo-success-color)");
                resultadoField.getStyle().set("font-weight", "bold");
            } else {
                resultadoField.setValue("REPROBADO");
                resultadoField.getStyle().set("color", "var(--lumo-error-color)");
                resultadoField.getStyle().set("font-weight", "bold");
            }
        } else {
            puntajeField.clear();
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
            .bind(ExamenTeorico::getExaminador, ExamenTeorico::setExaminador);

        binder.forField(fechaExamenPicker)
            .asRequired("La fecha del examen es obligatoria")
            .bind(ExamenTeorico::getFecha, ExamenTeorico::setFecha);

        binder.forField(cantidadPreguntasField)
            .asRequired("La cantidad de preguntas es obligatoria")
            .withValidator(cantidad -> cantidad != null && cantidad > 0, 
                          "Debe haber al menos 1 pregunta")
            .bind(ExamenTeorico::getCantidadPreguntas, ExamenTeorico::setCantidadPreguntas);

        binder.forField(respuestasCorrectasField)
            .asRequired("Las respuestas correctas son obligatorias")
            .withValidator(respuestas -> {
                Integer cantidad = cantidadPreguntasField.getValue();
                return respuestas != null && respuestas >= 0 && 
                       (cantidad == null || respuestas <= cantidad);
            }, "Las respuestas correctas no pueden ser más que las preguntas totales")
            .bind(ExamenTeorico::getRespuestasCorrectas, ExamenTeorico::setRespuestasCorrectas);

        binder.forField(observacionesField)
            .bind(ExamenTeorico::getObservaciones, ExamenTeorico::setObservaciones);
    }

    private void registrarExamen() {
        try {
            ExamenTeorico examen = new ExamenTeorico();
            binder.writeBean(examen);

            // Validar que las respuestas correctas no excedan las preguntas
            if (examen.getRespuestasCorrectas() > examen.getCantidadPreguntas()) {
                showNotification("Las respuestas correctas no pueden exceder la cantidad de preguntas", 
                               NotificationVariant.LUMO_ERROR);
                return;
            }

            // Calcular puntaje automáticamente (parte entera)
            int puntaje = (examen.getRespuestasCorrectas() * 100) / examen.getCantidadPreguntas();
            examen.setPuntaje(puntaje);
            
            // La entidad ya tiene @PrePersist que calcula automáticamente aprobado basado en puntaje

            tramiteService.registrarExamenTeorico(tramite.getId(), examen);
            
            String mensaje = puntaje >= 80 ? 
                "Examen teórico registrado como APROBADO (Puntaje: " + puntaje + "%)" : 
                "Examen teórico registrado como REPROBADO (Puntaje: " + puntaje + "%) - Se permite reintento";
            
            showNotification(mensaje, puntaje >= 80 ? 
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
                motivo = "Examen teórico rechazado";
            }

            tramiteService.rechazarExamenTeorico(tramite.getId(), motivo);
            
            showNotification("Examen teórico RECHAZADO - El trámite permite reintento", 
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
