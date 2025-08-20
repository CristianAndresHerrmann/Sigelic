-- =======================================================================
-- SIGELIC - Sistema Integral de Gestión de Licencias de Conducir
-- Migración V6: Crear tabla de configuración del sistema
-- Fecha: 2025-08-20
-- =======================================================================

-- Tabla configuracion (parámetros del sistema)
CREATE TABLE configuracion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(500) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    tipo ENUM('TEXT', 'INTEGER', 'BOOLEAN', 'EMAIL', 'URL', 'PHONE', 'PASSWORD') NOT NULL DEFAULT 'TEXT',
    modificable BOOLEAN NOT NULL DEFAULT TRUE,
    actualizado_por VARCHAR(100),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_configuracion_clave (clave),
    INDEX idx_configuracion_categoria (categoria),
    INDEX idx_configuracion_modificable (modificable)
);

-- Insertar configuraciones por defecto del sistema
INSERT INTO configuracion (clave, valor, descripcion, categoria, tipo, actualizado_por) VALUES

-- Configuración General
('sistema.nombre', 'SIGELIC - Sistema de Gestión de Licencias', 'Nombre del sistema', 'GENERAL', 'TEXT', 'SYSTEM'),
('sistema.url', 'http://localhost:8080', 'URL base del sistema', 'GENERAL', 'URL', 'SYSTEM'),
('contacto.email', 'contacto@sigelic.gov.ar', 'Email de contacto', 'GENERAL', 'EMAIL', 'SYSTEM'),
('contacto.telefono', '+54 342 4573000', 'Teléfono de contacto', 'GENERAL', 'PHONE', 'SYSTEM'),

-- Configuración de Seguridad
('seguridad.max_intentos_fallidos', '3', 'Máximo de intentos fallidos antes de bloquear cuenta', 'SEGURIDAD', 'INTEGER', 'SYSTEM'),
('seguridad.tiempo_bloqueo_minutos', '30', 'Tiempo de bloqueo en minutos', 'SEGURIDAD', 'INTEGER', 'SYSTEM'),
('seguridad.duracion_sesion_minutos', '60', 'Duración de sesión en minutos', 'SEGURIDAD', 'INTEGER', 'SYSTEM'),
('seguridad.cambio_password_obligatorio', 'true', 'Requerir cambio de contraseña en primer acceso', 'SEGURIDAD', 'BOOLEAN', 'SYSTEM'),

-- Configuración de Licencias
('licencias.validez_anos', '5', 'Años de validez por defecto para nuevas licencias', 'LICENCIAS', 'INTEGER', 'SYSTEM'),
('licencias.dias_aviso_vencimiento', '90', 'Días antes del vencimiento para notificar', 'LICENCIAS', 'INTEGER', 'SYSTEM'),

-- Configuraciones del sistema (no modificables por el usuario)
('sistema.version', '1.0.0', 'Versión del sistema', 'SISTEMA', 'TEXT', 'SYSTEM'),
('sistema.build', '2025-08-20', 'Fecha de build del sistema', 'SISTEMA', 'TEXT', 'SYSTEM');

-- Marcar configuraciones del sistema como no modificables
UPDATE configuracion SET modificable = FALSE WHERE categoria = 'SISTEMA';

-- Registrar en auditoría la creación de la tabla de configuración
INSERT INTO auditoria (entidad, entidad_id, operacion, usuario, fecha, detalles) VALUES
('configuracion', 0, 'CREATE', 'SYSTEM', CURRENT_TIMESTAMP, 'Tabla de configuración creada con valores por defecto');
