package com.example.sigelic.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.sigelic.model.Permiso;
import com.example.sigelic.model.RolSistema;
import com.example.sigelic.service.LicenciaService;
import com.example.sigelic.service.PagoService;
import com.example.sigelic.service.TramiteService;
import com.example.sigelic.service.UsuarioService;

class RbacPolicyTest {

    @Test
    void agenteSoloEjecutaLasTransicionesAsignadas() {
        assertPermite(RolSistema.AGENTE, Permiso.TRAMITE_INICIAR, Permiso.TRAMITE_VALIDAR_DOCUMENTACION,
                Permiso.LICENCIA_EMITIR, Permiso.PAGO_ORDEN_GENERAR);
        assertNoPermite(RolSistema.AGENTE, Permiso.PAGO_ACREDITAR, Permiso.PAGO_RECHAZAR,
                Permiso.EXAMEN_REINTENTO_AUTORIZAR);
    }

    @Test
    void rolesEspecialistasNoCruzanResponsabilidades() {
        assertPermite(RolSistema.MEDICO, Permiso.APTO_MEDICO_REGISTRAR, Permiso.APTO_MEDICO_VER);
        assertNoPermite(RolSistema.MEDICO, Permiso.EXAMEN_TEO_REGISTRAR, Permiso.PAGO_ACREDITAR,
                Permiso.LICENCIA_EMITIR);

        assertPermite(RolSistema.EXAMINADOR, Permiso.EXAMEN_TEO_REGISTRAR, Permiso.EXAMEN_PRA_REGISTRAR,
                Permiso.EXAMEN_VER);
        assertNoPermite(RolSistema.EXAMINADOR, Permiso.EXAMEN_REINTENTO_AUTORIZAR,
                Permiso.PAGO_ACREDITAR, Permiso.LICENCIA_EMITIR);

        assertPermite(RolSistema.CAJERO, Permiso.PAGO_ORDEN_GENERAR, Permiso.PAGO_ACREDITAR,
                Permiso.PAGO_RECHAZAR);
        assertNoPermite(RolSistema.CAJERO, Permiso.TRAMITE_VALIDAR_DOCUMENTACION,
                Permiso.EXAMEN_TEO_REGISTRAR, Permiso.LICENCIA_EMITIR);
    }

    @Test
    void supervisorAuditorYAdministradorRespetanLaMatriz() {
        assertPermite(RolSistema.SUPERVISOR, Permiso.TRAMITE_RECHAZAR,
                Permiso.EXAMEN_REINTENTO_AUTORIZAR, Permiso.LICENCIA_GESTIONAR_ESTADO,
                Permiso.TITULAR_GESTIONAR, Permiso.REPORTE_VER);
        assertNoPermite(RolSistema.SUPERVISOR, Permiso.APTO_MEDICO_REGISTRAR,
                Permiso.EXAMEN_TEO_REGISTRAR, Permiso.PAGO_ACREDITAR);

        assertPermite(RolSistema.AUDITOR, Permiso.TRAMITE_VER, Permiso.LICENCIA_VER,
                Permiso.PAGO_VER, Permiso.EXAMEN_VER, Permiso.REPORTE_VER);
        assertNoPermite(RolSistema.AUDITOR, Permiso.TRAMITE_INICIAR, Permiso.LICENCIA_EMITIR,
                Permiso.PAGO_ACREDITAR, Permiso.EXAMEN_TEO_REGISTRAR);

        assertPermite(RolSistema.ADMINISTRADOR, Permiso.SEGURIDAD_GESTIONAR_ROLES,
                Permiso.PROCESO_VENCIMIENTOS_EJECUTAR, Permiso.TRAMITE_RECHAZAR,
                Permiso.EXAMEN_REINTENTO_AUTORIZAR, Permiso.LICENCIA_GESTIONAR_ESTADO);
    }

    @Test
    void serviciosSensiblesExigenLaAuthorityCanonica() {
        assertGuard(TramiteService.class, "iniciarTramite", Authorities.TRAMITE_INICIAR);
        assertGuard(TramiteService.class, "validarDocumentacion", Authorities.TRAMITE_VALIDAR_DOCUMENTACION);
        assertGuard(TramiteService.class, "registrarAptoMedico", Authorities.APTO_MEDICO_REGISTRAR);
        assertGuard(TramiteService.class, "registrarExamenTeorico", Authorities.EXAMEN_TEO_REGISTRAR);
        assertGuard(TramiteService.class, "permitirReintento", Authorities.EXAMEN_REINTENTO_AUTORIZAR);
        assertGuard(TramiteService.class, "emitirLicencia", Authorities.LICENCIA_EMITIR);
        assertGuard(PagoService.class, "acreditarPago", Authorities.PAGO_ACREDITAR);
        assertGuard(PagoService.class, "rechazarPago", Authorities.PAGO_RECHAZAR);
        assertGuard(PagoService.class, "procesarPagosVencidos", Authorities.PROCESO_VENCIMIENTOS_EJECUTAR);
        assertGuard(LicenciaService.class, "suspenderLicencia", Authorities.LICENCIA_GESTIONAR_ESTADO);
        assertGuard(LicenciaService.class, "actualizarLicenciasVencidas", Authorities.PROCESO_VENCIMIENTOS_EJECUTAR);
        assertGuard(UsuarioService.class, "asignarRol", Authorities.SEGURIDAD_GESTIONAR_ROLES);
    }

    private void assertPermite(RolSistema rol, Permiso... permisos) {
        assertThat(rol.getPermisos()).contains(permisos);
    }

    private void assertNoPermite(RolSistema rol, Permiso... permisos) {
        assertThat(rol.getPermisos()).doesNotContain(permisos);
    }

    private void assertGuard(Class<?> serviceClass, String methodName, String authority) {
        assertThat(Arrays.stream(serviceClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .map(method -> method.getAnnotation(PreAuthorize.class))
                .filter(annotation -> annotation != null)
                .map(PreAuthorize::value))
                .anyMatch(expression -> expression.contains(authority));
    }
}
