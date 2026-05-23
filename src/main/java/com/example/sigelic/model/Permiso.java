package com.example.sigelic.model;

import com.example.sigelic.security.Authorities;

/**
 * Enumeración de permisos del sistema SIGELIC
 * Define todas las autoridades (authorities) disponibles para asignar a roles
 */
public enum Permiso {
    
    // Seguridad y Administración
    SEGURIDAD_GESTIONAR_ROLES(Authorities.SEGURIDAD_GESTIONAR_ROLES, "Gestionar roles y usuarios"),
    PARAMETROS_EDITAR(Authorities.PARAMETROS_EDITAR, "Editar parámetros del sistema"),
    AUDITORIA_VER(Authorities.AUDITORIA_VER, "Ver registros de auditoría"),
    
    // Gestión de Titulares
    TITULAR_VER(Authorities.TITULAR_VER, "Ver información de titulares"),
    TITULAR_GESTIONAR(Authorities.TITULAR_GESTIONAR, "Crear, editar y eliminar titulares"),
    
    // Inhabilitaciones
    INHABILITACION_GESTIONAR(Authorities.INHABILITACION_GESTIONAR, "Gestionar inhabilitaciones"),
    
    // Gestión de Trámites
    TRAMITE_VER(Authorities.TRAMITE_VER, "Ver información de trámites"),
    TRAMITE_INICIAR(Authorities.TRAMITE_INICIAR, "Iniciar nuevos trámites"),
    TRAMITE_VALIDAR_DOCUMENTACION(Authorities.TRAMITE_VALIDAR_DOCUMENTACION, "Validar documentación de trámites"),
    TRAMITE_RECHAZAR(Authorities.TRAMITE_RECHAZAR, "Rechazar trámites"),
    
    // Gestión de Turnos
    TURNO_VER(Authorities.TURNO_VER, "Ver información de turnos"),
    TURNO_ASIGNAR(Authorities.TURNO_ASIGNAR, "Asignar nuevos turnos"),
    TURNO_REPROGRAMAR(Authorities.TURNO_REPROGRAMAR, "Reprogramar turnos existentes"),
    TURNO_CANCELAR(Authorities.TURNO_CANCELAR, "Cancelar turnos"),
    
    // Exámenes
    EXAMEN_VER(Authorities.EXAMEN_VER, "Ver resultados de exámenes"),
    EXAMEN_TEO_REGISTRAR(Authorities.EXAMEN_TEO_REGISTRAR, "Registrar exámenes teóricos"),
    EXAMEN_PRA_REGISTRAR(Authorities.EXAMEN_PRA_REGISTRAR, "Registrar exámenes prácticos"),
    EXAMEN_REINTENTO_AUTORIZAR(Authorities.EXAMEN_REINTENTO_AUTORIZAR, "Autorizar reintentos de exámenes"),
    
    // Apto Médico
    APTO_MEDICO_VER(Authorities.APTO_MEDICO_VER, "Ver información de aptos médicos"),
    APTO_MEDICO_REGISTRAR(Authorities.APTO_MEDICO_REGISTRAR, "Registrar aptos médicos"),
    
    // Pagos
    PAGO_VER(Authorities.PAGO_VER, "Ver información de pagos"),
    PAGO_ORDEN_GENERAR(Authorities.PAGO_ORDEN_GENERAR, "Generar órdenes de pago"),
    PAGO_ACREDITAR(Authorities.PAGO_ACREDITAR, "Acreditar pagos"),
    PAGO_RECHAZAR(Authorities.PAGO_RECHAZAR, "Rechazar pagos"),
    
    // Licencias
    LICENCIA_VER(Authorities.LICENCIA_VER, "Ver información de licencias"),
    LICENCIA_EMITIR(Authorities.LICENCIA_EMITIR, "Emitir nuevas licencias"),
    LICENCIA_GESTIONAR_ESTADO(Authorities.LICENCIA_GESTIONAR_ESTADO, "Suspender o inhabilitar licencias"),
    LICENCIA_REIMPRIMIR_PDF_QR(Authorities.LICENCIA_REIMPRIMIR_PDF_QR, "Reimprimir licencias en PDF con QR"),
    
    // Reportes
    REPORTE_VER(Authorities.REPORTE_VER, "Ver reportes del sistema"),
    PROCESO_VENCIMIENTOS_EJECUTAR(Authorities.PROCESO_VENCIMIENTOS_EJECUTAR, "Ejecutar procesos manuales de vencimiento"),
    
    // Overrides especiales
    OVERRIDE_TRANSICION_ESTADO("OVERRIDE_TRANSICION_ESTADO", "Override de transiciones de estado"),
    OVERRIDE_VENCIMIENTO_PAGO("OVERRIDE_VENCIMIENTO_PAGO", "Override de vencimientos de pago");
    
    private final String authority;
    private final String descripcion;
    
    Permiso(String authority, String descripcion) {
        this.authority = authority;
        this.descripcion = descripcion;
    }
    
    public String getAuthority() {
        return authority;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String toString() {
        return authority;
    }
}
