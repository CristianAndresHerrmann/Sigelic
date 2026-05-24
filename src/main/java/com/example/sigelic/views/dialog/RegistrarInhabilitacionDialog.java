package com.example.sigelic.views.dialog;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import com.example.sigelic.model.Inhabilitacion;
import com.example.sigelic.model.Titular;
import com.example.sigelic.service.TitularService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class RegistrarInhabilitacionDialog extends Dialog {

    private final TitularService titularService;
    private final Consumer<Void> onSuccess;

    private TextField dniField;
    private Button buscarBtn;
    private Span titularInfoSpan;

    private TextArea motivoField;
    private DatePicker fechaInicioField;
    private DatePicker fechaFinField;
    private TextField autoridadField;
    private TextField numeroExpedienteField;

    private Titular selectedTitular = null;
    private Binder<Inhabilitacion> binder;
    private Button guardarBtn;

    public RegistrarInhabilitacionDialog(TitularService titularService, Consumer<Void> onSuccess) {
        this.titularService = titularService;
        this.onSuccess = onSuccess;

        setHeaderTitle("Registrar Inhabilitación");
        setModal(true);
        setWidth("550px");
        setResizable(false);

        createForm();
        createButtons();
        setupBinder();
    }

    private void createForm() {
        H3 title = new H3("Información de la Sanción");
        title.getStyle().set("margin-top", "0");

        // Sección Titular
        dniField = new TextField("DNI del Titular");
        dniField.setPlaceholder("Escriba el DNI...");
        dniField.setRequiredIndicatorVisible(true);

        buscarBtn = new Button("Buscar", new Icon(VaadinIcon.SEARCH));
        buscarBtn.addClickListener(e -> buscarTitular());

        HorizontalLayout dniLayout = new HorizontalLayout(dniField, buscarBtn);
        dniLayout.setAlignItems(FlexComponent.Alignment.END);
        dniLayout.setWidthFull();

        titularInfoSpan = new Span("Ingrese DNI para buscar titular...");
        titularInfoSpan.getStyle()
                .set("padding", "var(--lumo-space-xs)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Campos de inhabilitación
        motivoField = new TextArea("Motivo / Causa");
        motivoField.setPlaceholder("Describa el motivo de la inhabilitación...");
        motivoField.setRequiredIndicatorVisible(true);
        motivoField.setMaxLength(500);
        motivoField.setEnabled(false);

        fechaInicioField = new DatePicker("Fecha de Inicio");
        fechaInicioField.setValue(LocalDate.now());
        fechaInicioField.setRequiredIndicatorVisible(true);
        fechaInicioField.setEnabled(false);

        fechaFinField = new DatePicker("Fecha de Finalización (Opcional)");
        fechaFinField.setPlaceholder("Indefinida si se deja vacío");
        fechaFinField.setEnabled(false);

        autoridadField = new TextField("Autoridad Competente");
        autoridadField.setPlaceholder("Ej: Juzgado de Faltas Nro 1");
        autoridadField.setRequiredIndicatorVisible(true);
        autoridadField.setEnabled(false);

        numeroExpedienteField = new TextField("Número de Expediente");
        numeroExpedienteField.setPlaceholder("Ej: EXP-2026-99123");
        numeroExpedienteField.setEnabled(false);

        FormLayout formLayout = new FormLayout();
        formLayout.add(dniLayout);
        formLayout.add(titularInfoSpan);
        formLayout.add(motivoField, fechaInicioField, fechaFinField, autoridadField, numeroExpedienteField);
        
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("450px", 2)
        );

        formLayout.setColspan(dniLayout, 2);
        formLayout.setColspan(titularInfoSpan, 2);
        formLayout.setColspan(motivoField, 2);
        formLayout.setColspan(fechaInicioField, 1);
        formLayout.setColspan(fechaFinField, 1);
        formLayout.setColspan(autoridadField, 2);
        formLayout.setColspan(numeroExpedienteField, 2);

        add(formLayout);
    }

    private void createButtons() {
        Button cancelarBtn = new Button("Cancelar", e -> close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        guardarBtn = new Button("Registrar", e -> guardarInhabilitacion());
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardarBtn.setEnabled(false);

        HorizontalLayout footer = new HorizontalLayout(cancelarBtn, guardarBtn);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        getFooter().add(footer);
    }

    private void setupBinder() {
        binder = new Binder<>(Inhabilitacion.class);

        binder.forField(motivoField)
            .withValidator(new StringLengthValidator("El motivo es obligatorio y no puede superar 500 caracteres", 1, 500))
            .bind(Inhabilitacion::getMotivo, Inhabilitacion::setMotivo);

        binder.forField(fechaInicioField)
            .withValidator(fecha -> fecha != null, "La fecha de inicio es obligatoria")
            .bind(Inhabilitacion::getFechaInicio, Inhabilitacion::setFechaInicio);

        binder.forField(fechaFinField)
            .bind(Inhabilitacion::getFechaFin, Inhabilitacion::setFechaFin);

        binder.forField(autoridadField)
            .withValidator(new StringLengthValidator("La autoridad es obligatoria y no puede superar 100 caracteres", 1, 100))
            .bind(Inhabilitacion::getAutoridad, Inhabilitacion::setAutoridad);

        binder.forField(numeroExpedienteField)
            .withValidator(new StringLengthValidator("El número de expediente no puede superar 100 caracteres", 0, 100))
            .bind(Inhabilitacion::getNumeroExpediente, Inhabilitacion::setNumeroExpediente);
    }

    private void buscarTitular() {
        String dni = dniField.getValue();
        if (dni == null || dni.trim().isEmpty()) {
            showNotification("El DNI es obligatorio", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Optional<Titular> titularOpt = titularService.findByDni(dni.trim());
            if (titularOpt.isPresent()) {
                Titular titular = titularOpt.get();
                selectedTitular = titular;
                boolean yaInhabilitado = titular.tieneInhabilitacionesActivas();

                if (yaInhabilitado) {
                    titularInfoSpan.setText("Titular: " + titular.getNombre() + " " + titular.getApellido() + " - DNI: " + titular.getDni() + " [YA POSEE INHABILITACIONES ACTIVAS]");
                    titularInfoSpan.getStyle().set("color", "var(--lumo-warning-text-color)");
                } else {
                    titularInfoSpan.setText("Titular: " + titular.getNombre() + " " + titular.getApellido() + " - DNI: " + titular.getDni());
                    titularInfoSpan.getStyle().set("color", "var(--lumo-success-text-color)");
                }
                
                motivoField.setEnabled(true);
                fechaInicioField.setEnabled(true);
                fechaFinField.setEnabled(true);
                autoridadField.setEnabled(true);
                numeroExpedienteField.setEnabled(true);
                guardarBtn.setEnabled(true);
            } else {
                selectedTitular = null;
                titularInfoSpan.setText("Titular no encontrado en el sistema.");
                titularInfoSpan.getStyle().set("color", "var(--lumo-error-text-color)");
                
                motivoField.setEnabled(false);
                fechaInicioField.setEnabled(false);
                fechaFinField.setEnabled(false);
                autoridadField.setEnabled(false);
                numeroExpedienteField.setEnabled(false);
                guardarBtn.setEnabled(false);
            }
        } catch (Exception ex) {
            showNotification("Error al buscar titular: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void guardarInhabilitacion() {
        if (selectedTitular == null) {
            showNotification("Debe buscar y seleccionar un titular primero", NotificationVariant.LUMO_ERROR);
            return;
        }

        Inhabilitacion inhabilitacion = new Inhabilitacion();
        try {
            binder.writeBean(inhabilitacion);

            if (inhabilitacion.getFechaFin() != null && inhabilitacion.getFechaFin().isBefore(inhabilitacion.getFechaInicio())) {
                showNotification("La fecha de fin no puede ser anterior a la de inicio", NotificationVariant.LUMO_ERROR);
                return;
            }

            titularService.agregarInhabilitacion(selectedTitular.getId(), inhabilitacion);
            showNotification("Inhabilitación registrada correctamente", NotificationVariant.LUMO_SUCCESS);
            onSuccess.accept(null);
            close();
        } catch (ValidationException e) {
            showNotification("Por favor, complete los campos requeridos y corrija los errores", NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("Error al registrar inhabilitación: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
