package com.example.sigelic.views.dialog;

import java.time.LocalDate;
import java.util.function.Consumer;

import com.example.sigelic.dto.request.TitularRequestDTO;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

/**
 * Diálogo para crear un nuevo titular
 */
public class NuevoTitularDialog extends Dialog {

    private final TitularService titularService;
    private final Consumer<Void> onSuccess;

    private TextField nombreField;
    private TextField apellidoField;
    private TextField dniField;
    private DatePicker fechaNacimientoPicker;
    private TextField domicilioField;
    private EmailField emailField;
    private TextField telefonoField;
    private Span validationInfo;

    private final Binder<TitularRequestDTO> binder;

    public NuevoTitularDialog(TitularService titularService, Consumer<Void> onSuccess) {
        this.titularService = titularService;
        this.onSuccess = onSuccess;
        this.binder = new Binder<>(TitularRequestDTO.class);

        setHeaderTitle("Nuevo Titular");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("700px");

        createForm();
        createButtons();
        bindFields();
    }

    private void createForm() {
        H3 title = new H3("Información Personal");
        title.getStyle().set("margin-top", "0");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        // Campo Nombre
        nombreField = new TextField("Nombre");
        nombreField.setPlaceholder("Ingrese el nombre...");
        nombreField.setRequired(true);
        nombreField.setMaxLength(100);

        // Campo Apellido
        apellidoField = new TextField("Apellido");
        apellidoField.setPlaceholder("Ingrese el apellido...");
        apellidoField.setRequired(true);
        apellidoField.setMaxLength(100);

        // Campo DNI
        dniField = new TextField("DNI");
        dniField.setPlaceholder("12345678");
        dniField.setRequired(true);
        dniField.setMaxLength(8);
        dniField.setHelperText("Solo números, sin puntos ni espacios");
        dniField.addValueChangeListener(e -> validateDniUniqueness());

        // Campo Fecha de Nacimiento
        fechaNacimientoPicker = new DatePicker("Fecha de Nacimiento");
        fechaNacimientoPicker.setPlaceholder("dd/mm/yyyy");
        fechaNacimientoPicker.setRequired(true);
        fechaNacimientoPicker.setMax(LocalDate.now().minusYears(16)); // Mínimo 16 años
        fechaNacimientoPicker.setMin(LocalDate.now().minusYears(100)); // Máximo 100 años
        fechaNacimientoPicker.addValueChangeListener(e -> updateAgeInfo());

        // Campo Domicilio
        domicilioField = new TextField("Domicilio");
        domicilioField.setPlaceholder("Dirección completa...");
        domicilioField.setRequired(true);
        domicilioField.setMaxLength(200);

        // Campo Email
        emailField = new EmailField("Email");
        emailField.setPlaceholder("ejemplo@correo.com");
        emailField.setMaxLength(100);
        emailField.setHelperText("Opcional");
        emailField.addValueChangeListener(e -> validateEmailUniqueness());

        // Campo Teléfono
        telefonoField = new TextField("Teléfono");
        telefonoField.setPlaceholder("3414567890");
        telefonoField.setMaxLength(20);
        telefonoField.setHelperText("Opcional");

        // Información de validación
        validationInfo = new Span();
        validationInfo.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");
        validationInfo.setVisible(false);

        formLayout.add(
                nombreField, apellidoField,
                dniField, fechaNacimientoPicker,
                domicilioField, emailField,
                telefonoField
        );
        formLayout.setColspan(domicilioField, 2);

        VerticalLayout content = new VerticalLayout(title, formLayout, validationInfo);
        content.setPadding(false);
        content.setSpacing(true);

        add(content);
    }

    private void createButtons() {
        Button cancelButton = new Button("Cancelar", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveButton = new Button("Crear Titular", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> crearTitular());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setPadding(true);

        getFooter().add(buttonLayout);
    }

    private void bindFields() {
        // Validación para nombre
        binder.forField(nombreField)
                .asRequired("El nombre es obligatorio")
                .withValidator(new StringLengthValidator("El nombre debe tener entre 2 y 100 caracteres", 2, 100))
                .bind(TitularRequestDTO::getNombre, TitularRequestDTO::setNombre);

        // Validación para apellido
        binder.forField(apellidoField)
                .asRequired("El apellido es obligatorio")
                .withValidator(new StringLengthValidator("El apellido debe tener entre 2 y 100 caracteres", 2, 100))
                .bind(TitularRequestDTO::getApellido, TitularRequestDTO::setApellido);

        // Validación para DNI
        binder.forField(dniField)
                .asRequired("El DNI es obligatorio")
                .withValidator(new StringLengthValidator("El DNI debe tener entre 7 y 8 dígitos", 7, 8))
                .withValidator(new RegexpValidator("El DNI debe contener solo números", "\\d+"))
                .bind(TitularRequestDTO::getDni, TitularRequestDTO::setDni);

        // Validación para fecha de nacimiento
        binder.forField(fechaNacimientoPicker)
                .asRequired("La fecha de nacimiento es obligatoria")
                .withValidator(fecha -> fecha.isBefore(LocalDate.now().minusYears(16)), 
                             "Debe ser mayor de 16 años para obtener una licencia")
                .withValidator(fecha -> fecha.isAfter(LocalDate.now().minusYears(100)), 
                             "Fecha de nacimiento no válida")
                .bind(TitularRequestDTO::getFechaNacimiento, TitularRequestDTO::setFechaNacimiento);

        // Validación para domicilio
        binder.forField(domicilioField)
                .asRequired("El domicilio es obligatorio")
                .withValidator(new StringLengthValidator("El domicilio debe tener entre 5 y 200 caracteres", 5, 200))
                .bind(TitularRequestDTO::getDomicilio, TitularRequestDTO::setDomicilio);

        // Validación para email (opcional)
        binder.forField(emailField)
                .withValidator(new EmailValidator("Formato de email inválido"))
                .bind(TitularRequestDTO::getEmail, TitularRequestDTO::setEmail);

        // Validación para teléfono (opcional)
        binder.forField(telefonoField)
                .withValidator(new StringLengthValidator("El teléfono no puede exceder 20 caracteres", 0, 20))
                .bind(TitularRequestDTO::getTelefono, TitularRequestDTO::setTelefono);
    }

    private void validateDniUniqueness() {
        String dni = dniField.getValue();
        if (dni != null && dni.length() >= 7) {
            try {
                boolean existe = titularService.existsByDni(dni);
                if (existe) {
                    dniField.setErrorMessage("Ya existe un titular con este DNI");
                    dniField.setInvalid(true);
                } else {
                    dniField.setInvalid(false);
                }
            } catch (Exception e) {
                // Error al verificar, no marcar como inválido pero log el error
                System.err.println("Error al verificar DNI: " + e.getMessage());
            }
        }
    }

    private void validateEmailUniqueness() {
        String email = emailField.getValue();
        if (email != null && !email.trim().isEmpty()) {
            try {
                boolean existe = titularService.existsByEmail(email);
                if (existe) {
                    emailField.setErrorMessage("Ya existe un titular con este email");
                    emailField.setInvalid(true);
                } else {
                    emailField.setInvalid(false);
                }
            } catch (Exception e) {
                // Error al verificar, no marcar como inválido pero log el error
                System.err.println("Error al verificar email: " + e.getMessage());
            }
        }
    }

    private void updateAgeInfo() {
        LocalDate fechaNacimiento = fechaNacimientoPicker.getValue();
        if (fechaNacimiento != null) {
            int edad = LocalDate.now().getYear() - fechaNacimiento.getYear();
            if (LocalDate.now().getDayOfYear() < fechaNacimiento.getDayOfYear()) {
                edad--;
            }

            String info = String.format("Edad: %d años - ", edad);
            
            if (edad < 17) {
                info += "No apto para ninguna clase de licencia";
                validationInfo.getStyle().set("color", "var(--lumo-error-text-color)");
            } else if (edad < 21) {
                info += "Apto para clases A y B únicamente";
                validationInfo.getStyle().set("color", "var(--lumo-warning-text-color)");
            } else {
                info += "Apto para todas las clases de licencia";
                validationInfo.getStyle().set("color", "var(--lumo-success-text-color)");
            }
            
            validationInfo.setText(info);
            validationInfo.setVisible(true);
        } else {
            validationInfo.setVisible(false);
        }
    }

    private void crearTitular() {
        try {
            TitularRequestDTO titularDTO = new TitularRequestDTO();
            binder.writeBean(titularDTO);

            // Validaciones adicionales de negocio
            if (dniField.isInvalid() || emailField.isInvalid()) {
                showNotification("Por favor, corrija los errores de validación antes de continuar", 
                               NotificationVariant.LUMO_ERROR);
                return;
            }

            // Crear el titular usando el servicio
            Titular titular = titularService.createFromDTO(titularDTO);
            
            showNotification("Titular creado exitosamente: " + titular.getNombre() + " " + titular.getApellido(), 
                           NotificationVariant.LUMO_SUCCESS);
            onSuccess.accept(null);
            close();

        } catch (ValidationException e) {
            showNotification("Por favor, complete todos los campos obligatorios correctamente", 
                           NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("Error al crear titular: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(5000);
    }
}
