package com.example.sigelic.views;

import com.example.sigelic.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@Route(value = "cambiar-contrasena", layout = MainLayout.class)
@PageTitle("Cambiar Contraseña | SIGELIC")
@PermitAll
public class CambiarContrasenaView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final AuthenticationContext authContext;

    private final PasswordField passwordActual = new PasswordField("Contraseña Actual");
    private final PasswordField passwordNueva = new PasswordField("Nueva Contraseña");
    private final PasswordField passwordConfirmacion = new PasswordField("Confirmar Nueva Contraseña");
    private final Button cambiarButton = new Button("Cambiar Contraseña");

    public CambiarContrasenaView(UsuarioService usuarioService, AuthenticationContext authContext) {
        this.usuarioService = usuarioService;
        this.authContext = authContext;
        addClassName("cambiar-contrasena-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Cambiar Contraseña");
        Paragraph subtitle = new Paragraph("Por motivos de seguridad, debe actualizar su contraseña.");

        passwordActual.setRequired(true);
        passwordNueva.setRequired(true);
        passwordConfirmacion.setRequired(true);

        passwordActual.setWidth("300px");
        passwordNueva.setWidth("300px");
        passwordConfirmacion.setWidth("300px");
        cambiarButton.setWidth("300px");
        cambiarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cambiarButton.addClickListener(e -> cambiarPassword());

        VerticalLayout formLayout = new VerticalLayout(title, subtitle, passwordActual, passwordNueva, passwordConfirmacion, cambiarButton);
        formLayout.setAlignItems(Alignment.CENTER);
        formLayout.setSpacing(true);
        formLayout.setPadding(true);
        formLayout.setMaxWidth("400px");
        formLayout.getStyle().set("background", "var(--lumo-base-color)");
        formLayout.getStyle().set("border-radius", "8px");
        formLayout.getStyle().set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        add(formLayout);
    }

    private void cambiarPassword() {
        String actual = passwordActual.getValue();
        String nueva = passwordNueva.getValue();
        String confirmacion = passwordConfirmacion.getValue();

        if (actual.isEmpty() || nueva.isEmpty() || confirmacion.isEmpty()) {
            showError("Todos los campos son obligatorios");
            return;
        }

        if (nueva.length() < 8) {
            showError("La nueva contraseña debe tener al menos 8 caracteres");
            return;
        }

        if (!nueva.equals(confirmacion)) {
            showError("La nueva contraseña y la confirmación no coinciden");
            return;
        }

        try {
            usuarioService.cambiarPasswordPropia(actual, nueva);
            Notification notification = Notification.show("Contraseña cambiada exitosamente. Inicie sesión nuevamente.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            authContext.logout();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
