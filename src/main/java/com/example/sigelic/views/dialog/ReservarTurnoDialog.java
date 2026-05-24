package com.example.sigelic.views.dialog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.example.sigelic.model.Recurso;
import com.example.sigelic.model.TipoRecurso;
import com.example.sigelic.model.TipoTurno;
import com.example.sigelic.model.Titular;
import com.example.sigelic.repository.RecursoRepository;
import com.example.sigelic.service.TitularService;
import com.example.sigelic.service.TurnoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import com.vaadin.flow.component.textfield.TextField;

public class ReservarTurnoDialog extends Dialog {

    private final TurnoService turnoService;
    private final TitularService titularService;
    private final RecursoRepository recursoRepository;
    private final Consumer<Void> onSuccess;

    private TextField dniField;
    private Button buscarBtn;
    private Span titularInfoSpan;
    
    private ComboBox<TipoTurno> tipoTurnoComboBox;
    private ComboBox<Recurso> recursoComboBox;
    private DateTimePicker inicioPicker;

    private Titular selectedTitular = null;
    private Button guardarBtn;

    public ReservarTurnoDialog(TurnoService turnoService, TitularService titularService, RecursoRepository recursoRepository, Consumer<Void> onSuccess) {
        this.turnoService = turnoService;
        this.titularService = titularService;
        this.recursoRepository = recursoRepository;
        this.onSuccess = onSuccess;

        setHeaderTitle("Reservar Turno");
        setModal(true);
        setWidth("500px");
        setResizable(false);

        createForm();
        createButtons();
    }

    private void createForm() {
        H3 title = new H3("Reservar Turno");
        title.getStyle().set("margin-top", "0");

        // Sección Titular
        dniField = new TextField("DNI del Titular");
        dniField.setPlaceholder("Escriba el DNI...");
        dniField.setRequiredIndicatorVisible(true);

        buscarBtn = new Button("Buscar", new Icon(VaadinIcon.SEARCH));
        buscarBtn.addClickListener(e -> buscarTitular());

        HorizontalLayout dniLayout = new HorizontalLayout(dniField, buscarBtn);
        dniLayout.setAlignItems(FlexComponent.Alignment.END);
        dniLayout.setWidthFull();

        titularInfoSpan = new Span("Ingrese DNI para buscar titular...");
        titularInfoSpan.getStyle()
                .set("padding", "var(--lumo-space-xs)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Campos del turno
        tipoTurnoComboBox = new ComboBox<>("Tipo de Turno");
        tipoTurnoComboBox.setItems(TipoTurno.values());
        tipoTurnoComboBox.setItemLabelGenerator(TipoTurno::getDescripcion);
        tipoTurnoComboBox.setRequiredIndicatorVisible(true);
        tipoTurnoComboBox.setEnabled(false); // Enable only after finding titular

        recursoComboBox = new ComboBox<>("Recurso / Sala / Box");
        recursoComboBox.setItemLabelGenerator(Recurso::getNombre);
        recursoComboBox.setRequiredIndicatorVisible(true);
        recursoComboBox.setEnabled(false);

        tipoTurnoComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                TipoRecurso tipoRec = mappingTipoTurnoToRecurso(e.getValue());
                List<Recurso> recursos = recursoRepository.findRecursosActivosPorTipo(tipoRec);
                recursoComboBox.setItems(recursos);
                recursoComboBox.setEnabled(true);
            } else {
                recursoComboBox.setItems(List.of());
                recursoComboBox.setEnabled(false);
            }
        });

        inicioPicker = new DateTimePicker("Fecha y Hora de Inicio");
        inicioPicker.setRequiredIndicatorVisible(true);
        inicioPicker.setEnabled(false);
        inicioPicker.setMin(LocalDateTime.now());

        tipoTurnoComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                inicioPicker.setEnabled(true);
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(dniLayout);
        formLayout.add(titularInfoSpan);
        formLayout.add(tipoTurnoComboBox, recursoComboBox, inicioPicker);
        formLayout.setColspan(dniLayout, 2);
        formLayout.setColspan(titularInfoSpan, 2);
        formLayout.setColspan(tipoTurnoComboBox, 2);
        formLayout.setColspan(recursoComboBox, 2);
        formLayout.setColspan(inicioPicker, 2);

        add(formLayout);
    }

    private void createButtons() {
        Button cancelarBtn = new Button("Cancelar", e -> close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        guardarBtn = new Button("Reservar", e -> realizarReserva());
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardarBtn.setEnabled(false);

        HorizontalLayout footer = new HorizontalLayout(cancelarBtn, guardarBtn);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        getFooter().add(footer);
    }

    private void buscarTitular() {
        String dni = dniField.getValue();
        if (dni == null || dni.trim().isEmpty()) {
            showNotification("El DNI es obligatorio", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Optional<Titular> titularOpt = titularService.findByDni(dni.trim());
            if (titularOpt.isPresent()) {
                Titular titular = titularOpt.get();
                selectedTitular = titular;
                boolean inhabilitado = titular.tieneInhabilitacionesActivas();
                
                if (inhabilitado) {
                    titularInfoSpan.setText("Titular: " + titular.getNombre() + " " + titular.getApellido() + " - DNI: " + titular.getDni() + " [INHABILITADO]");
                    titularInfoSpan.getStyle().set("color", "var(--lumo-error-text-color)");
                    tipoTurnoComboBox.setEnabled(false);
                    guardarBtn.setEnabled(false);
                    showNotification("El titular posee inhabilitaciones activas y no puede reservar turnos.", NotificationVariant.LUMO_ERROR);
                } else {
                    titularInfoSpan.setText("Titular: " + titular.getNombre() + " " + titular.getApellido() + " - DNI: " + titular.getDni());
                    titularInfoSpan.getStyle().set("color", "var(--lumo-success-text-color)");
                    tipoTurnoComboBox.setEnabled(true);
                    guardarBtn.setEnabled(true);
                }
            } else {
                selectedTitular = null;
                titularInfoSpan.setText("Titular no encontrado en el sistema.");
                titularInfoSpan.getStyle().set("color", "var(--lumo-error-text-color)");
                tipoTurnoComboBox.setEnabled(false);
                guardarBtn.setEnabled(false);
            }
        } catch (Exception ex) {
            showNotification("Error al buscar titular: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private TipoRecurso mappingTipoTurnoToRecurso(TipoTurno tipo) {
        return switch (tipo) {
            case DOCUMENTACION -> TipoRecurso.BOX;
            case APTO_MEDICO -> TipoRecurso.CONSULTORIO_MEDICO;
            case EXAMEN_TEORICO -> TipoRecurso.AULA_TEORICO;
            case EXAMEN_PRACTICO -> TipoRecurso.PISTA;
            case EMISION -> TipoRecurso.BOX;
        };
    }

    private void realizarReserva() {
        if (selectedTitular == null) {
            showNotification("Debe buscar y seleccionar un titular primero", NotificationVariant.LUMO_ERROR);
            return;
        }
        TipoTurno tipo = tipoTurnoComboBox.getValue();
        Recurso recurso = recursoComboBox.getValue();
        LocalDateTime inicio = inicioPicker.getValue();

        if (tipo == null || recurso == null || inicio == null) {
            showNotification("Todos los campos obligatorios deben completarse", NotificationVariant.LUMO_ERROR);
            return;
        }

        LocalDateTime fin = inicio.plusMinutes(recurso.getDuracionTurnoMinutos());

        try {
            turnoService.reservarTurno(selectedTitular.getId(), tipo, inicio, fin, recurso.getId(), null);
            showNotification("Turno reservado exitosamente", NotificationVariant.LUMO_SUCCESS);
            onSuccess.accept(null);
            close();
        } catch (Exception ex) {
            showNotification("Error al reservar turno: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
