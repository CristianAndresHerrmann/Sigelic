package com.example.sigelic.views.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.example.sigelic.model.EstadoTramite;
import com.example.sigelic.model.Tramite;
import com.example.sigelic.service.TramiteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Diálogo selector: permite registrar un examen desde la vista de Exámenes.
 * Lista los trámites que están a la espera de un examen (teórico o práctico,
 * incluidos los que permiten reintento) y abre el formulario que corresponde
 * al estado del trámite elegido.
 */
public class NuevoExamenDialog extends Dialog {

    private final TramiteService tramiteService;
    private final Consumer<Void> onSuccess;
    private final ComboBox<Tramite> tramiteCombo;

    public NuevoExamenDialog(TramiteService tramiteService, Consumer<Void> onSuccess) {
        this.tramiteService = tramiteService;
        this.onSuccess = onSuccess;

        setHeaderTitle("Registrar Examen");
        setModal(true);
        setWidth("540px");

        List<Tramite> pendientes = cargarTramitesPendientes();

        tramiteCombo = new ComboBox<>("Trámite pendiente de examen");
        tramiteCombo.setWidthFull();
        tramiteCombo.setItems(pendientes);
        tramiteCombo.setItemLabelGenerator(this::etiquetaTramite);
        tramiteCombo.setPlaceholder("Seleccione un trámite...");

        Span ayuda = new Span("Se listan los trámites a la espera de un examen teórico o práctico, "
                + "incluidos los que fueron desaprobados y permiten reintento.");
        ayuda.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout content = new VerticalLayout(tramiteCombo, ayuda);
        content.setPadding(false);
        content.setSpacing(false);
        add(content);

        Button continuar = new Button("Continuar", e -> continuar());
        continuar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelar = new Button("Cancelar", e -> close());
        getFooter().add(new HorizontalLayout(cancelar, continuar));
    }

    private List<Tramite> cargarTramitesPendientes() {
        List<Tramite> pendientes = new ArrayList<>();

        // findAll() hace JOIN FETCH del titular → los trámites quedan con el titular
        // inicializado y se pueden usar aunque estén desprendidos (sin lazy).
        for (Tramite t : tramiteService.findAll()) {
            EstadoTramite estado = t.getEstado();

            // Pendiente de examen teórico (APTO_MED o reintento tras rechazo)
            boolean teoricoPendiente = t.requiereExamenTeorico()
                    && ((estado == EstadoTramite.APTO_MED && !Boolean.TRUE.equals(t.getExamenTeoricoAprobado()))
                        || estado == EstadoTramite.EX_TEO_RECHAZADO);

            // Pendiente de examen práctico (teórico aprobado o reintento tras rechazo)
            boolean practicoPendiente = t.requiereExamenPractico()
                    && Boolean.TRUE.equals(t.getExamenTeoricoAprobado())
                    && (estado == EstadoTramite.EX_TEO_OK || estado == EstadoTramite.EX_PRA_RECHAZADO);

            if (teoricoPendiente || practicoPendiente) {
                pendientes.add(t);
            }
        }
        return pendientes;
    }

    private String etiquetaTramite(Tramite t) {
        return "T" + String.format("%06d", t.getId()) + " - "
                + t.getTitular().getApellido() + ", " + t.getTitular().getNombre()
                + " - " + (esTeorico(t) ? "Examen teórico" : "Examen práctico");
    }

    private boolean esTeorico(Tramite t) {
        return t.getEstado() == EstadoTramite.APTO_MED || t.getEstado() == EstadoTramite.EX_TEO_RECHAZADO;
    }

    private void continuar() {
        Tramite seleccionado = tramiteCombo.getValue();
        if (seleccionado == null) {
            Notification.show("Seleccione un trámite", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        close();

        // El trámite proviene de findAll() (titular ya inicializado) → seguro para el diálogo
        if (esTeorico(seleccionado)) {
            new RegistrarExamenTeoricoDialog(tramiteService, seleccionado, onSuccess).open();
        } else {
            new RegistrarExamenPracticoDialog(tramiteService, seleccionado, onSuccess).open();
        }
    }
}
