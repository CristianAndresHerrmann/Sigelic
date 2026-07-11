package com.example.sigelic.views;

import org.springframework.security.core.userdetails.UserDetails;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Layout principal de la aplicación SIGELIC
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private H2 viewTitle;
    private final AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menú");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Avatar y botón de logout
        HorizontalLayout userLayout = createUserLayout();

        HorizontalLayout header = new HorizontalLayout(toggle, viewTitle);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM);

        // Layout completo del header
        HorizontalLayout fullHeader = new HorizontalLayout(header, userLayout);
        fullHeader.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        fullHeader.setWidthFull();
        fullHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        addToNavbar(fullHeader);
    }

    private HorizontalLayout createUserLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);

        // Obtener información del usuario autenticado
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            Avatar avatar = new Avatar(user.getUsername());

            Button logoutButton = new Button("Cerrar Sesión", new Icon(VaadinIcon.SIGN_OUT));
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            logoutButton.addClickListener(e -> authContext.logout());

            layout.add(avatar, logoutButton);
        });

        return layout;
    }

    private void addDrawerContent() {
        H1 appName = new H1("SIGELIC");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // Dashboard principal
        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));

        // Gestión de Usuarios (solo para administradores y supervisores)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR")) {
                nav.addItem(new SideNavItem("Usuarios", UsuariosView.class, VaadinIcon.USERS.create()));
            }
        });

        // Gestión de Titulares (para agentes y superiores)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "AGENTE", "AUDITOR")) {
                nav.addItem(new SideNavItem("Titulares", TitularesView.class, VaadinIcon.USER_CHECK.create()));
            }
        });

        // Gestión de Licencias (para la mayoría de roles)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "AGENTE", "EXAMINADOR", "AUDITOR")) {
                nav.addItem(new SideNavItem("Licencias", LicenciasView.class, VaadinIcon.FILE_TEXT.create()));
            }
        });

        // Gestión de Trámites
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "AGENTE", "MEDICO", "EXAMINADOR", "CAJERO")) {
                nav.addItem(new SideNavItem("Trámites", TramitesView.class, VaadinIcon.CLIPBOARD_TEXT.create()));
            }
        });

        // Gestión de Turnos
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "AGENTE")) {
                nav.addItem(new SideNavItem("Turnos", TurnosView.class, VaadinIcon.CALENDAR.create()));
            }
        });

        // Gestión de Inhabilitaciones
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "AUDITOR")) {
                nav.addItem(new SideNavItem("Inhabilitaciones", InhabilitacionesView.class, VaadinIcon.BAN.create()));
            }
        });

        // Exámenes (solo para médicos y examinadores)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "EXAMINADOR")) {
                nav.addItem(new SideNavItem("Exámenes", ExamenesView.class, VaadinIcon.CLIPBOARD_CHECK.create()));
            }
        });

        // Pagos (solo para cajeros y auditores)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "CAJERO", "AUDITOR")) {
                nav.addItem(new SideNavItem("Pagos", PagosView.class, VaadinIcon.CREDIT_CARD.create()));
            }
        });

        // Reportes (para administradores y auditores)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR", "SUPERVISOR", "AUDITOR")) {
                nav.addItem(new SideNavItem("Reportes", ReportesView.class, VaadinIcon.CHART.create()));
            }
        });

        // Costos de trámites (solo para administradores)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR")) {
                nav.addItem(new SideNavItem("Costos", CostosView.class, VaadinIcon.DOLLAR.create()));
            }
        });

        // Configuración (solo para administradores)
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (hasAnyRole(user, "ADMINISTRADOR")) {
                nav.addItem(new SideNavItem("Configuración", ConfiguracionView.class, VaadinIcon.COG.create()));
            }
        });

        // Cambiar Contraseña
        nav.addItem(new SideNavItem("Cambiar Contraseña", CambiarContrasenaView.class, VaadinIcon.KEY.create()));

        return nav;
    }

    private boolean hasAnyRole(UserDetails user, String... roles) {
        for (String role : roles) {
            if (user.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role))) {
                return true;
            }
        }
        return false;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL);

        layout.add("SIGELIC © " + java.time.LocalDate.now().getYear() + " - Gobierno de Santa Fe");
        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        authContext.getAuthenticatedUser(com.example.sigelic.service.CustomUserDetailsService.CustomUserDetails.class)
                .ifPresent(user -> {
                    if (user.requiereCambioPassword()
                            && !event.getNavigationTarget().equals(CambiarContrasenaView.class)) {
                        event.forwardTo(CambiarContrasenaView.class);
                    }
                });
    }
}
