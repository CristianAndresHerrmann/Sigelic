package com.example.sigelic.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Vista de Login para SIGELIC
 */
@Route("login")
@PageTitle("Login | SIGELIC")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Configurar el LoginForm para trabajar con Spring Security
        login.setAction("login");
        login.setForgotPasswordButtonVisible(false);

        // Header con logo y título
        H1 title = new H1("SIGELIC");
        title.addClassName("login-title");
        title.getStyle().set("color", "#1976d2");
        
        H2 subtitle = new H2("Sistema Integral de Gestión de Licencias de Conducir");
        subtitle.getStyle().set("color", "#666");
        subtitle.getStyle().set("font-size", "1.2em");
        subtitle.getStyle().set("text-align", "center");
        
        // Información del sistema
        VerticalLayout loginInfo = new VerticalLayout();
        loginInfo.setAlignItems(Alignment.CENTER);
        loginInfo.setSpacing(true);
        loginInfo.setPadding(true);
        loginInfo.getStyle().set("background", "white");
        loginInfo.getStyle().set("border-radius", "8px");
        loginInfo.getStyle().set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");
        loginInfo.add(
            title,
            subtitle,
            login
        );
        loginInfo.setMaxWidth("400px");
        loginInfo.setWidth("100%");

        // Estilo para el fondo
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        
        add(loginInfo);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Informar al usuario sobre errores de autenticación
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}
