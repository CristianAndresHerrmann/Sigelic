-- =======================================================================
-- SIGELIC - Sistema Integral de Gestión de Licencias de Conducir
-- Migración V1: Creación del esquema inicial
-- Fecha: 2025-08-18
-- =======================================================================

-- Tabla titulares (base para el sistema)
CREATE TABLE titulares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    domicilio VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_titular_dni (dni),
    INDEX idx_titular_apellido (apellido)
);

-- Tabla recursos (instalaciones y equipamiento)
CREATE TABLE recursos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('AULA_TEORICO', 'BOX', 'CONSULTORIO_MEDICO', 'PISTA') NOT NULL,
    capacidad INT NOT NULL CHECK (capacidad >= 1),
    ubicacion VARCHAR(100),
    descripcion VARCHAR(200),
    hora_inicio TIME,
    hora_fin TIME,
    duracion_turno_minutos INT CHECK (duracion_turno_minutos >= 1),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_recurso_tipo (tipo),
    INDEX idx_recurso_activo (activo)
);

-- Tabla tramites (gestión de procesos)
CREATE TABLE tramites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titular_id BIGINT NOT NULL,
    tipo ENUM('EMISION', 'RENOVACION', 'DUPLICADO', 'CAMBIO_DOMICILIO') NOT NULL,
    clase_solicitada ENUM('A', 'B', 'C', 'D', 'E') NOT NULL,
    estado ENUM('INICIADO', 'DOCS_OK', 'PAGO_OK', 'EX_TEO_OK', 'EX_PRA_OK', 'APTO_MED', 'EMITIDA', 'RECHAZADA') NOT NULL DEFAULT 'INICIADO',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    agente_responsable VARCHAR(100),
    observaciones VARCHAR(500),
    documentacion_validada BOOLEAN DEFAULT FALSE,
    pago_acreditado BOOLEAN DEFAULT FALSE,
    examen_teorico_aprobado BOOLEAN DEFAULT FALSE,
    examen_practico_aprobado BOOLEAN DEFAULT FALSE,
    apto_medico_vigente BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (titular_id) REFERENCES titulares(id) ON DELETE RESTRICT,
    INDEX idx_tramite_titular (titular_id),
    INDEX idx_tramite_estado (estado),
    INDEX idx_tramite_tipo (tipo),
    INDEX idx_tramite_fecha (fecha_creacion)
);

-- Tabla costos_tramite (tarifario del sistema)
CREATE TABLE costos_tramite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_tramite ENUM('EMISION', 'RENOVACION', 'DUPLICADO', 'CAMBIO_DOMICILIO') NOT NULL,
    clase_licencia ENUM('A', 'B', 'C', 'D', 'E') NOT NULL,
    costo DECIMAL(12,2) NOT NULL,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    descripcion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_costo_vigencia (fecha_vigencia_desde, fecha_vigencia_hasta),
    INDEX idx_costo_tipo_clase (tipo_tramite, clase_licencia),
    INDEX idx_costo_activo (activo)
);

-- Tabla turnos (gestión de citas)
CREATE TABLE turnos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titular_id BIGINT NOT NULL,
    tramite_id BIGINT,
    recurso_id BIGINT,
    tipo ENUM('DOCUMENTACION', 'EXAMEN_TEORICO', 'EXAMEN_PRACTICO', 'APTO_MEDICO', 'EMISION') NOT NULL,
    tipo_recurso ENUM('AULA_TEORICO', 'BOX', 'CONSULTORIO_MEDICO', 'PISTA'),
    estado ENUM('RESERVADO', 'CONFIRMADO', 'COMPLETADO', 'CANCELADO', 'AUSENTE') NOT NULL DEFAULT 'RESERVADO',
    inicio TIMESTAMP NOT NULL,
    fin TIMESTAMP NOT NULL,
    fecha_reserva TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_confirmacion TIMESTAMP,
    fecha_completion TIMESTAMP,
    profesional_asignado VARCHAR(100),
    observaciones VARCHAR(500),
    
    FOREIGN KEY (titular_id) REFERENCES titulares(id) ON DELETE RESTRICT,
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE SET NULL,
    FOREIGN KEY (recurso_id) REFERENCES recursos(id) ON DELETE SET NULL,
    INDEX idx_turno_titular (titular_id),
    INDEX idx_turno_fecha (inicio),
    INDEX idx_turno_estado (estado),
    INDEX idx_turno_tipo (tipo)
);

-- Tabla pagos (gestión financiera)
CREATE TABLE pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tramite_id BIGINT NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    medio ENUM('CAJA', 'TRANSFERENCIA', 'PASARELA_ONLINE') NOT NULL,
    estado ENUM('PENDIENTE', 'ACREDITADO', 'RECHAZADO', 'VENCIDO') NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento TIMESTAMP,
    fecha_acreditacion TIMESTAMP,
    numero_transaccion VARCHAR(100),
    numero_comprobante VARCHAR(100),
    cajero VARCHAR(100),
    observaciones VARCHAR(500),
    
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE RESTRICT,
    INDEX idx_pago_tramite (tramite_id),
    INDEX idx_pago_estado (estado),
    INDEX idx_pago_fecha (fecha)
);

-- Tabla examenes_teoricos
CREATE TABLE examenes_teoricos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tramite_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    examinador VARCHAR(100),
    puntaje INT NOT NULL CHECK (puntaje >= 0 AND puntaje <= 100),
    cantidad_preguntas INT CHECK (cantidad_preguntas >= 1),
    respuestas_correctas INT CHECK (respuestas_correctas >= 0),
    aprobado BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones VARCHAR(500),
    
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE RESTRICT,
    INDEX idx_examen_teorico_tramite (tramite_id),
    INDEX idx_examen_teorico_fecha (fecha)
);

-- Tabla examenes_practicos
CREATE TABLE examenes_practicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tramite_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    examinador VARCHAR(100),
    vehiculo_utilizado VARCHAR(50),
    pista_utilizada VARCHAR(50),
    faltas_leves INT NOT NULL DEFAULT 0 CHECK (faltas_leves >= 0),
    faltas_graves INT NOT NULL DEFAULT 0 CHECK (faltas_graves >= 0),
    aprobado BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones VARCHAR(500),
    
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE RESTRICT,
    INDEX idx_examen_practico_tramite (tramite_id),
    INDEX idx_examen_practico_fecha (fecha)
);

-- Tabla aptos_medicos
CREATE TABLE aptos_medicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tramite_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    profesional VARCHAR(100) NOT NULL,
    matricula_profesional VARCHAR(50),
    apto BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_vencimiento DATE,
    presion_sistolica DOUBLE,
    presion_diastolica DOUBLE,
    agudeza_visual VARCHAR(50),
    restricciones VARCHAR(200),
    observaciones VARCHAR(500),
    
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE RESTRICT,
    INDEX idx_apto_medico_tramite (tramite_id),
    INDEX idx_apto_medico_fecha (fecha)
);

-- Tabla licencias
CREATE TABLE licencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titular_id BIGINT NOT NULL,
    tramite_id BIGINT,
    numero_licencia VARCHAR(20) UNIQUE,
    clase ENUM('A', 'B', 'C', 'D', 'E') NOT NULL,
    estado ENUM('VIGENTE', 'VENCIDA', 'SUSPENDIDA', 'INHABILITADA', 'DUPLICADA') NOT NULL DEFAULT 'VIGENTE',
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    observaciones VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (titular_id) REFERENCES titulares(id) ON DELETE RESTRICT,
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE SET NULL,
    INDEX idx_licencia_titular (titular_id),
    INDEX idx_licencia_numero (numero_licencia),
    INDEX idx_licencia_estado (estado),
    INDEX idx_licencia_vencimiento (fecha_vencimiento)
);

-- Tabla inhabilitaciones
CREATE TABLE inhabilitaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titular_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    motivo VARCHAR(500) NOT NULL,
    autoridad VARCHAR(100) NOT NULL,
    numero_expediente VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (titular_id) REFERENCES titulares(id) ON DELETE RESTRICT,
    INDEX idx_inhabilitacion_titular (titular_id),
    INDEX idx_inhabilitacion_vigencia (fecha_inicio, fecha_fin)
);

-- Tabla auditoria (log de cambios)
CREATE TABLE auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidad VARCHAR(50) NOT NULL,
    entidad_id BIGINT NOT NULL,
    operacion VARCHAR(20) NOT NULL,
    usuario VARCHAR(100),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(45),
    detalles VARCHAR(200),
    valores_anteriores TEXT,
    valores_nuevos TEXT,
    
    INDEX idx_auditoria_entidad (entidad, entidad_id),
    INDEX idx_auditoria_fecha (fecha),
    INDEX idx_auditoria_usuario (usuario)
);
