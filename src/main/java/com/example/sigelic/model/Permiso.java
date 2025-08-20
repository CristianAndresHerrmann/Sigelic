package com.example.sigelic.model;

/**
 * Enumeración de permisos del sistema SIGELIC
 * Define todas las autoridades (authorities) disponibles para asignar a roles
 */
public enum Permiso {
    
    // Seguridad y Administración
    SEGURIDAD_GESTIONAR_ROLES("SEGURIDAD_GESTIONAR_ROLES", "Gestionar roles y usuarios"),
    PARAMETROS_EDITAR("PARAMETROS_EDITAR", "Editar parámetros del sistema"),
    AUDITORIA_VER("AUDITORIA_VER", "Ver registros de auditoría"),
    
    // Gestión de Titulares
    TITULAR_VER("TITULAR_VER", "Ver información de titulares"),
    TITULAR_CREAR("TITULAR_CREAR", "Crear nuevos titulares"),
    TITULAR_EDITAR("TITULAR_EDITAR", "Editar información de titulares"),
    
    // Inhabilitaciones
    INHABILITACION_GESTIONAR("INHABILITACION_GESTIONAR", "Gestionar inhabilitaciones"),
    
    // Gestión de Trámites
    TRAMITE_VER("TRAMITE_VER", "Ver información de trámites"),
    TRAMITE_INICIAR("TRAMITE_INICIAR", "Iniciar nuevos trámites"),
    TRAMITE_AVANZAR("TRAMITE_AVANZAR", "Avanzar estados de trámites"),
    TRAMITE_RECHAZAR("TRAMITE_RECHAZAR", "Rechazar trámites"),
    
    // Gestión de Turnos
    TURNO_VER("TURNO_VER", "Ver información de turnos"),
    TURNO_ASIGNAR("TURNO_ASIGNAR", "Asignar nuevos turnos"),
    TURNO_REPROGRAMAR("TURNO_REPROGRAMAR", "Reprogramar turnos existentes"),
    TURNO_CANCELAR("TURNO_CANCELAR", "Cancelar turnos"),
    
    // Exámenes
    EXAMEN_VER("EXAMEN_VER", "Ver resultados de exámenes"),
    EXAMEN_TEO_REGISTRAR("EXAMEN_TEO_REGISTRAR", "Registrar exámenes teóricos"),
    EXAMEN_PRA_REGISTRAR("EXAMEN_PRA_REGISTRAR", "Registrar exámenes prácticos"),
    
    // Apto Médico
    APTO_MEDICO_VER("APTO_MEDICO_VER", "Ver información de aptos médicos"),
    APTO_MEDICO_REGISTRAR("APTO_MEDICO_REGISTRAR", "Registrar aptos médicos"),
    
    // Pagos
    PAGO_VER("PAGO_VER", "Ver información de pagos"),
    PAGO_ORDEN_GENERAR("PAGO_ORDEN_GENERAR", "Generar órdenes de pago"),
    PAGO_ACREDITAR("PAGO_ACREDITAR", "Acreditar pagos"),
    
    // Licencias
    LICENCIA_VER("LICENCIA_VER", "Ver información de licencias"),
    LICENCIA_EMITIR("LICENCIA_EMITIR", "Emitir nuevas licencias"),
    LICENCIA_REIMPRIMIR_PDF_QR("LICENCIA_REIMPRIMIR_PDF_QR", "Reimprimir licencias en PDF con QR"),
    
    // Reportes
    REPORTE_VER("REPORTE_VER", "Ver reportes del sistema"),
    
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
