package com.example.sigelic.views.dialog;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import com.example.sigelic.dto.AptoMedicoRequestDTO;
import com.example.sigelic.dto.response.AptoMedicoResponseDTO;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * Diálogo para registrar apto médico
 */
public class RegistrarAptoMedicoDialog extends Dialog {

    private final TramiteService tramiteService;
    private final Tramite tramite;
    private final Consumer<Void> onSuccess;

    // Componentes del formulario
    private TextField medicoExaminadorField;
    private DateTimePicker fechaExamenPicker;
    private Checkbox aptoCheckbox;
    
    // Datos físicos
    private NumberField presionSistolicaField;
    private NumberField presionDiastolicaField;
    
    // Examen visual
    private NumberField agudezaVisualOjoDerecho;
    private NumberField agudezaVisualOjoIzquierdo;
    private Checkbox campoVisualNormal;
    private Checkbox visionCromaticaNormal;
    
    // Otros exámenes
    private Checkbox audicionNormal;
    private Checkbox reflejosNormales;
    private Checkbox coordinacionNormal;
    private Checkbox equilibrioNormal;
    private Checkbox cardiovascularNormal;
    private Checkbox sistemaLocomotorNormal;
    
    // Observaciones
    private TextArea observacionesField;
    private TextArea restriccionesField;
    private NumberField mesesValidezField;

    private Binder<AptoMedicoRequestDTO> binder;

    public RegistrarAptoMedicoDialog(TramiteService tramiteService, Tramite tramite, Consumer<Void> onSuccess) {
        this.tramiteService = tramiteService;
        this.tramite = tramite;
        this.onSuccess = onSuccess;

        setHeaderTitle("Registrar Apto Médico");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("800px");
        setMaxHeight("90vh");

        createForm();
        createButtons();
        
        binder = new Binder<>(AptoMedicoRequestDTO.class);
        bindFields();
        
        // Establecer valores por defecto
        fechaExamenPicker.setValue(LocalDateTime.now());
        mesesValidezField.setValue(12.0); // 12 meses por defecto
        aptoCheckbox.setValue(true);
    }

    private void createForm() {
        H3 title = new H3("Examen Médico - Trámite T" + String.format("%06d", tramite.getId()));
        title.getStyle().set("margin-top", "0");

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

        // Datos del médico
        FormLayout medicoLayout = new FormLayout();
        medicoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        medicoExaminadorField = new TextField("Médico Examinador");
        medicoExaminadorField.setPlaceholder("Nombre del médico...");
        medicoExaminadorField.setWidthFull();

        fechaExamenPicker = new DateTimePicker("Fecha y Hora del Examen");
        fechaExamenPicker.setWidthFull();

        medicoLayout.add(medicoExaminadorField, fechaExamenPicker);

        // Datos del examen físico
        H3 examenFisicoTitle = new H3("Examen Físico");
        examenFisicoTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        FormLayout examenFisicoLayout = new FormLayout();
        examenFisicoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
        );

        presionSistolicaField = new NumberField("Presión Sistólica (mmHg)");
        presionSistolicaField.setMin(80);
        presionSistolicaField.setMax(200);
        presionSistolicaField.setStep(1);

        presionDiastolicaField = new NumberField("Presión Diastólica (mmHg)");
        presionDiastolicaField.setMin(50);
        presionDiastolicaField.setMax(120);
        presionDiastolicaField.setStep(1);

        examenFisicoLayout.add(presionSistolicaField, presionDiastolicaField);

        // Examen visual
        H3 examenVisualTitle = new H3("Examen Visual");
        examenVisualTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        FormLayout examenVisualLayout = new FormLayout();
        examenVisualLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        agudezaVisualOjoDerecho = new NumberField("Agudeza Visual Ojo Derecho");
        agudezaVisualOjoDerecho.setMin(0.1);
        agudezaVisualOjoDerecho.setMax(2.0);
        agudezaVisualOjoDerecho.setStep(0.1);
        agudezaVisualOjoDerecho.setValue(1.0);

        agudezaVisualOjoIzquierdo = new NumberField("Agudeza Visual Ojo Izquierdo");
        agudezaVisualOjoIzquierdo.setMin(0.1);
        agudezaVisualOjoIzquierdo.setMax(2.0);
        agudezaVisualOjoIzquierdo.setStep(0.1);
        agudezaVisualOjoIzquierdo.setValue(1.0);

        campoVisualNormal = new Checkbox("Campo Visual Normal");
        campoVisualNormal.setValue(true);

        visionCromaticaNormal = new Checkbox("Visión Cromática Normal");
        visionCromaticaNormal.setValue(true);

        examenVisualLayout.add(agudezaVisualOjoDerecho, agudezaVisualOjoIzquierdo);
        examenVisualLayout.add(campoVisualNormal, visionCromaticaNormal);

        // Otros exámenes
        H3 otrosExamenesTitle = new H3("Otros Exámenes");
        otrosExamenesTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        FormLayout otrosExamenesLayout = new FormLayout();
        otrosExamenesLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        audicionNormal = new Checkbox("Audición Normal");
        audicionNormal.setValue(true);

        reflejosNormales = new Checkbox("Reflejos Normales");
        reflejosNormales.setValue(true);

        coordinacionNormal = new Checkbox("Coordinación Normal");
        coordinacionNormal.setValue(true);

        equilibrioNormal = new Checkbox("Equilibrio Normal");
        equilibrioNormal.setValue(true);

        cardiovascularNormal = new Checkbox("Cardiovascular Normal");
        cardiovascularNormal.setValue(true);

        sistemaLocomotorNormal = new Checkbox("Sistema Locomotor Normal");
        sistemaLocomotorNormal.setValue(true);

        otrosExamenesLayout.add(audicionNormal, reflejosNormales);
        otrosExamenesLayout.add(coordinacionNormal, equilibrioNormal);
        otrosExamenesLayout.add(cardiovascularNormal, sistemaLocomotorNormal);

        // Resultado y observaciones
        H3 resultadoTitle = new H3("Resultado del Examen");
        resultadoTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        FormLayout resultadoLayout = new FormLayout();
        resultadoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        aptoCheckbox = new Checkbox("APTO para conducir");
        aptoCheckbox.getStyle().set("font-weight", "bold");

        mesesValidezField = new NumberField("Validez (meses)");
        mesesValidezField.setMin(1);
        mesesValidezField.setMax(60);
        mesesValidezField.setStep(1);

        observacionesField = new TextArea("Observaciones");
        observacionesField.setMaxLength(500);
        observacionesField.setPlaceholder("Observaciones médicas...");

        restriccionesField = new TextArea("Restricciones");
        restriccionesField.setMaxLength(200);
        restriccionesField.setPlaceholder("Restricciones para conducir...");

        resultadoLayout.add(aptoCheckbox, mesesValidezField);
        resultadoLayout.add(observacionesField, restriccionesField);
        resultadoLayout.setColspan(observacionesField, 2);
        resultadoLayout.setColspan(restriccionesField, 2);

        VerticalLayout content = new VerticalLayout(
                title, titularInfo,
                medicoLayout, new Hr(),
                examenFisicoTitle, examenFisicoLayout, new Hr(),
                examenVisualTitle, examenVisualLayout, new Hr(),
                otrosExamenesTitle, otrosExamenesLayout, new Hr(),
                resultadoTitle, resultadoLayout
        );
        content.setPadding(false);
        content.setSpacing(true);

        add(content);
    }

    private void createButtons() {
        Button cancelButton = new Button("Cancelar", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveButton = new Button("Registrar Apto Médico", e -> registrarAptoMedico());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        getFooter().add(buttonLayout);
    }

    private void bindFields() {
        binder.forField(medicoExaminadorField)
                .asRequired("El médico examinador es obligatorio")
                .bind(AptoMedicoRequestDTO::getMedicoExaminador, AptoMedicoRequestDTO::setMedicoExaminador);

        binder.forField(fechaExamenPicker)
                .asRequired("La fecha del examen es obligatoria")
                .bind(AptoMedicoRequestDTO::getFechaExamen, AptoMedicoRequestDTO::setFechaExamen);

        binder.forField(aptoCheckbox)
                .bind(AptoMedicoRequestDTO::getApto, AptoMedicoRequestDTO::setApto);

        binder.forField(presionSistolicaField)
                .bind(AptoMedicoRequestDTO::getPresionSistolica, AptoMedicoRequestDTO::setPresionSistolica);

        binder.forField(presionDiastolicaField)
                .bind(AptoMedicoRequestDTO::getPresionDiastolica, AptoMedicoRequestDTO::setPresionDiastolica);

        binder.forField(agudezaVisualOjoDerecho)
                .asRequired("La agudeza visual del ojo derecho es obligatoria")
                .bind(AptoMedicoRequestDTO::getAgudezaVisualOjoDerecho, AptoMedicoRequestDTO::setAgudezaVisualOjoDerecho);

        binder.forField(agudezaVisualOjoIzquierdo)
                .asRequired("La agudeza visual del ojo izquierdo es obligatoria")
                .bind(AptoMedicoRequestDTO::getAgudezaVisualOjoIzquierdo, AptoMedicoRequestDTO::setAgudezaVisualOjoIzquierdo);

        binder.forField(campoVisualNormal)
                .bind(AptoMedicoRequestDTO::getCampoVisualNormal, AptoMedicoRequestDTO::setCampoVisualNormal);

        binder.forField(visionCromaticaNormal)
                .bind(AptoMedicoRequestDTO::getVisionCromaticaNormal, AptoMedicoRequestDTO::setVisionCromaticaNormal);

        binder.forField(audicionNormal)
                .bind(AptoMedicoRequestDTO::getAudicionNormal, AptoMedicoRequestDTO::setAudicionNormal);

        binder.forField(reflejosNormales)
                .bind(AptoMedicoRequestDTO::getReflejosNormales, AptoMedicoRequestDTO::setReflejosNormales);

        binder.forField(coordinacionNormal)
                .bind(AptoMedicoRequestDTO::getCoordinacionNormal, AptoMedicoRequestDTO::setCoordinacionNormal);

        binder.forField(equilibrioNormal)
                .bind(AptoMedicoRequestDTO::getEquilibrioNormal, AptoMedicoRequestDTO::setEquilibrioNormal);

        binder.forField(cardiovascularNormal)
                .bind(AptoMedicoRequestDTO::getCardiovascularNormal, AptoMedicoRequestDTO::setCardiovascularNormal);

        binder.forField(sistemaLocomotorNormal)
                .bind(AptoMedicoRequestDTO::getSistemaLocomotorNormal, AptoMedicoRequestDTO::setSistemaLocomotorNormal);

        binder.forField(observacionesField)
                .bind(AptoMedicoRequestDTO::getObservaciones, AptoMedicoRequestDTO::setObservaciones);

        binder.forField(restriccionesField)
                .bind(AptoMedicoRequestDTO::getRestricciones, AptoMedicoRequestDTO::setRestricciones);

        binder.forField(mesesValidezField)
                .asRequired("Los meses de validez son obligatorios")
                .withConverter(Double::intValue, Integer::doubleValue)
                .bind(AptoMedicoRequestDTO::getMesesValidez, AptoMedicoRequestDTO::setMesesValidez);
    }

    private void registrarAptoMedico() {
        try {
            AptoMedicoRequestDTO request = new AptoMedicoRequestDTO();
            binder.writeBean(request);

            AptoMedicoResponseDTO response = tramiteService.registrarAptoMedico(tramite.getId(), request);

            String mensaje = response.getApto() ? 
                "Apto médico registrado exitosamente. Válido hasta: " + response.getFechaVencimiento() :
                "Registrado como NO APTO para conducir";
            
            showNotification(mensaje, NotificationVariant.LUMO_SUCCESS);
            onSuccess.accept(null);
            close();

        } catch (ValidationException e) {
            showNotification("Por favor, complete todos los campos obligatorios", NotificationVariant.LUMO_ERROR);
        } catch (IllegalStateException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("Error al registrar apto médico: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
