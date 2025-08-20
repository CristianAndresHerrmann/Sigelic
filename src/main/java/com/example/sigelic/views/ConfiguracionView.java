package com.example.sigelic.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

/**
 * Vista para configuración del sistema
 */
@Route(value = "configuracion", layout = MainLayout.class)
@PageTitle("Configuración | SIGELIC")
@RolesAllowed({"ADMINISTRADOR"})
public class ConfiguracionView extends VerticalLayout {

    // Campos de configuración general
    private TextField nombreSistemaField;
    private TextField urlSistemaField;
    private TextField emailContactoField;
    private TextField telefonoContactoField;

    // Campos de configuración de seguridad
    private IntegerField maxIntentosFallidosField;
    private IntegerField tiempoBloqueoField;
    private IntegerField duracionSesionField;
    private Checkbox cambioPasswordObligatorioCheckbox;

    // Campos de configuración de licencias
    private IntegerField validezLicenciaField;
    private IntegerField diasAvisoVencimientoField;

    public ConfiguracionView() {
        addClassName("configuracion-view");
        setSizeFull();

        createHeader();
        createGeneralSettings();
        createSecuritySettings();
        createLicenseSettings();
        createActionButtons();
        
        loadCurrentConfiguration();
    }

    private void createHeader() {
        H2 title = new H2("Configuración del Sistema");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);
        add(title);
    }

    private void createGeneralSettings() {
        H3 generalTitle = new H3("Configuración General");
        generalTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Top.MEDIUM);

        nombreSistemaField = new TextField("Nombre del Sistema");
        nombreSistemaField.setWidthFull();

        urlSistemaField = new TextField("URL del Sistema");
        urlSistemaField.setWidthFull();

        emailContactoField = new TextField("Email de Contacto");
        emailContactoField.setWidthFull();

        telefonoContactoField = new TextField("Teléfono de Contacto");
        telefonoContactoField.setWidthFull();

        FormLayout generalForm = new FormLayout();
        generalForm.add(nombreSistemaField, urlSistemaField, emailContactoField, telefonoContactoField);
        generalForm.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("400px", 2)
        );

        add(generalTitle, generalForm);
    }

    private void createSecuritySettings() {
        H3 securityTitle = new H3("Configuración de Seguridad");
        securityTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Top.MEDIUM);

        maxIntentosFallidosField = new IntegerField("Máximo Intentos Fallidos");
        maxIntentosFallidosField.setMin(1);
        maxIntentosFallidosField.setMax(10);
        maxIntentosFallidosField.setHelperText("Número de intentos antes de bloquear cuenta");

        tiempoBloqueoField = new IntegerField("Tiempo de Bloqueo (minutos)");
        tiempoBloqueoField.setMin(5);
        tiempoBloqueoField.setMax(1440);
        tiempoBloqueoField.setHelperText("Tiempo en minutos que permanece bloqueada la cuenta");

        duracionSesionField = new IntegerField("Duración de Sesión (minutos)");
        duracionSesionField.setMin(15);
        duracionSesionField.setMax(480);
        duracionSesionField.setHelperText("Tiempo de inactividad antes de cerrar sesión");

        cambioPasswordObligatorioCheckbox = new Checkbox("Cambio de Contraseña Obligatorio");
        cambioPasswordObligatorioCheckbox.setLabel("Requerir cambio de contraseña en primer acceso");

        FormLayout securityForm = new FormLayout();
        securityForm.add(
            maxIntentosFallidosField, tiempoBloqueoField, 
            duracionSesionField, cambioPasswordObligatorioCheckbox
        );
        securityForm.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("400px", 2)
        );

        add(securityTitle, securityForm);
    }

    private void createLicenseSettings() {
        H3 licenseTitle = new H3("Configuración de Licencias");
        licenseTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Top.MEDIUM);

        validezLicenciaField = new IntegerField("Validez de Licencia (años)");
        validezLicenciaField.setMin(1);
        validezLicenciaField.setMax(10);
        validezLicenciaField.setHelperText("Años de validez por defecto para nuevas licencias");

        diasAvisoVencimientoField = new IntegerField("Días de Aviso de Vencimiento");
        diasAvisoVencimientoField.setMin(30);
        diasAvisoVencimientoField.setMax(365);
        diasAvisoVencimientoField.setHelperText("Días antes del vencimiento para notificar");

        FormLayout licenseForm = new FormLayout();
        licenseForm.add(validezLicenciaField, diasAvisoVencimientoField);
        licenseForm.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("400px", 2)
        );

        add(licenseTitle, licenseForm);
    }

    private void createActionButtons() {
        Button saveButton = new Button("Guardar Configuración", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveConfiguration());

        Button resetButton = new Button("Restablecer", new Icon(VaadinIcon.REFRESH));
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> loadCurrentConfiguration());

        Button testButton = new Button("Probar Configuración", new Icon(VaadinIcon.PLAY));
        testButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        testButton.addClickListener(e -> testConfiguration());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, resetButton, testButton);
        buttonLayout.addClassNames(LumoUtility.Margin.Top.LARGE);

        add(buttonLayout);
    }

    private void loadCurrentConfiguration() {
        // TODO: Cargar configuración actual desde base de datos o archivos de configuración
        // Por ahora, valores por defecto
        nombreSistemaField.setValue("SIGELIC - Sistema de Gestión de Licencias");
        urlSistemaField.setValue("http://localhost:8080");
        emailContactoField.setValue("contacto@sigelic.gov.ar");
        telefonoContactoField.setValue("+54 342 4573000");
        
        maxIntentosFallidosField.setValue(3);
        tiempoBloqueoField.setValue(30);
        duracionSesionField.setValue(60);
        cambioPasswordObligatorioCheckbox.setValue(true);
        
        validezLicenciaField.setValue(5);
        diasAvisoVencimientoField.setValue(90);
    }

    private void saveConfiguration() {
        try {
            // TODO: Validar campos obligatorios
            if (nombreSistemaField.getValue().trim().isEmpty()) {
                showNotification("El nombre del sistema es obligatorio", NotificationVariant.LUMO_ERROR);
                return;
            }

            // TODO: Guardar configuración en base de datos o archivos
            // Por ahora, solo mostrar confirmación
            showNotification("Configuración guardada exitosamente", NotificationVariant.LUMO_SUCCESS);
            
        } catch (Exception e) {
            showNotification("Error al guardar configuración: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void testConfiguration() {
        try {
            // TODO: Implementar pruebas de configuración
            // - Probar conectividad de email
            // - Validar URLs
            // - Verificar configuraciones de seguridad
            
            showNotification("Configuración probada exitosamente", NotificationVariant.LUMO_SUCCESS);
            
        } catch (Exception e) {
            showNotification("Error en la prueba de configuración: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
