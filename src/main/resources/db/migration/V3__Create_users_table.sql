-- =======================================================================
-- SIGELIC - Sistema Integral de Gestión de Licencias de Conducir
-- Migración V3: Agregar tabla de usuarios y sistema de roles
-- Fecha: 2025-08-19
-- =======================================================================

-- Tabla usuarios (sistema de autenticación y autorización)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(15),
    dni VARCHAR(20),
    direccion VARCHAR(200),
    rol ENUM('ADMINISTRADOR', 'SUPERVISOR', 'AGENTE', 'MEDICO', 'EXAMINADOR', 'CAJERO', 'AUDITOR', 'CIUDADANO') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    cuenta_bloqueada BOOLEAN NOT NULL DEFAULT FALSE,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    cambio_password_requerido BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso TIMESTAMP NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    actualizado_por VARCHAR(100),
    
    INDEX idx_usuario_username (username),
    INDEX idx_usuario_email (email),
    INDEX idx_usuario_rol (rol),
    INDEX idx_usuario_activo (activo),
    INDEX idx_usuario_dni (dni)
);

-- Insertar usuario administrador inicial
-- Password: admin123 (encriptado con BCrypt, strength 12)
INSERT INTO usuarios (
    username, 
    password, 
    email, 
    nombre, 
    apellido, 
    rol, 
    activo, 
    cuenta_bloqueada, 
    intentos_fallidos, 
    cambio_password_requerido,
    fecha_creacion,
    creado_por
) VALUES (
    'admin',
    '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', -- admin123
    'admin@sigelic.gov.ar',
    'Administrador',
    'Sistema',
    'ADMINISTRADOR',
    TRUE,
    FALSE,
    0,
    TRUE,
    CURRENT_TIMESTAMP,
    'SISTEMA'
);

-- Insertar usuarios de ejemplo para testing
INSERT INTO usuarios (
    username, password, email, nombre, apellido, rol, 
    activo, cuenta_bloqueada, intentos_fallidos, cambio_password_requerido,
    fecha_creacion, creado_por
) VALUES 
-- Supervisor
('supervisor', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'supervisor@sigelic.gov.ar', 'Carlos', 'Supervisor', 'SUPERVISOR', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),

-- Agentes
('agente1', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'agente1@sigelic.gov.ar', 'María', 'Gonzalez', 'AGENTE', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),
('agente2', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'agente2@sigelic.gov.ar', 'Juan', 'Rodriguez', 'AGENTE', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),

-- Médicos
('medico1', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'medico1@sigelic.gov.ar', 'Dr. Pedro', 'Martinez', 'MEDICO', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),
('medico2', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'medico2@sigelic.gov.ar', 'Dra. Ana', 'Lopez', 'MEDICO', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),

-- Examinadores
('examinador1', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'examinador1@sigelic.gov.ar', 'Roberto', 'Fernandez', 'EXAMINADOR', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),
('examinador2', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'examinador2@sigelic.gov.ar', 'Laura', 'Sanchez', 'EXAMINADOR', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),

-- Cajeros
('cajero1', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'cajero1@sigelic.gov.ar', 'Patricia', 'Ruiz', 'CAJERO', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),
('cajero2', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'cajero2@sigelic.gov.ar', 'Miguel', 'Torres', 'CAJERO', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin'),

-- Auditor
('auditor', '$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe', 'auditor@sigelic.gov.ar', 'Elena', 'Auditor', 'AUDITOR', TRUE, FALSE, 0, TRUE, CURRENT_TIMESTAMP, 'admin');

-- Actualizar la tabla de auditoría para registrar la creación de usuarios
INSERT INTO auditoria (entidad, entidad_id, operacion, usuario, fecha, detalles) VALUES
('usuarios', 1, 'INSERT', 'SISTEMA', CURRENT_TIMESTAMP, 'Creación de usuario administrador inicial'),
('usuarios', 2, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario supervisor'),
('usuarios', 3, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario agente1'),
('usuarios', 4, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario agente2'),
('usuarios', 5, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario medico1'),
('usuarios', 6, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario medico2'),
('usuarios', 7, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario examinador1'),
('usuarios', 8, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario examinador2'),
('usuarios', 9, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario cajero1'),
('usuarios', 10, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario cajero2'),
('usuarios', 11, 'INSERT', 'admin', CURRENT_TIMESTAMP, 'Creación de usuario auditor');
