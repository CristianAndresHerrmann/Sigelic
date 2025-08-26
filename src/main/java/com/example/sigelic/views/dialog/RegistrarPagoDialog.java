package com.example.sigelic.views.dialog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

public class RegistrarPagoDialog extends Dialog {
    
    private final TramiteService tramiteService;
    private final PagoService pagoService;
    private final Tramite tramite;
    private final Runnable onSuccess;
    
    private NumberField montoField;
    private ComboBox<MedioPago> medioPagoField;
    private ComboBox<EstadoPago> estadoPagoField;
    private TextField numeroComprobanteField;
    private TextField numeroTransaccionField;
    private DatePicker fechaVencimientoField;
    private TextField cajeroField;
    private TextField observacionesField;
    
    private Binder<Pago> binder;
    
    public RegistrarPagoDialog(Tramite tramite, TramiteService tramiteService, PagoService pagoService, Runnable onSuccess) {
        this.tramite = tramite;
        this.tramiteService = tramiteService;
        this.pagoService = pagoService;
        this.onSuccess = onSuccess;
        
        setHeaderTitle("Registrar Pago");
        setModal(true);
        setWidth("600px");
        setResizable(false);
        
        createForm();
        createButtons();
        setupBinder();
    }
    
    private void createForm() {
        H3 titulo = new H3("Registro de Pago - Trámite de Licencia");
        titulo.getStyle().set("margin", "0 0 1rem 0");
        
        // Información del trámite
        TextField solicitanteField = new TextField("Solicitante");
        solicitanteField.setValue(tramite.getTitular().getNombre() + " " + tramite.getTitular().getApellido());
        solicitanteField.setReadOnly(true);
        
        TextField tipoTramiteField = new TextField("Tipo de Trámite");
        tipoTramiteField.setValue(tramite.getTipo().getDescripcion());
        tipoTramiteField.setReadOnly(true);
        
        TextField claseLicenciaField = new TextField("Clase de Licencia");
        claseLicenciaField.setValue(tramite.getClaseSolicitada().getDescripcion());
        claseLicenciaField.setReadOnly(true);
        
        // Campos del pago
        montoField = new NumberField("Monto");
        montoField.setValue(calcularMontoPago());
        montoField.setPrefixComponent(new com.vaadin.flow.component.html.Span("$"));
        montoField.setStep(0.01);
        montoField.setMin(0);
        montoField.setHelperText("Monto en pesos argentinos");
        
        medioPagoField = new ComboBox<>("Medio de Pago");
        medioPagoField.setItems(MedioPago.values());
        medioPagoField.setItemLabelGenerator(MedioPago::getDescripcion);
        medioPagoField.setPlaceholder("Seleccione el medio de pago");
        
        estadoPagoField = new ComboBox<>("Estado del Pago");
        estadoPagoField.setItems(EstadoPago.values());
        estadoPagoField.setItemLabelGenerator(EstadoPago::getDescripcion);
        estadoPagoField.setValue(EstadoPago.ACREDITADO); // Por defecto acreditado
        estadoPagoField.setHelperText("Estado actual del pago");
        
        // Agregar listener para cambiar la UI según el estado seleccionado
        estadoPagoField.addValueChangeListener(event -> {
            EstadoPago estado = event.getValue();
            actualizarCamposSegunEstado(estado);
        });
        
        numeroComprobanteField = new TextField("Número de Comprobante");
        numeroComprobanteField.setPlaceholder("Ej: 001-00012345");
        numeroComprobanteField.setHelperText("Número del comprobante de pago");
        
        // Crear botón para generar comprobante automático
        Button generarComprobanteBtn = new Button("Generar Automático", new Icon(VaadinIcon.MAGIC));
        generarComprobanteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        generarComprobanteBtn.setTooltipText("Generar número de comprobante automáticamente");
        generarComprobanteBtn.addClickListener(e -> generarComprobanteAutomatico());
        
        // Layout para el campo de comprobante con el botón
        HorizontalLayout comprobanteLayout = new HorizontalLayout();
        comprobanteLayout.setAlignItems(FlexComponent.Alignment.END);
        comprobanteLayout.setSpacing(true);
        numeroComprobanteField.setWidthFull();
        comprobanteLayout.add(numeroComprobanteField, generarComprobanteBtn);
        comprobanteLayout.setFlexGrow(1, numeroComprobanteField);
        comprobanteLayout.setFlexGrow(0, generarComprobanteBtn);
        
        numeroTransaccionField = new TextField("Número de Transacción");
        numeroTransaccionField.setPlaceholder("ID de transacción (opcional)");
        numeroTransaccionField.setHelperText("Identificador de la transacción");
        
        fechaVencimientoField = new DatePicker("Fecha de Vencimiento");
        fechaVencimientoField.setValue(LocalDate.now().plusDays(30)); // 30 días por defecto
        fechaVencimientoField.setHelperText("Fecha límite para el pago");
        
        cajeroField = new TextField("Cajero/Operador");
        cajeroField.setPlaceholder("Nombre del cajero u operador");
        cajeroField.setHelperText("Persona que procesó el pago");
        
        observacionesField = new TextField("Observaciones");
        observacionesField.setPlaceholder("Información adicional (opcional)");
        
        FormLayout formLayout = new FormLayout();
        formLayout.add(
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
        formLayout.setColspan(solicitanteField, 2);
        formLayout.setColspan(tipoTramiteField, 2);
        formLayout.setColspan(comprobanteLayout, 2);
        formLayout.setColspan(observacionesField, 2);
        
        add(titulo, formLayout);
    }
    
    private void createButtons() {
        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addClickListener(e -> close());
        
        Button registrarBtn = new Button("Registrar Pago");
        registrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registrarBtn.addClickListener(e -> registrarPago());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelarBtn, registrarBtn);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
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
            
        // Configurar campos iniciales según el estado por defecto
        actualizarCamposSegunEstado(EstadoPago.ACREDITADO);
    }
    
    private double calcularMontoPago() {
        // Este sería el lugar para consultar CostoTramite
        // Por ahora uso valores fijos según la clase de licencia
        return switch (tramite.getClaseSolicitada()) {
            case A -> 15000.0;
            case B -> 12000.0;
            case C -> 18000.0;
            case D -> 20000.0;
            case E -> 25000.0;
        };
    }
    
    private void actualizarCamposSegunEstado(EstadoPago estado) {
        if (estado == null) return;
        
        switch (estado) {
            case ACREDITADO:
                // Para pagos acreditados, el comprobante es obligatorio
                numeroComprobanteField.setRequiredIndicatorVisible(true);
                cajeroField.setRequiredIndicatorVisible(true);
                numeroComprobanteField.setHelperText("Obligatorio para pagos acreditados");
                cajeroField.setHelperText("Operador que procesó el pago");
                fechaVencimientoField.setVisible(false);
                break;
                
            case RECHAZADO:
                // Para pagos rechazados, las observaciones son obligatorias
                numeroComprobanteField.setRequiredIndicatorVisible(false);
                cajeroField.setRequiredIndicatorVisible(false);
                observacionesField.setRequiredIndicatorVisible(true);
                observacionesField.setHelperText("Obligatorio: especificar motivo del rechazo");
                fechaVencimientoField.setVisible(false);
                break;
                
            case PENDIENTE:
                // Para pagos pendientes, la fecha de vencimiento es relevante
                numeroComprobanteField.setRequiredIndicatorVisible(false);
                cajeroField.setRequiredIndicatorVisible(false);
                observacionesField.setRequiredIndicatorVisible(false);
                fechaVencimientoField.setVisible(true);
                fechaVencimientoField.setHelperText("Fecha límite para realizar el pago");
                break;
                
            case VENCIDO:
                // Para pagos vencidos, mostrar información relevante
                numeroComprobanteField.setRequiredIndicatorVisible(false);
                cajeroField.setRequiredIndicatorVisible(false);
                observacionesField.setHelperText("Información adicional sobre el vencimiento");
                fechaVencimientoField.setVisible(true);
                fechaVencimientoField.setHelperText("Fecha en que venció el pago");
                break;
        }
    }
    
    private void registrarPago() {
        Pago pago = new Pago();
        try {
            binder.writeBean(pago);
            
            // Validaciones específicas según el estado
            if (!validarSegunEstado(pago)) {
                return;
            }
            
            // Establecer fechas según el estado
            switch (pago.getEstado()) {
                case ACREDITADO -> pago.setFechaAcreditacion(LocalDateTime.now());
                case RECHAZADO -> {
                    // No establecer fecha de acreditación
                }
                case PENDIENTE -> {
                    // La fecha de vencimiento ya se estableció en el binding
                }
                case VENCIDO -> {
                    // Para pagos marcados como vencidos manualmente
                }
            }
            
            // Registrar el pago
            tramiteService.registrarPago(tramite.getId(), pago);
            
            String mensaje = switch (pago.getEstado()) {
                case ACREDITADO -> "Pago acreditado exitosamente. El trámite puede continuar.";
                case RECHAZADO -> "Pago rechazado registrado. El trámite mantiene su estado actual.";
                case PENDIENTE -> "Orden de pago pendiente creada. Esperando acreditación.";
                case VENCIDO -> "Pago marcado como vencido.";
            };
            
            showNotification(mensaje, NotificationVariant.LUMO_SUCCESS);
            
            if (onSuccess != null) {
                onSuccess.run();
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
                    showNotification("El número de comprobante es obligatorio para pagos acreditados", 
                                   NotificationVariant.LUMO_ERROR);
                    return false;
                }
                if (pago.getCajero() == null || pago.getCajero().trim().isEmpty()) {
                    showNotification("El cajero/operador es obligatorio para pagos acreditados", 
                                   NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;
                
            case RECHAZADO:
                if (pago.getObservaciones() == null || pago.getObservaciones().trim().isEmpty()) {
                    showNotification("Las observaciones son obligatorias para pagos rechazados (especificar motivo)", 
                                   NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;
                
            case PENDIENTE:
                if (pago.getFechaVencimiento() == null) {
                    showNotification("La fecha de vencimiento es obligatoria para pagos pendientes", 
                                   NotificationVariant.LUMO_ERROR);
                    return false;
                }
                if (pago.getFechaVencimiento().isBefore(LocalDateTime.now())) {
                    showNotification("La fecha de vencimiento no puede ser anterior a la fecha actual", 
                                   NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;
                
            case VENCIDO:
                // Para pagos vencidos no hay validaciones específicas
                return true;
                
            default:
                return true;
        }
    }
    
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }

    /**
     * Genera automáticamente un número de comprobante y lo asigna al campo
     */
    private void generarComprobanteAutomatico() {
        try {
            String siguienteComprobante = pagoService.previsualizarSiguienteComprobante();
            numeroComprobanteField.setValue(siguienteComprobante);
            numeroComprobanteField.setHelperText("Comprobante generado automáticamente - No modificar a menos que sea necesario");
            showNotification("Número de comprobante generado: " + siguienteComprobante, NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Error al generar número de comprobante: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }
}
