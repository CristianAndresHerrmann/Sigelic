package com.example.sigelic.views;

import com.example.sigelic.model.Configuracion;
import com.example.sigelic.service.ConfiguracionService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Vista para configuración del sistema
 */
@Route(value = "configuracion", layout = MainLayout.class)
@PageTitle("Configuración | SIGELIC")
@RolesAllowed({"ADMINISTRADOR"})
@Slf4j
public class ConfiguracionView extends VerticalLayout {

    private final ConfiguracionService configuracionService;

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

    public ConfiguracionView(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
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
        try {
            log.info("Cargando configuración actual del sistema");
            
            // Cargar configuración general
            configuracionService.getValor("sistema.nombre")
                .ifPresent(nombreSistemaField::setValue);
            
            configuracionService.getValor("sistema.url")
                .ifPresent(urlSistemaField::setValue);
            
            configuracionService.getValor("contacto.email")
                .ifPresent(emailContactoField::setValue);
            
            configuracionService.getValor("contacto.telefono")
                .ifPresent(telefonoContactoField::setValue);
            
            // Cargar configuración de seguridad
            configuracionService.getValorComoInteger("seguridad.max_intentos_fallidos")
                .ifPresent(maxIntentosFallidosField::setValue);
            
            configuracionService.getValorComoInteger("seguridad.tiempo_bloqueo_minutos")
                .ifPresent(tiempoBloqueoField::setValue);
            
            configuracionService.getValorComoInteger("seguridad.duracion_sesion_minutos")
                .ifPresent(duracionSesionField::setValue);
            
            configuracionService.getValorComoBoolean("seguridad.cambio_password_obligatorio")
                .ifPresent(cambioPasswordObligatorioCheckbox::setValue);
            
            // Cargar configuración de licencias
            configuracionService.getValorComoInteger("licencias.validez_anos")
                .ifPresent(validezLicenciaField::setValue);
            
            configuracionService.getValorComoInteger("licencias.dias_aviso_vencimiento")
                .ifPresent(diasAvisoVencimientoField::setValue);
            
            log.info("Configuración cargada exitosamente");
            
        } catch (Exception e) {
            log.error("Error al cargar configuración", e);
            showNotification("Error al cargar configuración: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void saveConfiguration() {
        try {
            log.info("Guardando configuración del sistema");
            
            // Validar campos obligatorios
            if (nombreSistemaField.getValue() == null || nombreSistemaField.getValue().trim().isEmpty()) {
                showNotification("El nombre del sistema es obligatorio", NotificationVariant.LUMO_ERROR);
                return;
            }
            
            if (emailContactoField.getValue() == null || emailContactoField.getValue().trim().isEmpty()) {
                showNotification("El email de contacto es obligatorio", NotificationVariant.LUMO_ERROR);
                return;
            }
            
            // Obtener usuario actual
            String usuarioActual = getCurrentUser();
            
            // Preparar mapa de configuraciones para actualizar
            Map<String, String> configuraciones = new HashMap<>();
            
            // Configuración general
            configuraciones.put("sistema.nombre", nombreSistemaField.getValue());
            configuraciones.put("sistema.url", urlSistemaField.getValue());
            configuraciones.put("contacto.email", emailContactoField.getValue());
            configuraciones.put("contacto.telefono", telefonoContactoField.getValue());
            
            // Configuración de seguridad
            if (maxIntentosFallidosField.getValue() != null) {
                configuraciones.put("seguridad.max_intentos_fallidos", maxIntentosFallidosField.getValue().toString());
            }
            if (tiempoBloqueoField.getValue() != null) {
                configuraciones.put("seguridad.tiempo_bloqueo_minutos", tiempoBloqueoField.getValue().toString());
            }
            if (duracionSesionField.getValue() != null) {
                configuraciones.put("seguridad.duracion_sesion_minutos", duracionSesionField.getValue().toString());
            }
            configuraciones.put("seguridad.cambio_password_obligatorio", 
                               cambioPasswordObligatorioCheckbox.getValue().toString());
            
            // Configuración de licencias
            if (validezLicenciaField.getValue() != null) {
                configuraciones.put("licencias.validez_anos", validezLicenciaField.getValue().toString());
            }
            if (diasAvisoVencimientoField.getValue() != null) {
                configuraciones.put("licencias.dias_aviso_vencimiento", diasAvisoVencimientoField.getValue().toString());
            }
            
            // Actualizar configuraciones
            configuracionService.actualizarConfiguraciones(configuraciones, usuarioActual);
            
            showNotification("Configuración guardada exitosamente", NotificationVariant.LUMO_SUCCESS);
            log.info("Configuración guardada exitosamente por usuario: {}", usuarioActual);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al guardar configuración: {}", e.getMessage());
            showNotification("Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            log.error("Error al guardar configuración", e);
            showNotification("Error al guardar configuración: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void testConfiguration() {
        try {
            log.info("Probando configuración del sistema");
            
            // Validar configuración de email
            String email = emailContactoField.getValue();
            if (email != null && !email.trim().isEmpty()) {
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showNotification("El formato del email no es válido", NotificationVariant.LUMO_ERROR);
                    return;
                }
            }
            
            // Validar URL del sistema
            String url = urlSistemaField.getValue();
            if (url != null && !url.trim().isEmpty()) {
                if (!url.matches("^https?://.*")) {
                    showNotification("La URL debe comenzar con http:// o https://", NotificationVariant.LUMO_ERROR);
                    return;
                }
            }
            
            // Validar configuraciones de seguridad
            if (maxIntentosFallidosField.getValue() != null && maxIntentosFallidosField.getValue() < 1) {
                showNotification("El máximo de intentos fallidos debe ser mayor a 0", NotificationVariant.LUMO_ERROR);
                return;
            }
            
            if (tiempoBloqueoField.getValue() != null && tiempoBloqueoField.getValue() < 5) {
                showNotification("El tiempo de bloqueo debe ser de al menos 5 minutos", NotificationVariant.LUMO_ERROR);
                return;
            }
            
            if (duracionSesionField.getValue() != null && duracionSesionField.getValue() < 15) {
                showNotification("La duración de sesión debe ser de al menos 15 minutos", NotificationVariant.LUMO_ERROR);
                return;
            }
            
            // Validar configuraciones de licencias
            if (validezLicenciaField.getValue() != null && validezLicenciaField.getValue() < 1) {
                showNotification("La validez de licencia debe ser de al menos 1 año", NotificationVariant.LUMO_ERROR);
                return;
            }
            
            if (diasAvisoVencimientoField.getValue() != null && diasAvisoVencimientoField.getValue() < 30) {
                showNotification("Los días de aviso deben ser de al menos 30", NotificationVariant.LUMO_ERROR);
                return;
            }
            
            showNotification("Configuración probada exitosamente", NotificationVariant.LUMO_SUCCESS);
            log.info("Prueba de configuración completada exitosamente");
            
        } catch (Exception e) {
            log.error("Error en la prueba de configuración", e);
            showNotification("Error en la prueba de configuración: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Obtiene el usuario actual autenticado
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "UNKNOWN";
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
