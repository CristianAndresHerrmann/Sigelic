package com.example.sigelic.views.dialog;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

import com.example.sigelic.model.Pago;
import com.example.sigelic.service.PagoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import lombok.extern.slf4j.Slf4j;

/**
 * Diálogo para rechazar una orden de pago pendiente indicando el motivo (CU-018)
 */
@Slf4j
public class RechazarPagoDialog extends Dialog {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));

    private final PagoService pagoService;
    private final Pago pago;
    private final Consumer<Pago> onSuccess;

    private TextArea motivoField;

    public RechazarPagoDialog(PagoService pagoService, Pago pago, Consumer<Pago> onSuccess) {
        this.pagoService = pagoService;
        this.pago = pago;
        this.onSuccess = onSuccess;

        setHeaderTitle("Rechazar Pago");
        setModal(true);
        setWidth("500px");
        setResizable(false);

        createForm();
        createButtons();
    }

    private void createForm() {
        H3 titulo = new H3("Rechazo de orden de pago");
        titulo.getStyle().set("margin", "0 0 1rem 0");

        TextField numeroField = new TextField("Número de Pago");
        numeroField.setValue("P" + pago.getId());
        numeroField.setReadOnly(true);

        TextField tramiteField = new TextField("Trámite");
        tramiteField.setValue(pago.getTramite() != null
                ? "T" + pago.getTramite().getId() + " - " + pago.getTramite().getTipo().getDescripcion()
                : "N/A");
        tramiteField.setReadOnly(true);

        TextField montoField = new TextField("Monto");
        montoField.setValue(CURRENCY_FORMAT.format(pago.getMonto()));
        montoField.setReadOnly(true);

        motivoField = new TextArea("Motivo del rechazo");
        motivoField.setPlaceholder("Ingrese el motivo del rechazo");
        motivoField.setRequiredIndicatorVisible(true);
        motivoField.setMaxLength(500);
        motivoField.setWidthFull();
        motivoField.setMinHeight("100px");

        FormLayout form = new FormLayout(numeroField, tramiteField, montoField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));

        add(titulo, form, motivoField);
    }

    private void createButtons() {
        Button cancelButton = new Button("Cancelar", e -> close());

        Button rechazarButton = new Button("Rechazar Pago", e -> rechazarPago());
        rechazarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout footer = new HorizontalLayout(cancelButton, rechazarButton);
        getFooter().add(footer);
    }

    private void rechazarPago() {
        String motivo = motivoField.getValue();
        if (motivo == null || motivo.trim().isEmpty()) {
            motivoField.setInvalid(true);
            motivoField.setErrorMessage("Debe ingresar el motivo del rechazo");
            return;
        }

        try {
            Pago pagoRechazado = pagoService.rechazarPago(pago.getId(), motivo.trim());
            Notification.show("El pago P" + pago.getId() + " fue rechazado",
                    3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
            if (onSuccess != null) {
                onSuccess.accept(pagoRechazado);
            }
        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            log.error("Error al rechazar el pago ID {}", pago.getId(), ex);
            Notification.show("Error al rechazar el pago: " + ex.getMessage(),
                    4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
