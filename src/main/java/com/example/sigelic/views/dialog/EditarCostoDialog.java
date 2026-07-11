package com.example.sigelic.views.dialog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

import com.example.sigelic.model.CostoTramite;
import com.example.sigelic.service.CostoTramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

import lombok.extern.slf4j.Slf4j;

/**
 * Diálogo para actualizar el costo vigente de una combinación existente de tipo de trámite y
 * clase de licencia (CU-016/017). Las combinaciones son fijas y ya están parametrizadas; este
 * diálogo no permite crear combinaciones nuevas, solo registrar un nuevo valor para la
 * combinación seleccionada.
 */
@Slf4j
public class EditarCostoDialog extends Dialog {

    private final CostoTramiteService costoTramiteService;
    private final CostoTramite costoActual;
    private final Consumer<Void> onSuccess;

    private BigDecimalField costoField;
    private DatePicker vigenciaDesdePicker;
    private TextField descripcionField;

    public EditarCostoDialog(CostoTramiteService costoTramiteService, CostoTramite costoActual,
                              Consumer<Void> onSuccess) {
        this.costoTramiteService = costoTramiteService;
        this.costoActual = costoActual;
        this.onSuccess = onSuccess;

        setHeaderTitle("Editar Costo de Trámite");
        setModal(true);
        setWidth("520px");
        setResizable(false);

        createForm();
        createButtons();
    }

    private void createForm() {
        TextField tipoField = new TextField("Tipo de Trámite");
        tipoField.setValue(costoActual.getTipoTramite().getDescripcion());
        tipoField.setReadOnly(true);

        TextField claseField = new TextField("Clase de Licencia");
        claseField.setValue(costoActual.getClaseLicencia().name() + " - "
                + costoActual.getClaseLicencia().getDescripcion());
        claseField.setReadOnly(true);

        costoField = new BigDecimalField("Nuevo Costo");
        costoField.setValue(costoActual.getCosto());
        costoField.setPrefixComponent(new Span("$"));
        costoField.setRequiredIndicatorVisible(true);

        vigenciaDesdePicker = new DatePicker("Vigente desde");
        vigenciaDesdePicker.setValue(LocalDate.now());
        vigenciaDesdePicker.setRequiredIndicatorVisible(true);
        vigenciaDesdePicker.setHelperText("Fecha a partir de la cual aplica el nuevo valor");

        descripcionField = new TextField("Descripción");
        descripcionField.setValue(costoActual.getDescripcion() != null ? costoActual.getDescripcion() : "");
        descripcionField.setMaxLength(255);
        descripcionField.setWidthFull();

        FormLayout form = new FormLayout(tipoField, claseField, costoField,
                vigenciaDesdePicker, descripcionField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        form.setColspan(descripcionField, 2);

        add(form);
    }

    private void createButtons() {
        Button cancelButton = new Button("Cancelar", e -> close());

        Button saveButton = new Button("Guardar", e -> guardarCosto());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(new HorizontalLayout(cancelButton, saveButton));
    }

    private void guardarCosto() {
        BigDecimal costo = costoField.getValue();
        LocalDate desde = vigenciaDesdePicker.getValue();

        if (costo == null || desde == null) {
            Notification.show("Complete el nuevo costo y la fecha de vigencia",
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            costoTramiteService.actualizarCosto(costoActual.getTipoTramite(), costoActual.getClaseLicencia(),
                    costo, desde, null, descripcionField.getValue());
            Notification.show("Costo actualizado correctamente", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            log.error("Error al actualizar el costo ID {}", costoActual.getId(), ex);
            Notification.show("Error al actualizar el costo: " + ex.getMessage(),
                    4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
