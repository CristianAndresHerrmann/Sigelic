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
 * Diálogo para editar un titular existente
 */
public class EditarTitularDialog extends Dialog {

    private final TitularService titularService;
    private final Consumer<Void> onSuccess;
    private final Titular titularOriginal;

    private TextField nombreField;
    private TextField apellidoField;
    private TextField dniField;
    private DatePicker fechaNacimientoPicker;
    private TextField domicilioField;
    private EmailField emailField;
    private TextField telefonoField;
    private Span validationInfo;
    private Span edadInfo;

    private final Binder<TitularRequestDTO> binder;

    public EditarTitularDialog(Titular titular, TitularService titularService, Consumer<Void> onSuccess) {
        this.titularOriginal = titular;
        this.titularService = titularService;
        this.onSuccess = onSuccess;
        this.binder = new Binder<>(TitularRequestDTO.class);

        initializeDialog();
        createFormFields();
        setupValidation();
        createButtons();
        loadTitularData();
    }

    private void initializeDialog() {
        setModal(true);
        setDraggable(true);
        setResizable(false);
        setWidth("600px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        // Header
        H3 title = new H3("Editar Titular");
        title.getStyle().set("margin", "0").set("color", "var(--lumo-primary-text-color)");
        
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        closeButton.addClickListener(e -> close());
        
        HorizontalLayout header = new HorizontalLayout(title, closeButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "var(--lumo-space-m)")
                         .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        getHeader().add(header);
    }

    private void createFormFields() {
        // Crear campos del formulario
        nombreField = new TextField("Nombre");
        nombreField.setWidthFull();
        nombreField.setRequired(true);
        nombreField.setRequiredIndicatorVisible(true);

        apellidoField = new TextField("Apellido");
        apellidoField.setWidthFull();
        apellidoField.setRequired(true);
        apellidoField.setRequiredIndicatorVisible(true);

        dniField = new TextField("DNI");
        dniField.setWidthFull();
        dniField.setRequired(true);
        dniField.setRequiredIndicatorVisible(true);
        dniField.setHelperText("8 dígitos sin puntos ni espacios");

        fechaNacimientoPicker = new DatePicker("Fecha de Nacimiento");
        fechaNacimientoPicker.setWidthFull();
        fechaNacimientoPicker.setRequired(true);
        fechaNacimientoPicker.setRequiredIndicatorVisible(true);
        fechaNacimientoPicker.setMax(LocalDate.now().minusYears(16));
        fechaNacimientoPicker.setMin(LocalDate.now().minusYears(100));

        domicilioField = new TextField("Domicilio");
        domicilioField.setWidthFull();
        domicilioField.setRequired(true);
        domicilioField.setRequiredIndicatorVisible(true);

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setRequired(true);
        emailField.setRequiredIndicatorVisible(true);

        telefonoField = new TextField("Teléfono");
        telefonoField.setWidthFull();
        telefonoField.setHelperText("Opcional - formato: +54 9 342 123-4567");

        // Información de validación
        validationInfo = new Span();
        validationInfo.getStyle().set("font-size", "var(--lumo-font-size-s)")
                                 .set("color", "var(--lumo-secondary-text-color)");

        // Información de edad
        edadInfo = new Span();
        edadInfo.getStyle().set("font-size", "var(--lumo-font-size-s)")
                          .set("font-weight", "bold")
                          .set("color", "var(--lumo-primary-text-color)");

        // Layout del formulario
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );

        formLayout.add(nombreField, apellidoField);
        formLayout.add(dniField, fechaNacimientoPicker);
        formLayout.add(domicilioField, emailField);
        formLayout.add(telefonoField);
        formLayout.setColspan(domicilioField, 1);
        formLayout.setColspan(emailField, 1);
        formLayout.setColspan(telefonoField, 2);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.add(formLayout, validationInfo, edadInfo);

        add(content);
    }

    private void setupValidation() {
        // Validación de nombre
        binder.forField(nombreField)
                .withValidator(new StringLengthValidator("El nombre debe tener entre 2 y 50 caracteres", 2, 50))
                .withValidator(new RegexpValidator("El nombre solo puede contener letras y espacios", 
                              "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))
                .bind(TitularRequestDTO::getNombre, TitularRequestDTO::setNombre);

        // Validación de apellido
        binder.forField(apellidoField)
                .withValidator(new StringLengthValidator("El apellido debe tener entre 2 y 50 caracteres", 2, 50))
                .withValidator(new RegexpValidator("El apellido solo puede contener letras y espacios", 
                              "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))
                .bind(TitularRequestDTO::getApellido, TitularRequestDTO::setApellido);

        // Validación de DNI
        binder.forField(dniField)
                .withValidator(new StringLengthValidator("El DNI debe tener exactamente 8 dígitos", 8, 8))
                .withValidator(new RegexpValidator("El DNI solo puede contener números", "^[0-9]+$"))
                .withValidator(dni -> !titularService.existsByDni(dni) || dni.equals(titularOriginal.getDni()), 
                              "Ya existe un titular con este DNI")
                .bind(TitularRequestDTO::getDni, TitularRequestDTO::setDni);

        // Validación de fecha de nacimiento
        binder.forField(fechaNacimientoPicker)
                .withValidator(fecha -> fecha != null, "La fecha de nacimiento es obligatoria")
                .withValidator(fecha -> fecha.isBefore(LocalDate.now().minusYears(16)), 
                              "El titular debe tener al menos 16 años")
                .withValidator(fecha -> fecha.isAfter(LocalDate.now().minusYears(100)), 
                              "La fecha de nacimiento no puede ser anterior a 100 años")
                .bind(TitularRequestDTO::getFechaNacimiento, TitularRequestDTO::setFechaNacimiento);

        // Validación de domicilio
        binder.forField(domicilioField)
                .withValidator(new StringLengthValidator("El domicilio debe tener entre 5 y 100 caracteres", 5, 100))
                .bind(TitularRequestDTO::getDomicilio, TitularRequestDTO::setDomicilio);

        // Validación de email
        binder.forField(emailField)
                .withValidator(new EmailValidator("Ingrese un email válido"))
                .withValidator(email -> !titularService.existsByEmail(email) || email.equals(titularOriginal.getEmail()), 
                              "Ya existe un titular con este email")
                .bind(TitularRequestDTO::getEmail, TitularRequestDTO::setEmail);

        // Validación de teléfono (opcional)
        binder.forField(telefonoField)
                .withValidator(telefono -> telefono == null || telefono.trim().isEmpty() || telefono.length() >= 8,
                              "El teléfono debe tener al menos 8 caracteres")
                .bind(TitularRequestDTO::getTelefono, TitularRequestDTO::setTelefono);

        // Listener para calcular edad automáticamente
        fechaNacimientoPicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                int edad = LocalDate.now().getYear() - e.getValue().getYear();
                edadInfo.setText("Edad: " + edad + " años");
                
                if (edad >= 17) {
                    edadInfo.getStyle().set("color", "var(--lumo-success-color)");
                    edadInfo.setText(edadInfo.getText() + " ✓ Apto para licencia de conducir");
                } else if (edad == 16) {
                    edadInfo.getStyle().set("color", "var(--lumo-warning-color)");
                    edadInfo.setText(edadInfo.getText() + " ⚠ Solo apto para licencia de ciclomotor");
                } else {
                    edadInfo.getStyle().set("color", "var(--lumo-error-color)");
                    edadInfo.setText(edadInfo.getText() + " ✗ No apto para ninguna licencia");
                }
            } else {
                edadInfo.setText("");
            }
        });

        // Listener para validación en tiempo real de DNI
        dniField.addValueChangeListener(e -> {
            String dni = e.getValue();
            if (dni != null && dni.length() == 8 && dni.matches("^[0-9]+$")) {
                if (titularService.existsByDni(dni) && !dni.equals(titularOriginal.getDni())) {
                    validationInfo.setText("⚠ Ya existe un titular con el DNI " + dni);
                    validationInfo.getStyle().set("color", "var(--lumo-error-color)");
                } else {
                    validationInfo.setText("✓ DNI disponible");
                    validationInfo.getStyle().set("color", "var(--lumo-success-color)");
                }
            } else {
                validationInfo.setText("");
            }
        });

        // Listener para validación en tiempo real de email
        emailField.addValueChangeListener(e -> {
            String email = e.getValue();
            if (email != null && !email.trim().isEmpty() && email.contains("@")) {
                if (titularService.existsByEmail(email) && !email.equals(titularOriginal.getEmail())) {
                    if (validationInfo.getText().isEmpty()) {
                        validationInfo.setText("⚠ Ya existe un titular con el email " + email);
                        validationInfo.getStyle().set("color", "var(--lumo-error-color)");
                    }
                } else {
                    if (!validationInfo.getText().contains("DNI")) {
                        validationInfo.setText("✓ Email disponible");
                        validationInfo.getStyle().set("color", "var(--lumo-success-color)");
                    }
                }
            }
        });
    }

    private void createButtons() {
        Button cancelButton = new Button("Cancelar");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> close());

        Button saveButton = new Button("Guardar Cambios");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setIcon(new Icon(VaadinIcon.CHECK));
        saveButton.addClickListener(e -> updateTitular());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.getStyle().set("padding", "var(--lumo-space-m)")
                              .set("border-top", "1px solid var(--lumo-contrast-10pct)");

        getFooter().add(buttonLayout);
    }

    private void loadTitularData() {
        // Crear DTO con los datos actuales del titular
        TitularRequestDTO dto = new TitularRequestDTO();
        dto.setNombre(titularOriginal.getNombre());
        dto.setApellido(titularOriginal.getApellido());
        dto.setDni(titularOriginal.getDni());
        dto.setFechaNacimiento(titularOriginal.getFechaNacimiento());
        dto.setDomicilio(titularOriginal.getDomicilio());
        dto.setEmail(titularOriginal.getEmail());
        dto.setTelefono(titularOriginal.getTelefono());

        // Cargar los datos en el formulario
        binder.readBean(dto);

        // Calcular y mostrar edad actual
        int edad = titularOriginal.getEdad();
        edadInfo.setText("Edad: " + edad + " años");
        if (edad >= 17) {
            edadInfo.getStyle().set("color", "var(--lumo-success-color)");
            edadInfo.setText(edadInfo.getText() + " ✓ Apto para licencia de conducir");
        } else if (edad == 16) {
            edadInfo.getStyle().set("color", "var(--lumo-warning-color)");
            edadInfo.setText(edadInfo.getText() + " ⚠ Solo apto para licencia de ciclomotor");
        } else {
            edadInfo.getStyle().set("color", "var(--lumo-error-color)");
            edadInfo.setText(edadInfo.getText() + " ✗ No apto para ninguna licencia");
        }
    }

    private void updateTitular() {
        try {
            TitularRequestDTO dto = new TitularRequestDTO();
            binder.writeBean(dto);

            // Actualizar el titular existente
            titularOriginal.setNombre(dto.getNombre());
            titularOriginal.setApellido(dto.getApellido());
            titularOriginal.setDni(dto.getDni());
            titularOriginal.setFechaNacimiento(dto.getFechaNacimiento());
            titularOriginal.setDomicilio(dto.getDomicilio());
            titularOriginal.setEmail(dto.getEmail());
            titularOriginal.setTelefono(dto.getTelefono());

            titularService.update(titularOriginal);

            Notification.show("Titular actualizado exitosamente", 3000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();
            if (onSuccess != null) {
                onSuccess.accept(null);
            }

        } catch (ValidationException e) {
            Notification.show("Por favor corrija los errores en el formulario", 3000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error al actualizar el titular: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
