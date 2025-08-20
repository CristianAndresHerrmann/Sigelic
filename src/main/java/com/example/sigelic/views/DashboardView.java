package com.example.sigelic.views;

import com.example.sigelic.service.ExamenService;
import com.example.sigelic.service.LicenciaService;
import com.example.sigelic.service.PagoService;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Vista principal del Dashboard
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | SIGELIC")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final AuthenticationContext authContext;
    private final TramiteService tramiteService;
    private final LicenciaService licenciaService;
    private final ExamenService examenService;
    private final PagoService pagoService;

    public DashboardView(AuthenticationContext authContext, 
                        TramiteService tramiteService,
                        LicenciaService licenciaService,
                        ExamenService examenService,
                        PagoService pagoService) {
        this.authContext = authContext;
        this.tramiteService = tramiteService;
        this.licenciaService = licenciaService;
        this.examenService = examenService;
        this.pagoService = pagoService;
        
        addClassName("dashboard-view");
        setSpacing(false);
        setSizeFull();

        createHeader();
        createStatsCards();
        createQuickActions();
    }

    private void createHeader() {
        H2 title = new H2("Dashboard");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        // Mensaje de bienvenida personalizado
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            Span welcomeMessage = new Span("Bienvenido, " + user.getUsername());
            welcomeMessage.addClassNames(LumoUtility.FontSize.LARGE);
            add(welcomeMessage);
        });

        add(title);
    }

    private void createStatsCards() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.addClassName("stats-layout");
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);

        // Calcular estadísticas reales desde la base de datos
        long tramitesActivos = tramiteService.countTramitesActivos();
        long licenciasEmitidas = licenciaService.countLicenciasEmitidas();
        long examenesPendientes = examenService.countExamenesPendientes();
        double pagosDiarios = pagoService.getTotalPagosDiarios();

        // Tarjetas de estadísticas con datos reales
        Div tramitesCard = createStatsCard("Trámites Activos", String.valueOf(tramitesActivos), VaadinIcon.CLIPBOARD_TEXT, "primary");
        Div licenciasCard = createStatsCard("Licencias Emitidas", String.valueOf(licenciasEmitidas), VaadinIcon.CREDIT_CARD, "success");
        Div examenesCard = createStatsCard("Exámenes Pendientes", String.valueOf(examenesPendientes), VaadinIcon.CLIPBOARD_CHECK, "contrast");
        Div pagosCard = createStatsCard("Pagos del Día", String.format("$%.2f", pagosDiarios), VaadinIcon.COIN_PILES, "success");

        statsLayout.add(tramitesCard, licenciasCard, examenesCard, pagosCard);
        add(statsLayout);
    }

    private Div createStatsCard(String title, String value, VaadinIcon iconType, String theme) {
        Div card = new Div();
        card.addClassName("stats-card");
        card.addClassNames(LumoUtility.Padding.LARGE, LumoUtility.BorderRadius.MEDIUM);
        card.getStyle().set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        Icon icon = iconType.create();
        icon.addClassName("stats-icon");
        icon.setSize("2em");
        icon.getStyle().set("color", "var(--lumo-" + theme + "-color)");

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.BOLD);

        VerticalLayout content = new VerticalLayout(icon, titleSpan, valueSpan);
        content.setSpacing(false);
        content.setPadding(false);

        card.add(content);
        return card;
    }

    private void createQuickActions() {
        H2 actionsTitle = new H2("Acciones Rápidas");
        actionsTitle.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.Margin.Bottom.MEDIUM);

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setWidthFull();
        actionsLayout.setSpacing(true);

        // Tarjetas de acciones rápidas basadas en permisos
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAuthority(user, "LICENCIAS_CREAR")) {
                Div newLicenseCard = createActionCard(
                    "Nueva Licencia", 
                    "Iniciar proceso de emisión", 
                    VaadinIcon.PLUS,
                    () -> getUI().ifPresent(ui -> ui.navigate(LicenciasView.class))
                );
                actionsLayout.add(newLicenseCard);
            }

            if (hasAuthority(user, "TRAMITES_LEER")) {
                Div viewTramitesCard = createActionCard(
                    "Ver Trámites", 
                    "Consultar estado de trámites", 
                    VaadinIcon.CLIPBOARD_TEXT,
                    () -> getUI().ifPresent(ui -> ui.navigate(TramitesView.class))
                );
                actionsLayout.add(viewTramitesCard);
            }

            if (hasAuthority(user, "USUARIOS_LEER")) {
                Div usersCard = createActionCard(
                    "Gestionar Usuarios", 
                    "Administrar usuarios del sistema", 
                    VaadinIcon.USERS,
                    () -> getUI().ifPresent(ui -> ui.navigate(UsuariosView.class))
                );
                actionsLayout.add(usersCard);
            }

            if (hasAuthority(user, "REPORTES_GENERAR")) {
                Div reportsCard = createActionCard(
                    "Generar Reportes", 
                    "Crear reportes del sistema", 
                    VaadinIcon.CHART,
                    () -> getUI().ifPresent(ui -> ui.navigate(ReportesView.class))
                );
                actionsLayout.add(reportsCard);
            }
        });

        add(actionsTitle, actionsLayout);
    }

    private Div createActionCard(String title, String description, VaadinIcon iconType, Runnable action) {
        Div card = new Div();
        card.addClassName("action-card");
        card.addClassNames(LumoUtility.Padding.LARGE, LumoUtility.BorderRadius.MEDIUM);
        card.getStyle().set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("cursor", "pointer");

        card.addClickListener(e -> action.run());

        Icon icon = iconType.create();
        icon.setSize("1.5em");
        icon.getStyle().set("color", "var(--lumo-primary-color)");

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.SEMIBOLD);

        Paragraph desc = new Paragraph(description);
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        VerticalLayout content = new VerticalLayout(icon, titleSpan, desc);
        content.setSpacing(false);
        content.setPadding(false);

        card.add(content);
        return card;
    }

    private boolean hasAuthority(UserDetails user, String authority) {
        return user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
