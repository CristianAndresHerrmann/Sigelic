package com.example.sigelic.views.dialog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import com.example.sigelic.model.EstadoPago;
import com.example.sigelic.model.MedioPago;
import com.example.sigelic.model.Pago;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.PagoService;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class CrearPagoDialog extends Dialog {

    private final TramiteService tramiteService;
    private final PagoService pagoService;
    private final Consumer<Void> onSuccess;

    private ComboBox<Tramite> tramiteComboBox;
    
    private TextField solicitanteField;
    private TextField tipoTramiteField;
    private TextField claseLicenciaField;
    
    private NumberField montoField;
    private ComboBox<MedioPago> medioPagoField;
    private ComboBox<EstadoPago> estadoPagoField;
    private TextField numeroComprobanteField;
    private Button generarComprobanteBtn;
    private TextField numeroTransaccionField;
    private DatePicker fechaVencimientoField;
    private TextField cajeroField;
    private TextField observacionesField;

    private Binder<Pago> binder;
    private Button registrarBtn;

    public CrearPagoDialog(TramiteService tramiteService, PagoService pagoService, Consumer<Void> onSuccess) {
        this.tramiteService = tramiteService;
        this.pagoService = pagoService;
        this.onSuccess = onSuccess;

        setHeaderTitle("Registrar Pago General");
        setModal(true);
        setWidth("600px");
        setResizable(false);

        createForm();
        createButtons();
        setupBinder();
        loadTramites();
    }

    private void createForm() {
        H3 titulo = new H3("Registro de Pago General");
        titulo.getStyle().set("margin", "0 0 1rem 0");

        // Selector de trámite
        tramiteComboBox = new ComboBox<>("Seleccionar Trámite");
        tramiteComboBox.setPlaceholder("Seleccione el trámite a pagar...");
        tramiteComboBox.setItemLabelGenerator(t -> "T" + t.getId() + " - " + t.getTitular().getNombre() + " " + t.getTitular().getApellido() + " (" + t.getTitular().getDni() + ")");
        tramiteComboBox.setWidthFull();
        tramiteComboBox.setRequired(true);
        tramiteComboBox.addValueChangeListener(e -> onTramiteSelected(e.getValue()));

        // Información del trámite seleccionado
        solicitanteField = new TextField("Solicitante");
        solicitanteField.setReadOnly(true);
        
        tipoTramiteField = new TextField("Tipo de Trámite");
        tipoTramiteField.setReadOnly(true);
        
        claseLicenciaField = new TextField("Clase de Licencia");
        claseLicenciaField.setReadOnly(true);

        // Campos del pago (se habilitarán tras seleccionar trámite)
        montoField = new NumberField("Monto");
        montoField.setPrefixComponent(new Span("$"));
        montoField.setStep(0.01);
        montoField.setMin(0);
        montoField.setHelperText("Monto en pesos argentinos");
        montoField.setEnabled(false);
        
        medioPagoField = new ComboBox<>("Medio de Pago");
        medioPagoField.setItems(MedioPago.values());
        medioPagoField.setItemLabelGenerator(MedioPago::getDescripcion);
        medioPagoField.setPlaceholder("Seleccione el medio");
        medioPagoField.setEnabled(false);
        
        estadoPagoField = new ComboBox<>("Estado del Pago");
        estadoPagoField.setItems(EstadoPago.values());
        estadoPagoField.setItemLabelGenerator(EstadoPago::getDescripcion);
        estadoPagoField.setValue(EstadoPago.ACREDITADO);
        estadoPagoField.setEnabled(false);
        
        estadoPagoField.addValueChangeListener(event -> {
            EstadoPago estado = event.getValue();
            actualizarCamposSegunEstado(estado);
        });
        
        numeroComprobanteField = new TextField("Número de Comprobante");
        numeroComprobanteField.setPlaceholder("Ej: 001-00012345");
        numeroComprobanteField.setEnabled(false);
        
        generarComprobanteBtn = new Button("Generar", new Icon(VaadinIcon.MAGIC));
        generarComprobanteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        generarComprobanteBtn.setEnabled(false);
        generarComprobanteBtn.addClickListener(e -> generarComprobanteAutomatico());
        
        HorizontalLayout comprobanteLayout = new HorizontalLayout();
        comprobanteLayout.setAlignItems(FlexComponent.Alignment.END);
        comprobanteLayout.setSpacing(true);
        numeroComprobanteField.setWidthFull();
        comprobanteLayout.add(numeroComprobanteField, generarComprobanteBtn);
        comprobanteLayout.setFlexGrow(1, numeroComprobanteField);
        comprobanteLayout.setFlexGrow(0, generarComprobanteBtn);
        
        numeroTransaccionField = new TextField("Número de Transacción");
        numeroTransaccionField.setPlaceholder("ID transacción (opcional)");
        numeroTransaccionField.setEnabled(false);
        
        fechaVencimientoField = new DatePicker("Fecha de Vencimiento");
        fechaVencimientoField.setValue(LocalDate.now().plusDays(30));
        fechaVencimientoField.setVisible(false);
        fechaVencimientoField.setEnabled(false);
        
        cajeroField = new TextField("Cajero/Operador");
        cajeroField.setPlaceholder("Nombre del cajero");
        cajeroField.setEnabled(false);
        
        observacionesField = new TextField("Observaciones");
        observacionesField.setPlaceholder("Opcional");
        observacionesField.setEnabled(false);
        
        FormLayout formLayout = new FormLayout();
        formLayout.add(
            tramiteComboBox,
            solicitanteField, tipoTramiteField,
            claseLicenciaField, montoField,
            medioPagoField, estadoPagoField,
            comprobanteLayout, numeroTransaccionField,
            fechaVencimientoField, cajeroField,
            observacionesField
        );
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("400px", 2)
        );
        formLayout.setColspan(tramiteComboBox, 2);
        formLayout.setColspan(solicitanteField, 2);
        formLayout.setColspan(tipoTramiteField, 2);
        formLayout.setColspan(comprobanteLayout, 2);
        formLayout.setColspan(observacionesField, 2);
        
        add(titulo, formLayout);
    }

    private void createButtons() {
        Button cancelarBtn = new Button("Cancelar", e -> close());
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        registrarBtn = new Button("Registrar Pago", e -> registrarPago());
        registrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registrarBtn.setEnabled(false);
        
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelarBtn, registrarBtn);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(buttonLayout);
    }

    private void setupBinder() {
        binder = new Binder<>(Pago.class);
        
        binder.forField(montoField)
            .withValidator(monto -> monto != null && monto > 0, "El monto debe ser mayor a 0")
            .withConverter(
                doubleValue -> doubleValue != null ? BigDecimal.valueOf(doubleValue) : null,
                bigDecimalValue -> bigDecimalValue != null ? bigDecimalValue.doubleValue() : null
            )
            .bind(Pago::getMonto, Pago::setMonto);
            
        binder.forField(medioPagoField)
            .withValidator(medio -> medio != null, "Debe seleccionar un medio de pago")
            .bind(Pago::getMedio, Pago::setMedio);
            
        binder.forField(estadoPagoField)
            .withValidator(estado -> estado != null, "Debe seleccionar un estado de pago")
            .bind(Pago::getEstado, Pago::setEstado);
            
        binder.forField(numeroComprobanteField)
            .withValidator(new StringLengthValidator("El número de comprobante debe tener entre 3 y 100 caracteres", 3, 100))
            .bind(Pago::getNumeroComprobante, Pago::setNumeroComprobante);
            
        binder.forField(numeroTransaccionField)
            .withValidator(new StringLengthValidator("El número de transacción no puede exceder 100 caracteres", 0, 100))
            .bind(Pago::getNumeroTransaccion, Pago::setNumeroTransaccion);
            
        binder.forField(fechaVencimientoField)
            .withConverter(
                localDate -> localDate != null ? localDate.atStartOfDay() : null,
                localDateTime -> localDateTime != null ? localDateTime.toLocalDate() : null
            )
            .bind(Pago::getFechaVencimiento, Pago::setFechaVencimiento);
            
        binder.forField(cajeroField)
            .withValidator(new StringLengthValidator("El cajero/operador no puede exceder 100 caracteres", 0, 100))
            .bind(Pago::getCajero, Pago::setCajero);
            
        binder.forField(observacionesField)
            .withValidator(new StringLengthValidator("Las observaciones no pueden exceder 500 caracteres", 0, 500))
            .bind(Pago::getObservaciones, Pago::setObservaciones);
    }

    private void loadTramites() {
        try {
            List<Tramite> tramites = tramiteService.findAll().stream()
                .filter(t -> !t.getEstado().esFinal() && !t.getPagoAcreditado())
                .toList();
            tramiteComboBox.setItems(tramites);
        } catch (Exception e) {
            showNotification("Error al cargar trámites: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void onTramiteSelected(Tramite tramite) {
        if (tramite == null) {
            solicitanteField.clear();
            tipoTramiteField.clear();
            claseLicenciaField.clear();
            
            montoField.clear();
            montoField.setEnabled(false);
            medioPagoField.clear();
            medioPagoField.setEnabled(false);
            estadoPagoField.setEnabled(false);
            numeroComprobanteField.clear();
            numeroComprobanteField.setEnabled(false);
            generarComprobanteBtn.setEnabled(false);
            numeroTransaccionField.clear();
            numeroTransaccionField.setEnabled(false);
            fechaVencimientoField.clear();
            fechaVencimientoField.setEnabled(false);
            cajeroField.clear();
            cajeroField.setEnabled(false);
            observacionesField.clear();
            observacionesField.setEnabled(false);
            registrarBtn.setEnabled(false);
            return;
        }

        solicitanteField.setValue(tramite.getTitular().getNombre() + " " + tramite.getTitular().getApellido());
        tipoTramiteField.setValue(tramite.getTipo().getDescripcion());
        claseLicenciaField.setValue(tramite.getClaseSolicitada().getDescripcion());

        double monto = switch (tramite.getClaseSolicitada()) {
            case A -> 15000.0;
            case B -> 12000.0;
            case C -> 18000.0;
            case D -> 20000.0;
            case E -> 25000.0;
        };

        montoField.setValue(monto);
        montoField.setEnabled(true);
        medioPagoField.setEnabled(true);
        estadoPagoField.setEnabled(true);
        numeroComprobanteField.setEnabled(true);
        generarComprobanteBtn.setEnabled(true);
        numeroTransaccionField.setEnabled(true);
        fechaVencimientoField.setEnabled(true);
        cajeroField.setEnabled(true);
        observacionesField.setEnabled(true);
        registrarBtn.setEnabled(true);

        actualizarCamposSegunEstado(estadoPagoField.getValue());
    }

    private void actualizarCamposSegunEstado(EstadoPago estado) {
        if (estado == null || tramiteComboBox.getValue() == null) return;
        
        switch (estado) {
            case ACREDITADO:
                numeroComprobanteField.setRequiredIndicatorVisible(true);
                cajeroField.setRequiredIndicatorVisible(true);
                fechaVencimientoField.setVisible(false);
                break;
                
            case RECHAZADO:
                numeroComprobanteField.setRequiredIndicatorVisible(false);
                cajeroField.setRequiredIndicatorVisible(false);
                fechaVencimientoField.setVisible(false);
                break;
                
            case PENDIENTE:
                numeroComprobanteField.setRequiredIndicatorVisible(false);
                cajeroField.setRequiredIndicatorVisible(false);
                fechaVencimientoField.setVisible(true);
                break;
                
            case VENCIDO:
                numeroComprobanteField.setRequiredIndicatorVisible(false);
                cajeroField.setRequiredIndicatorVisible(false);
                fechaVencimientoField.setVisible(true);
                break;
        }
    }

    private void registrarPago() {
        Tramite tramite = tramiteComboBox.getValue();
        if (tramite == null) {
            showNotification("Debe seleccionar un trámite", NotificationVariant.LUMO_ERROR);
            return;
        }

        Pago pago = new Pago();
        try {
            binder.writeBean(pago);
            
            if (!validarSegunEstado(pago)) {
                return;
            }
            
            switch (pago.getEstado()) {
                case ACREDITADO -> pago.setFechaAcreditacion(LocalDateTime.now());
                case RECHAZADO -> {}
                case PENDIENTE -> {}
                case VENCIDO -> {}
            }
            
            tramiteService.registrarPago(tramite.getId(), pago);
            
            String mensaje = switch (pago.getEstado()) {
                case ACREDITADO -> "Pago acreditado exitosamente. El trámite puede continuar.";
                case RECHAZADO -> "Pago rechazado registrado.";
                case PENDIENTE -> "Orden de pago pendiente creada.";
                case VENCIDO -> "Pago marcado como vencido.";
            };
            
            showNotification(mensaje, NotificationVariant.LUMO_SUCCESS);
            
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
            
            close();
            
        } catch (ValidationException e) {
            showNotification("Por favor, corrija los errores en el formulario", NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("Error al registrar el pago: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validarSegunEstado(Pago pago) {
        switch (pago.getEstado()) {
            case ACREDITADO:
                if (pago.getNumeroComprobante() == null || pago.getNumeroComprobante().trim().isEmpty()) {
                    showNotification("El número de comprobante es obligatorio para pagos acreditados", NotificationVariant.LUMO_ERROR);
                    return false;
                }
                if (pago.getCajero() == null || pago.getCajero().trim().isEmpty()) {
                    showNotification("El cajero/operador es obligatorio para pagos acreditados", NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;
                
            case RECHAZADO:
                if (pago.getObservaciones() == null || pago.getObservaciones().trim().isEmpty()) {
                    showNotification("Las observaciones son obligatorias para pagos rechazados (especificar motivo)", NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;
                
            case PENDIENTE:
                if (pago.getFechaVencimiento() == null) {
                    showNotification("La fecha de vencimiento es obligatoria para pagos pendientes", NotificationVariant.LUMO_ERROR);
                    return false;
                }
                if (pago.getFechaVencimiento().isBefore(LocalDateTime.now())) {
                    showNotification("La fecha de vencimiento no puede ser anterior a la fecha actual", NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;
                
            case VENCIDO:
                return true;
                
            default:
                return true;
        }
    }

    private void generarComprobanteAutomatico() {
        try {
            String siguienteComprobante = pagoService.previsualizarSiguienteComprobante();
            numeroComprobanteField.setValue(siguienteComprobante);
            showNotification("Número de comprobante generado: " + siguienteComprobante, NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Error al generar número de comprobante: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }
}
