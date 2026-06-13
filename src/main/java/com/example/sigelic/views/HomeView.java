package com.example.sigelic.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Vista de inicio pública (Landing Page) para SIGELIC
 */
@Route("")
@PageTitle("Inicio - Sistema de Licencias | SIGELIC")
@AnonymousAllowed
@StyleSheet("https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Inter:wght@300;400;500;600;700&display=swap")
public class HomeView extends Div implements BeforeEnterObserver {

    private final AuthenticationContext authContext;

    public HomeView(AuthenticationContext authContext) {
        this.authContext = authContext;
        addClassName("landing-page");

        // 1. Header de Navegación
        createHeader();

        // 2. Sección Hero
        createHero();

        // 3. Sección de Pasos (Línea de tiempo para el ciudadano)
        createStepsSection();

        // 4. Sección de Tarjetas de Servicios e Información
        createServicesSection();

        // 5. Footer Municipal
        createFooter();
    }

    private void createHeader() {
        Header header = new Header();
        header.addClassName("landing-header");

        // Logo
        HorizontalLayout logoContainer = new HorizontalLayout();
        logoContainer.addClassName("landing-logo-container");
        logoContainer.setSpacing(true);
        logoContainer.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        Icon logoIcon = VaadinIcon.CAR.create();
        logoIcon.setColor("var(--lumo-primary-color)");
        logoIcon.setSize("1.8rem");

        H1 logoText = new H1("SIGELIC");
        logoText.addClassName("landing-logo-text");

        Span logoBadge = new Span("Santa Fe");
        logoBadge.addClassName("landing-logo-badge");

        logoContainer.add(logoIcon, logoText, logoBadge);

        // Links de Navegación y Botón de Acceso
        HorizontalLayout navContainer = new HorizontalLayout();
        navContainer.addClassName("landing-nav-links");
        navContainer.setSpacing(true);
        navContainer.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        Anchor linksReq = new Anchor("#requisitos", "Requisitos");
        linksReq.addClassName("landing-nav-link");

        Anchor linksPasos = new Anchor("#pasos", "Cómo Tramitar");
        linksPasos.addClassName("landing-nav-link");

        Button loginBtn = new Button("Acceso Agentes", new Icon(VaadinIcon.SIGN_IN));
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));

        navContainer.add(linksReq, linksPasos, loginBtn);

        header.add(logoContainer, navContainer);
        add(header);
    }

    private void createHero() {
        Div heroSection = new Div();
        heroSection.addClassName("landing-hero");

        Div heroContent = new Div();
        heroContent.addClassName("landing-hero-content");

        H1 title = new H1("Gestioná tu Licencia de Conducir de forma digital y simple");
        Paragraph subtitle = new Paragraph(
            "Bienvenido a SIGELIC, el portal oficial del Centro de Emisión de Licencias de Conducir. " +
            "Iniciá tus solicitudes, consultá tus turnos y completá tus evaluaciones teóricas y prácticas desde un solo lugar."
        );

        Div actions = new Div();
        actions.addClassName("landing-hero-actions");

        Button mainCta = new Button("Iniciar Trámite de Licencia", new Icon(VaadinIcon.FILE_ADD));
        mainCta.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        mainCta.getStyle().set("background", "#ffffff");
        mainCta.getStyle().set("color", "var(--lumo-primary-text-color)");
        mainCta.getStyle().set("box-shadow", "0 4px 15px rgba(255,255,255,0.2)");
        mainCta.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));

        Button secondaryCta = new Button("Ver Requisitos", new Icon(VaadinIcon.INFO_CIRCLE));
        secondaryCta.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        secondaryCta.getStyle().set("background", "rgba(255,255,255,0.15)");
        secondaryCta.getStyle().set("border", "1px solid rgba(255,255,255,0.3)");
        secondaryCta.getStyle().set("color", "#ffffff");
        secondaryCta.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().executeJs("document.getElementById('requisitos').scrollIntoView({behavior: 'smooth'});")));

        actions.add(mainCta, secondaryCta);
        heroContent.add(title, subtitle, actions);
        heroSection.add(heroContent);
        add(heroSection);
    }

    private void createStepsSection() {
        Div stepsContainer = new Div();
        stepsContainer.setId("pasos");
        stepsContainer.addClassName("landing-stepper-container");

        H2 sectionTitle = new H2("El camino para obtener tu licencia");
        sectionTitle.addClassName("landing-section-title");

        Paragraph sectionSubtitle = new Paragraph("Completá las 4 etapas obligatorias del Centro de Emisión para obtener tu credencial habilitante de forma ágil.");
        sectionSubtitle.addClassName("landing-section-subtitle");

        Div stepper = new Div();
        stepper.addClassName("landing-stepper");

        // Paso 1
        Div step1 = createStepCard("1", "Turno Online", "Solicitá tu turno en línea para iniciar el legajo de documentación.");
        // Paso 2
        Div step2 = createStepCard("2", "Apto Médico", "Realizá el examen psicofísico con nuestro equipo de profesionales.");
        // Paso 3
        Div step3 = createStepCard("3", "Evaluaciones", "Rendí el examen vial teórico y la prueba práctica de conducción.");
        // Paso 4
        Div step4 = createStepCard("4", "Retiro en el Día", "Aboná las tasas y retirá tu licencia impresa de forma inmediata.");

        stepper.add(step1, step2, step3, step4);
        stepsContainer.add(sectionTitle, sectionSubtitle, stepper);
        add(stepsContainer);
    }

    private Div createStepCard(String number, String title, String description) {
        Div card = new Div();
        card.addClassName("landing-step-card");

        Span num = new Span(number);
        num.addClassName("landing-step-number");

        H3 t = new H3(title);
        t.addClassName("landing-step-title");

        Paragraph d = new Paragraph(description);
        d.addClassName("landing-step-desc");

        card.add(num, t, d);
        return card;
    }

    private void createServicesSection() {
        Div servicesContainer = new Div();
        servicesContainer.setId("requisitos");
        servicesContainer.addClassName("landing-services-container");

        H2 sectionTitle = new H2("Información y Requisitos Clave");
        sectionTitle.addClassName("landing-section-title");

        Paragraph sectionSubtitle = new Paragraph("Consultá los requisitos e información clave de acuerdo al tipo de trámite de conducción que desees realizar.");
        sectionSubtitle.addClassName("landing-section-subtitle");

        servicesContainer.add(sectionTitle, sectionSubtitle);

        Div grid = new Div();
        grid.addClassName("landing-services-grid");

        // Tarjeta 1
        Div card1 = createServiceCard(
            VaadinIcon.CLIPBOARD_USER,
            "Primer Emisión",
            "Para obtener tu primer carnet debés presentar DNI original, constancia de grupo sanguíneo y realizar obligatoriamente los cursos teóricos y prácticos.",
            "primary"
        );

        // Tarjeta 2
        Div card2 = createServiceCard(
            VaadinIcon.REFRESH,
            "Renovación",
            "Presentá tu licencia anterior (vigente o vencida hace menos de 2 años) y DNI. Solo deberás completar el examen psicofísico obligatorio.",
            "success"
        );

        // Tarjeta 3
        Div card3 = createServiceCard(
            VaadinIcon.BAN,
            "Inhabilitaciones",
            "Nuestra oficina cuenta con conexión directa al Registro Nacional de Antecedentes de Tránsito (CENAT) para la validación instantánea del postulante.",
            "warning"
        );

        grid.add(card1, card2, card3);
        servicesContainer.add(grid);
        add(servicesContainer);
    }

    private Div createServiceCard(VaadinIcon iconType, String title, String description, String theme) {
        Div card = new Div();
        card.addClassName("landing-service-card");
        if (theme.equals("success")) {
            card.addClassName("success");
        } else if (theme.equals("warning")) {
            card.addClassName("warning");
        }

        Div iconWrapper = new Div();
        iconWrapper.addClassName("landing-card-icon-wrapper");
        Icon icon = iconType.create();
        icon.setSize("2rem");
        iconWrapper.add(icon);

        H3 t = new H3(title);
        t.addClassName("landing-card-title");

        Paragraph d = new Paragraph(description);
        d.addClassName("landing-card-desc");

        card.add(iconWrapper, t, d);
        return card;
    }

    private void createFooter() {
        Footer footer = new Footer();
        footer.addClassName("landing-footer");

        Div brand = new Div();
        brand.addClassName("landing-footer-brand");
        brand.add("SIGELIC © " + java.time.LocalDate.now().getYear());

        Paragraph info = new Paragraph("Centro Municipal de Emisión de Licencias — Gobierno de la Provincia de Santa Fe");
        Paragraph hours = new Paragraph("Horario de atención: Lunes a Viernes de 7:00 a 13:00 hs.");

        footer.add(brand, info, hours);
        add(footer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Redirigir automáticamente al Dashboard interno si el usuario ya está autenticado.
        if (authContext.isAuthenticated()) {
            event.forwardTo(DashboardView.class);
        }
    }
}
