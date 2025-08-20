package com.example.sigelic.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumeración de roles del sistema SIGELIC
 * Cada rol tiene asignado un conjunto específico de permisos siguiendo el principio de mínimo privilegio
 */
public enum RolSistema {
    
    ADMINISTRADOR("ADMINISTRADOR", "Administrador del sistema", Set.of(
        // Seguridad y administración
        Permiso.SEGURIDAD_GESTIONAR_ROLES,
        Permiso.PARAMETROS_EDITAR,
        Permiso.AUDITORIA_VER,
        
        // Titulares
        Permiso.TITULAR_VER,
        Permiso.TITULAR_CREAR,
        Permiso.TITULAR_EDITAR,
        
        // Inhabilitaciones
        Permiso.INHABILITACION_GESTIONAR,
        
        // Trámites
        Permiso.TRAMITE_VER,
        Permiso.TRAMITE_INICIAR,
        Permiso.TRAMITE_AVANZAR,
        Permiso.TRAMITE_RECHAZAR,
        
        // Turnos
        Permiso.TURNO_VER,
        Permiso.TURNO_ASIGNAR,
        Permiso.TURNO_REPROGRAMAR,
        Permiso.TURNO_CANCELAR,
        
        // Exámenes y Apto Médico
        Permiso.EXAMEN_VER,
        Permiso.APTO_MEDICO_VER,
        
        // Pagos
        Permiso.PAGO_VER,
        
        // Licencias
        Permiso.LICENCIA_VER,
        Permiso.LICENCIA_EMITIR,
        Permiso.LICENCIA_REIMPRIMIR_PDF_QR,
        
        // Reportes
        Permiso.REPORTE_VER,
        
        // Overrides
        Permiso.OVERRIDE_TRANSICION_ESTADO,
        Permiso.OVERRIDE_VENCIMIENTO_PAGO
    )),
    
    SUPERVISOR("SUPERVISOR", "Supervisor de operaciones", Set.of(
        // Trámites
        Permiso.TRAMITE_VER,
        Permiso.TRAMITE_RECHAZAR,
        Permiso.TRAMITE_AVANZAR,
        
        // Consultas
        Permiso.TITULAR_VER,
        Permiso.LICENCIA_VER,
        Permiso.EXAMEN_VER,
        Permiso.APTO_MEDICO_VER,
        Permiso.PAGO_VER,
        
        // Turnos
        Permiso.TURNO_VER,
        
        // Reportes y auditoría
        Permiso.REPORTE_VER,
        Permiso.AUDITORIA_VER
        
        // Override limitado se puede agregar según necesidad
        // Permiso.OVERRIDE_TRANSICION_ESTADO
    )),
    
    AGENTE("AGENTE", "Agente de ventanilla", Set.of(
        // Titulares
        Permiso.TITULAR_VER,
        Permiso.TITULAR_CREAR,
        Permiso.TITULAR_EDITAR,
        
        // Trámites
        Permiso.TRAMITE_VER,
        Permiso.TRAMITE_INICIAR,
        Permiso.TRAMITE_AVANZAR,
        
        // Turnos
        Permiso.TURNO_VER,
        Permiso.TURNO_ASIGNAR,
        Permiso.TURNO_REPROGRAMAR,
        Permiso.TURNO_CANCELAR,
        
        // Pagos
        Permiso.PAGO_ORDEN_GENERAR,
        Permiso.PAGO_VER,
        
        // Licencias
        Permiso.LICENCIA_VER,
        Permiso.LICENCIA_EMITIR,
        Permiso.LICENCIA_REIMPRIMIR_PDF_QR
    )),
    
    MEDICO("MEDICO", "Médico evaluador", Set.of(
        // Apto médico
        Permiso.APTO_MEDICO_REGISTRAR,
        Permiso.APTO_MEDICO_VER,
        
        // Consultas necesarias
        Permiso.TRAMITE_VER,
        Permiso.TITULAR_VER
    )),
    
    EXAMINADOR("EXAMINADOR", "Examinador teórico y práctico", Set.of(
        // Exámenes
        Permiso.EXAMEN_TEO_REGISTRAR,
        Permiso.EXAMEN_PRA_REGISTRAR,
        Permiso.EXAMEN_VER,
        
        // Consultas necesarias
        Permiso.TRAMITE_VER,
        Permiso.TITULAR_VER
    )),
    
    CAJERO("CAJERO", "Cajero para pagos", Set.of(
        // Pagos
        Permiso.PAGO_ORDEN_GENERAR,
        Permiso.PAGO_ACREDITAR,
        Permiso.PAGO_VER,
        
        // Consultas necesarias
        Permiso.TRAMITE_VER,
        Permiso.TITULAR_VER
    )),
    
    AUDITOR("AUDITOR", "Auditor del sistema", Set.of(
        // Auditoría y reportes
        Permiso.AUDITORIA_VER,
        Permiso.REPORTE_VER,
        
        // Solo lectura de todos los módulos
        Permiso.TRAMITE_VER,
        Permiso.TITULAR_VER,
        Permiso.PAGO_VER,
        Permiso.LICENCIA_VER,
        Permiso.EXAMEN_VER,
        Permiso.APTO_MEDICO_VER
    )),
    
    CIUDADANO("CIUDADANO", "Ciudadano - Portal externo", Set.of(
        // Solo recursos propios - limitados por contexto de seguridad
        Permiso.TURNO_ASIGNAR,
        Permiso.TURNO_VER,
        Permiso.TRAMITE_VER,
        Permiso.LICENCIA_REIMPRIMIR_PDF_QR
    ));
    
    private final String nombre;
    private final String descripcion;
    private final Set<Permiso> permisos;
    
    RolSistema(String nombre, String descripcion, Set<Permiso> permisos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = permisos;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public Set<Permiso> getPermisos() {
        return permisos;
    }
    
    /**
     * Obtiene las authorities de Spring Security para este rol
     */
    public Set<String> getAuthorities() {
        return permisos.stream()
                .map(Permiso::getAuthority)
                .collect(Collectors.toSet());
    }
    
    /**
     * Verifica si este rol tiene un permiso específico
     */
    public boolean tienePermiso(Permiso permiso) {
        return permisos.contains(permiso);
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
