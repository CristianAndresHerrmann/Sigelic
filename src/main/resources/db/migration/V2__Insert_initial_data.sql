-- =======================================================================
-- SIGELIC - Sistema Integral de Gestión de Licencias de Conducir
-- Migración V2: Inserción de datos iniciales y de prueba
-- Fecha: 2025-08-18
-- =======================================================================

-- Insertar recursos del sistema
INSERT INTO recursos (nombre, tipo, capacidad, ubicacion, descripcion, hora_inicio, hora_fin, duracion_turno_minutos, activo) VALUES
-- Aulas para exámenes teóricos
('Aula Teórica 1', 'AULA_TEORICO', 30, 'Planta Baja - Sector A', 'Aula principal para exámenes teóricos', '08:00:00', '18:00:00', 60, TRUE),
('Aula Teórica 2', 'AULA_TEORICO', 25, 'Planta Baja - Sector B', 'Aula secundaria para exámenes teóricos', '08:00:00', '18:00:00', 60, TRUE),
('Aula Teórica 3', 'AULA_TEORICO', 20, 'Primer Piso - Sector A', 'Aula para grupos reducidos', '08:00:00', '16:00:00', 60, TRUE),

-- Boxes para atención al público
('Box Atención 1', 'BOX', 1, 'Planta Baja - Hall Central', 'Box para documentación y trámites', '07:30:00', '19:00:00', 30, TRUE),
('Box Atención 2', 'BOX', 1, 'Planta Baja - Hall Central', 'Box para documentación y trámites', '07:30:00', '19:00:00', 30, TRUE),
('Box Atención 3', 'BOX', 1, 'Planta Baja - Hall Central', 'Box para documentación y trámites', '07:30:00', '19:00:00', 30, TRUE),
('Box Atención 4', 'BOX', 1, 'Planta Baja - Hall Central', 'Box para documentación y trámites', '07:30:00', '19:00:00', 30, TRUE),

-- Consultorios médicos
('Consultorio Médico 1', 'CONSULTORIO_MEDICO', 1, 'Primer Piso - Sector Médico', 'Consultorio principal para exámenes médicos', '08:00:00', '17:00:00', 45, TRUE),
('Consultorio Médico 2', 'CONSULTORIO_MEDICO', 1, 'Primer Piso - Sector Médico', 'Consultorio secundario para exámenes médicos', '08:00:00', '17:00:00', 45, TRUE),
('Consultorio Médico 3', 'CONSULTORIO_MEDICO', 1, 'Primer Piso - Sector Médico', 'Consultorio para casos especiales', '09:00:00', '16:00:00', 45, TRUE),

-- Pistas para exámenes prácticos
('Pista Principal', 'PISTA', 4, 'Sector Externo - Zona A', 'Pista principal para exámenes de conducción', '07:00:00', '18:00:00', 45, TRUE),
('Pista Secundaria', 'PISTA', 3, 'Sector Externo - Zona B', 'Pista para motocicletas y casos especiales', '07:00:00', '18:00:00', 45, TRUE),
('Pista de Maniobras', 'PISTA', 2, 'Sector Externo - Zona C', 'Pista para maniobras específicas', '08:00:00', '17:00:00', 30, TRUE);

-- Insertar costos de trámites
INSERT INTO costos_tramite (tipo_tramite, clase_licencia, costo, fecha_vigencia_desde, fecha_vigencia_hasta, descripcion, activo) VALUES
-- Emisión de licencias
('EMISION', 'A', 25000.00, '2025-01-01', NULL, 'Emisión licencia clase A - Motocicletas hasta 150cc', TRUE),
('EMISION', 'B', 35000.00, '2025-01-01', NULL, 'Emisión licencia clase B - Automóviles particulares', TRUE),
('EMISION', 'C', 45000.00, '2025-01-01', NULL, 'Emisión licencia clase C - Transporte de pasajeros', TRUE),
('EMISION', 'D', 50000.00, '2025-01-01', NULL, 'Emisión licencia clase D - Transporte de carga', TRUE),
('EMISION', 'E', 55000.00, '2025-01-01', NULL, 'Emisión licencia clase E - Transporte especial', TRUE),

-- Renovación de licencias
('RENOVACION', 'A', 18000.00, '2025-01-01', NULL, 'Renovación licencia clase A', TRUE),
('RENOVACION', 'B', 25000.00, '2025-01-01', NULL, 'Renovación licencia clase B', TRUE),
('RENOVACION', 'C', 30000.00, '2025-01-01', NULL, 'Renovación licencia clase C', TRUE),
('RENOVACION', 'D', 35000.00, '2025-01-01', NULL, 'Renovación licencia clase D', TRUE),
('RENOVACION', 'E', 40000.00, '2025-01-01', NULL, 'Renovación licencia clase E', TRUE),

-- Duplicados
('DUPLICADO', 'A', 12000.00, '2025-01-01', NULL, 'Duplicado licencia clase A', TRUE),
('DUPLICADO', 'B', 15000.00, '2025-01-01', NULL, 'Duplicado licencia clase B', TRUE),
('DUPLICADO', 'C', 18000.00, '2025-01-01', NULL, 'Duplicado licencia clase C', TRUE),
('DUPLICADO', 'D', 20000.00, '2025-01-01', NULL, 'Duplicado licencia clase D', TRUE),
('DUPLICADO', 'E', 22000.00, '2025-01-01', NULL, 'Duplicado licencia clase E', TRUE),

-- Cambio de domicilio
('CAMBIO_DOMICILIO', 'A', 8000.00, '2025-01-01', NULL, 'Cambio domicilio licencia clase A', TRUE),
('CAMBIO_DOMICILIO', 'B', 8000.00, '2025-01-01', NULL, 'Cambio domicilio licencia clase B', TRUE),
('CAMBIO_DOMICILIO', 'C', 10000.00, '2025-01-01', NULL, 'Cambio domicilio licencia clase C', TRUE),
('CAMBIO_DOMICILIO', 'D', 10000.00, '2025-01-01', NULL, 'Cambio domicilio licencia clase D', TRUE),
('CAMBIO_DOMICILIO', 'E', 12000.00, '2025-01-01', NULL, 'Cambio domicilio licencia clase E', TRUE);

-- Insertar titulares de prueba
INSERT INTO titulares (dni, nombre, apellido, fecha_nacimiento, domicilio, telefono, email) VALUES
('12345678', 'Juan Carlos', 'González', '1985-03-15', 'Av. San Martín 1234, Santa Fe', '0342-4567890', 'juan.gonzalez@email.com'),
('23456789', 'María Elena', 'Rodríguez', '1990-07-22', 'Bv. Pellegrini 567, Santa Fe', '0342-4567891', 'maria.rodriguez@email.com'),
('34567890', 'Carlos Alberto', 'Martínez', '1978-11-08', 'Calle 25 de Mayo 890, Santa Fe', '0342-4567892', 'carlos.martinez@email.com'),
('45678901', 'Ana Sofía', 'López', '1992-01-30', 'Av. Aristóbulo del Valle 234, Santa Fe', '0342-4567893', 'ana.lopez@email.com'),
('56789012', 'Roberto Daniel', 'Fernández', '1965-09-12', 'Calle Entre Ríos 456, Santa Fe', '0342-4567894', 'roberto.fernandez@email.com'),
('67890123', 'Lucía Beatriz', 'Morales', '1988-05-25', 'Av. Freyre 789, Santa Fe', '0342-4567895', 'lucia.morales@email.com'),
('78901234', 'Miguel Ángel', 'Herrera', '1982-12-03', 'Calle Rivadavia 123, Santa Fe', '0342-4567896', 'miguel.herrera@email.com'),
('89012345', 'Gabriela', 'Castro', '1995-04-18', 'Bv. Gálvez 345, Santa Fe', '0342-4567897', 'gabriela.castro@email.com'),
('90123456', 'Fernando Luis', 'Jiménez', '1970-08-14', 'Av. Blas Parera 678, Santa Fe', '0342-4567898', 'fernando.jimenez@email.com'),
('01234567', 'Valeria', 'Ruiz', '1993-10-27', 'Calle Hipólito Yrigoyen 901, Santa Fe', '0342-4567899', 'valeria.ruiz@email.com');

-- Insertar trámites de prueba
INSERT INTO tramites (titular_id, tipo, clase_solicitada, estado, agente_responsable, observaciones) VALUES
-- Trámites completados
(1, 'EMISION', 'B', 'EMITIDA', 'Agent001', 'Trámite completado sin observaciones'),
(2, 'RENOVACION', 'B', 'EMITIDA', 'Agent002', 'Renovación exitosa'),
(3, 'EMISION', 'C', 'EMITIDA', 'Agent001', 'Licencia profesional emitida'),

-- Trámites en proceso
(4, 'EMISION', 'B', 'APTO_MED', 'Agent003', 'Pendiente examen teórico'),
(5, 'DUPLICADO', 'B', 'PAGO_OK', 'Agent002', 'Documentación en revisión'),
(6, 'EMISION', 'A', 'EX_TEO_OK', 'Agent001', 'Pendiente examen práctico'),

-- Trámites iniciados
(7, 'RENOVACION', 'C', 'INICIADO', 'Agent003', 'Trámite recién iniciado'),
(8, 'EMISION', 'B', 'DOCS_OK', 'Agent002', 'Documentación validada'),
(9, 'CAMBIO_DOMICILIO', 'B', 'PAGO_OK', 'Agent001', 'Cambio de domicilio en proceso'),
(10, 'EMISION', 'D', 'INICIADO', 'Agent003', 'Licencia profesional en trámite');

-- Insertar licencias existentes (para renovaciones y duplicados)
INSERT INTO licencias (titular_id, tramite_id, numero_licencia, clase, estado, fecha_emision, fecha_vencimiento, observaciones) VALUES
(1, 1, 'SF001234567B', 'B', 'VIGENTE', '2025-08-18', '2030-08-18', 'Primera licencia emitida'),
(2, 2, 'SF002345678B', 'B', 'VIGENTE', '2025-08-18', '2030-08-18', 'Licencia renovada'),
(3, 3, 'SF003456789C', 'C', 'VIGENTE', '2025-08-18', '2028-08-18', 'Licencia profesional'),
-- Licencias vencidas para renovación
(5, NULL, 'SF005678901B', 'B', 'VENCIDA', '2020-08-18', '2025-08-18', 'Licencia vencida para duplicado'),
(7, NULL, 'SF007890123C', 'C', 'VENCIDA', '2020-08-18', '2025-08-18', 'Licencia vencida para renovación'),
(9, NULL, 'SF009012345B', 'B', 'VIGENTE', '2023-08-18', '2028-08-18', 'Licencia vigente para cambio domicilio');

-- Insertar pagos
INSERT INTO pagos (tramite_id, monto, medio, estado, fecha, fecha_acreditacion, numero_transaccion, numero_comprobante, cajero) VALUES
(1, 35000.00, 'CAJA', 'ACREDITADO', '2025-08-15 09:00:00', '2025-08-15 09:00:00', NULL, 'COMP001', 'Cajero001'),
(2, 25000.00, 'TRANSFERENCIA', 'ACREDITADO', '2025-08-16 14:30:00', '2025-08-16 15:00:00', 'TRX123456', 'COMP002', NULL),
(3, 45000.00, 'PASARELA_ONLINE', 'ACREDITADO', '2025-08-17 11:15:00', '2025-08-17 11:20:00', 'PAY789012', 'COMP003', NULL),
(5, 15000.00, 'CAJA', 'ACREDITADO', '2025-08-18 08:45:00', '2025-08-18 08:45:00', NULL, 'COMP004', 'Cajero002'),
(9, 8000.00, 'TRANSFERENCIA', 'ACREDITADO', '2025-08-18 10:20:00', '2025-08-18 10:30:00', 'TRX345678', 'COMP005', NULL);

-- Insertar exámenes teóricos
INSERT INTO examenes_teoricos (tramite_id, fecha, examinador, puntaje, cantidad_preguntas, respuestas_correctas, aprobado, observaciones) VALUES
(1, '2025-08-15 10:00:00', 'Prof. García', 85, 40, 34, TRUE, 'Examen aprobado sin dificultades'),
(2, '2025-08-16 15:30:00', 'Prof. Martínez', 78, 40, 31, TRUE, 'Aprobado en segundo intento'),
(3, '2025-08-17 12:00:00', 'Prof. García', 92, 40, 37, TRUE, 'Excelente desempeño'),
(6, '2025-08-18 09:30:00', 'Prof. López', 82, 40, 33, TRUE, 'Aprobado satisfactoriamente');

-- Insertar exámenes prácticos
INSERT INTO examenes_practicos (tramite_id, fecha, examinador, vehiculo_utilizado, pista_utilizada, faltas_leves, faltas_graves, aprobado, observaciones) VALUES
(1, '2025-08-15 15:00:00', 'Instructor Pérez', 'Ford Fiesta 2020', 'Pista Principal', 2, 0, TRUE, 'Conducción segura y responsable'),
(2, '2025-08-16 16:45:00', 'Instructor Silva', 'Chevrolet Onix 2021', 'Pista Principal', 1, 0, TRUE, 'Buen manejo general'),
(3, '2025-08-17 14:30:00', 'Instructor Pérez', 'Mercedes Sprinter 2019', 'Pista Principal', 0, 0, TRUE, 'Excelente para vehículos comerciales');

-- Insertar aptos médicos
INSERT INTO aptos_medicos (tramite_id, fecha, profesional, matricula_profesional, apto, fecha_vencimiento, presion_sistolica, presion_diastolica, agudeza_visual, observaciones) VALUES
(1, '2025-08-15 11:30:00', 'Dr. Rodríguez', 'MP12345', TRUE, '2026-08-15', 120.0, 80.0, '20/20', 'Apto sin restricciones'),
(2, '2025-08-16 13:15:00', 'Dra. Fernández', 'MP23456', TRUE, '2026-08-16', 125.0, 82.0, '20/25', 'Apto con anteojos'),
(3, '2025-08-17 10:45:00', 'Dr. Rodríguez', 'MP12345', TRUE, '2026-08-17', 118.0, 78.0, '20/20', 'Apto para conducción profesional'),
(4, '2025-08-18 12:00:00', 'Dra. González', 'MP34567', TRUE, '2026-08-18', 122.0, 81.0, '20/20', 'Apto sin observaciones');

-- Insertar turnos
INSERT INTO turnos (titular_id, tramite_id, recurso_id, tipo, tipo_recurso, estado, inicio, fin, profesional_asignado, observaciones) VALUES
-- Turnos completados
(1, 1, 4, 'DOCUMENTACION', 'BOX', 'COMPLETADO', '2025-08-15 08:30:00', '2025-08-15 09:00:00', 'Agent001', 'Documentación validada'),
(2, 2, 5, 'DOCUMENTACION', 'BOX', 'COMPLETADO', '2025-08-16 14:00:00', '2025-08-16 14:30:00', 'Agent002', 'Renovación procesada'),

-- Turnos pendientes
(4, 4, 1, 'EXAMEN_TEORICO', 'AULA_TEORICO', 'CONFIRMADO', '2025-08-19 10:00:00', '2025-08-19 11:00:00', 'Prof. García', 'Examen teórico programado'),
(6, 6, 11, 'EXAMEN_PRACTICO', 'PISTA', 'RESERVADO', '2025-08-20 15:00:00', '2025-08-20 15:45:00', 'Instructor Pérez', 'Examen práctico clase A'),
(7, 7, 6, 'DOCUMENTACION', 'BOX', 'RESERVADO', '2025-08-21 09:00:00', '2025-08-21 09:30:00', 'Agent003', 'Validación documentación renovación'),

-- Turnos médicos
(8, 8, 8, 'APTO_MEDICO', 'CONSULTORIO_MEDICO', 'CONFIRMADO', '2025-08-22 11:00:00', '2025-08-22 11:45:00', 'Dr. Rodríguez', 'Examen médico programado'),
(10, 10, 9, 'APTO_MEDICO', 'CONSULTORIO_MEDICO', 'RESERVADO', '2025-08-23 14:00:00', '2025-08-23 14:45:00', 'Dra. Fernández', 'Apto médico licencia D');

-- Insertar algunas inhabilitaciones (casos especiales)
INSERT INTO inhabilitaciones (titular_id, fecha_inicio, fecha_fin, motivo, autoridad, numero_expediente) VALUES
(5, '2024-01-15', '2024-07-15', 'Exceso de velocidad reiterado', 'Juzgado de Faltas Municipal', 'EXP2024001'),
(7, '2023-06-10', '2024-06-10', 'Conducción bajo efectos del alcohol', 'Fiscalía de Tránsito', 'EXP2023045');

-- Insertar registros de auditoría
INSERT INTO auditoria (entidad, entidad_id, operacion, usuario, ip_origen, detalles) VALUES
('titulares', 1, 'INSERT', 'admin', '192.168.1.100', 'Creación nuevo titular'),
('tramites', 1, 'INSERT', 'Agent001', '192.168.1.101', 'Inicio trámite emisión'),
('tramites', 1, 'UPDATE', 'Agent001', '192.168.1.101', 'Cambio estado a EMITIDA'),
('licencias', 1, 'INSERT', 'System', '127.0.0.1', 'Emisión automática licencia'),
('pagos', 1, 'INSERT', 'Cajero001', '192.168.1.102', 'Registro pago en caja'),
('pagos', 1, 'UPDATE', 'Cajero001', '192.168.1.102', 'Pago acreditado');
