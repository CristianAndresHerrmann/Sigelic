package com.example.sigelic.views.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.example.sigelic.model.ClaseLicencia;
import com.example.sigelic.model.Licencia;
import com.example.sigelic.model.TipoTramite;
import com.example.sigelic.model.Titular;
import com.example.sigelic.service.LicenciaService;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * Diálogo para crear un nuevo trámite
 */
public class NuevoTramiteDialog extends Dialog {

    private final TramiteService tramiteService;
    private final TitularService titularService;
    private final LicenciaService licenciaService;
    private final Consumer<Void> onSuccess;

    private ComboBox<Titular> titularComboBox;
    private ComboBox<TipoTramite> tipoTramiteComboBox;
    private ComboBox<ClaseLicencia> claseLicenciaComboBox;
    private TextArea observacionesField;
    private Span requisitosInfo;

    private Binder<TramiteData> binder;

    public NuevoTramiteDialog(TramiteService tramiteService, TitularService titularService, 
                              LicenciaService licenciaService, Consumer<Void> onSuccess) {
        this.tramiteService = tramiteService;
        this.titularService = titularService;
        this.licenciaService = licenciaService;
        this.onSuccess = onSuccess;

        setHeaderTitle("Nuevo Trámite");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("600px");

        createForm();
        createButtons();
        
        binder = new Binder<>(TramiteData.class);
        bindFields();
    }

    private void createForm() {
        H3 title = new H3("Información del Trámite");
        title.getStyle().set("margin-top", "0");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        // ComboBox para titular
        titularComboBox = new ComboBox<>("Titular");
        titularComboBox.setItemLabelGenerator(titular -> 
            titular.getNombre() + " " + titular.getApellido() + " - DNI: " + titular.getDni());
        titularComboBox.setPlaceholder("Seleccione un titular...");
        titularComboBox.addValueChangeListener(e -> {
            validarTitularSeleccionado();
            actualizarTiposTramiteDisponibles();
        });

        // ComboBox para tipo de trámite
        tipoTramiteComboBox = new ComboBox<>("Tipo de Trámite");
        tipoTramiteComboBox.setItemLabelGenerator(TipoTramite::getDescripcion);
        tipoTramiteComboBox.addValueChangeListener(e -> {
            updateRequisitosInfo();
            actualizarClasesDisponibles();
        });

        // ComboBox para clase de licencia
        claseLicenciaComboBox = new ComboBox<>("Clase de Licencia");
        claseLicenciaComboBox.setItems(ClaseLicencia.values());
        claseLicenciaComboBox.setItemLabelGenerator(clase -> 
            clase.name() + " - " + clase.getDescripcion());
        claseLicenciaComboBox.addValueChangeListener(e -> validarEdadParaClase());

        // Campo de observaciones
        observacionesField = new TextArea("Observaciones");
        observacionesField.setMaxLength(500);
        observacionesField.setPlaceholder("Observaciones adicionales (opcional)...");

        // Información de requisitos
        requisitosInfo = new Span();
        requisitosInfo.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");

        formLayout.add(
                titularComboBox, tipoTramiteComboBox,
                claseLicenciaComboBox, observacionesField
        );
        formLayout.setColspan(observacionesField, 2);

        VerticalLayout content = new VerticalLayout(title, formLayout, requisitosInfo);
        content.setPadding(false);
        content.setSpacing(true);

        add(content);

        loadTitulares();
    }

    private void createButtons() {
        Button cancelButton = new Button("Cancelar", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveButton = new Button("Crear Trámite", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> crearTramite());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setPadding(true);

        getFooter().add(buttonLayout);
    }

    private void bindFields() {
        binder.forField(titularComboBox)
                .asRequired("El titular es obligatorio")
                .bind(TramiteData::getTitular, TramiteData::setTitular);

        binder.forField(tipoTramiteComboBox)
                .asRequired("El tipo de trámite es obligatorio")
                .bind(TramiteData::getTipo, TramiteData::setTipo);

        binder.forField(claseLicenciaComboBox)
                .asRequired("La clase de licencia es obligatoria")
                .bind(TramiteData::getClaseSolicitada, TramiteData::setClaseSolicitada);

        binder.forField(observacionesField)
                .bind(TramiteData::getObservaciones, TramiteData::setObservaciones);
    }

    private void loadTitulares() {
        try {
            List<Titular> titulares = titularService.findAll();
            titularComboBox.setItems(titulares);
        } catch (Exception e) {
            showNotification("Error al cargar titulares: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void validarTitularSeleccionado() {
        Titular titular = titularComboBox.getValue();
        if (titular != null) {
            try {
                // Verificar inhabilitaciones
                boolean puedeIniciar = titularService.puedeIniciarTramite(titular.getId());
                if (!puedeIniciar) {
                    showNotification("El titular tiene inhabilitaciones activas y no puede iniciar trámites", 
                                   NotificationVariant.LUMO_ERROR);
                    titularComboBox.setValue(null);
                    return;
                }

                // Verificar si ya tiene un trámite activo
                if (tramiteService.getTramiteActivo(titular.getId()).isPresent()) {
                    showNotification("El titular ya tiene un trámite en proceso", NotificationVariant.LUMO_WARNING);
                    titularComboBox.setValue(null);
                    return;
                }
                
            } catch (Exception e) {
                showNotification("Error al validar titular: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
                titularComboBox.setValue(null);
            }
        }
    }

    private void updateRequisitosInfo() {
        TipoTramite tipo = tipoTramiteComboBox.getValue();
        if (tipo != null) {
            String requisitos = getRequisitosText(tipo);
            requisitosInfo.setText("Requisitos: " + requisitos);
            requisitosInfo.setVisible(true);
        } else {
            requisitosInfo.setVisible(false);
        }
    }

    private void validarEdadParaClase() {
        Titular titular = titularComboBox.getValue();
        ClaseLicencia clase = claseLicenciaComboBox.getValue();
        
        if (titular != null && clase != null) {
            int edad = titular.getEdad();
            if (edad < clase.getEdadMinima()) {
                showNotification(
                    String.format("El titular (edad: %d años) no cumple con la edad mínima para la clase %s (mínimo: %d años)", 
                                edad, clase.name(), clase.getEdadMinima()), 
                    NotificationVariant.LUMO_ERROR
                );
                claseLicenciaComboBox.setValue(null);
            }
        }
    }

    private String getRequisitosText(TipoTramite tipo) {
        switch (tipo) {
            case EMISION:
                return "Examen teórico, examen práctico y apto médico";
            case RENOVACION:
                return "Apto médico únicamente";
            case DUPLICADO:
                return "Solo documentación (sin exámenes ni apto médico)";
            case CAMBIO_DOMICILIO:
                return "Solo documentación (sin exámenes ni apto médico)";
            default:
                return "Consultar requisitos específicos";
        }
    }

    private void crearTramite() {
        try {
            TramiteData tramiteData = new TramiteData();
            binder.writeBean(tramiteData);

            tramiteService.iniciarTramite(
                    tramiteData.getTitular().getId(),
                    tramiteData.getTipo(),
                    tramiteData.getClaseSolicitada()
            );

            showNotification("Trámite creado exitosamente", NotificationVariant.LUMO_SUCCESS);
            onSuccess.accept(null);
            close();

        } catch (ValidationException e) {
            showNotification("Por favor, complete todos los campos obligatorios", NotificationVariant.LUMO_ERROR);
        } catch (IllegalStateException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("Error al crear trámite: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }

    /**
     * Actualiza los tipos de trámite disponibles según las licencias del titular seleccionado
     */
    private void actualizarTiposTramiteDisponibles() {
        Titular titular = titularComboBox.getValue();
        if (titular == null) {
            tipoTramiteComboBox.setItems();
            tipoTramiteComboBox.setEnabled(false);
            return;
        }

        List<TipoTramite> tiposDisponibles = new ArrayList<>();
        List<Licencia> licenciasDelTitular = licenciaService.findByTitular(titular);

        // EMISION siempre está disponible
        tiposDisponibles.add(TipoTramite.EMISION);

        // RENOVACION, DUPLICADO y CAMBIO_DOMICILIO solo si tiene licencias
        if (!licenciasDelTitular.isEmpty()) {
            // Verificar si tiene licencias vigentes para duplicado y cambio de domicilio
            boolean tieneLicenciasVigentes = licenciasDelTitular.stream()
                    .anyMatch(Licencia::isVigente);

            // Verificar si tiene licencias renovables (vigentes o vencidas hace menos de 2 años)
            boolean tieneLicenciasRenovables = licenciasDelTitular.stream()
                    .anyMatch(licencia -> licencia.getFechaVencimiento().isAfter(
                            java.time.LocalDate.now().minusYears(2)));

            if (tieneLicenciasVigentes) {
                tiposDisponibles.add(TipoTramite.DUPLICADO);
                tiposDisponibles.add(TipoTramite.CAMBIO_DOMICILIO);
            }

            if (tieneLicenciasRenovables) {
                tiposDisponibles.add(TipoTramite.RENOVACION);
            }
        }

        tipoTramiteComboBox.setItems(tiposDisponibles);
        tipoTramiteComboBox.setEnabled(true);
        tipoTramiteComboBox.setValue(null);
        claseLicenciaComboBox.setValue(null);
    }

    /**
     * Actualiza las clases disponibles según el tipo de trámite y las licencias del titular
     */
    private void actualizarClasesDisponibles() {
        Titular titular = titularComboBox.getValue();
        TipoTramite tipo = tipoTramiteComboBox.getValue();
        
        if (titular == null || tipo == null) {
            claseLicenciaComboBox.setItems();
            claseLicenciaComboBox.setEnabled(false);
            return;
        }

        List<ClaseLicencia> clasesDisponibles = new ArrayList<>();
        List<Licencia> licenciasDelTitular = licenciaService.findByTitular(titular);

        switch (tipo) {
            case EMISION:
                // Para emisión, todas las clases están disponibles (excepto las que ya tiene vigentes)
                for (ClaseLicencia clase : ClaseLicencia.values()) {
                    boolean yaTimeLicenciaVigente = licenciasDelTitular.stream()
                            .anyMatch(licencia -> licencia.getClase() == clase && licencia.isVigente());
                    if (!yaTimeLicenciaVigente) {
                        clasesDisponibles.add(clase);
                    }
                }
                break;

            case RENOVACION:
                // Solo las clases que tiene y son renovables
                licenciasDelTitular.stream()
                        .filter(licencia -> licencia.getFechaVencimiento().isAfter(
                                java.time.LocalDate.now().minusYears(2)))
                        .map(Licencia::getClase)
                        .distinct()
                        .forEach(clasesDisponibles::add);
                break;

            case DUPLICADO:
            case CAMBIO_DOMICILIO:
                // Solo las clases vigentes
                licenciasDelTitular.stream()
                        .filter(Licencia::isVigente)
                        .map(Licencia::getClase)
                        .distinct()
                        .forEach(clasesDisponibles::add);
                break;
        }

        claseLicenciaComboBox.setItems(clasesDisponibles);
        claseLicenciaComboBox.setEnabled(!clasesDisponibles.isEmpty());
        claseLicenciaComboBox.setValue(null);

        // Mostrar mensaje si no hay clases disponibles
        if (clasesDisponibles.isEmpty()) {
            String mensaje = switch (tipo) {
                case RENOVACION -> "El titular no tiene licencias renovables";
                case DUPLICADO -> "El titular no tiene licencias vigentes para duplicar";
                case CAMBIO_DOMICILIO -> "El titular no tiene licencias vigentes";
                case EMISION -> "El titular ya tiene licencias vigentes en todas las clases";
                default -> "No hay clases disponibles";
            };
            showNotification(mensaje, NotificationVariant.LUMO_WARNING);
        }
    }

    // Clase interna para el binder
    public static class TramiteData {
        private Titular titular;
        private TipoTramite tipo;
        private ClaseLicencia claseSolicitada;
        private String observaciones;

        // Getters y setters
        public Titular getTitular() { return titular; }
        public void setTitular(Titular titular) { this.titular = titular; }

        public TipoTramite getTipo() { return tipo; }
        public void setTipo(TipoTramite tipo) { this.tipo = tipo; }

        public ClaseLicencia getClaseSolicitada() { return claseSolicitada; }
        public void setClaseSolicitada(ClaseLicencia claseSolicitada) { this.claseSolicitada = claseSolicitada; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }
}
