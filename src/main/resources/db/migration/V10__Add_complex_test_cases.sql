-- =======================================================================
-- SIGELIC - Sistema Integral de Gestión de Licencias de Conducir
-- Migración V9: Casos de prueba complejos y particulares
-- Fecha: 2025-09-25
-- Descripción: Agregar datos de prueba con casos específicos y complejos
-- =======================================================================

-- Insertar titulares adicionales con casos especiales
INSERT INTO titulares (dni, nombre, apellido, fecha_nacimiento, domicilio, telefono, email) VALUES
-- Titulares con casos complejos
('11223344', 'Eduardo Raúl', 'Sánchez', '1955-06-15', 'Av. López y Planes 1455, Santa Fe', '0342-4111222', 'eduardo.sanchez@email.com'),
('22334455', 'Patricia Inés', 'Vega', '1975-11-23', 'Calle San Jerónimo 2567, Santa Fe', '0342-4222333', 'patricia.vega@email.com'),
('33445566', 'Jorge Andrés', 'Molina', '1998-02-14', 'Bv. Pellegrini 3890, Santa Fe', '0342-4333444', 'jorge.molina@email.com'),
('44556677', 'Mónica Graciela', 'Torres', '1963-09-07', 'Av. Freyre 4123, Santa Fe', '0342-4444555', 'monica.torres@email.com'),
('55667788', 'Sebastián Daniel', 'Ríos', '1987-04-18', 'Calle Rivadavia 5678, Santa Fe', '0342-4555666', 'sebastian.rios@email.com'),
('66778899', 'Claudia Marcela', 'Paz', '1991-12-29', 'Av. San Martín 6789, Santa Fe', '0342-4666777', 'claudia.paz@email.com'),
('77889900', 'Nicolás Emilio', 'Silva', '2000-08-03', 'Calle 25 de Mayo 7890, Santa Fe', '0342-4777888', 'nicolas.silva@email.com'),
('88990011', 'Adriana Beatriz', 'Cabrera', '1968-01-16', 'Av. Aristóbulo del Valle 8901, Santa Fe', '0342-4888999', 'adriana.cabrera@email.com'),
('99001122', 'Maximiliano José', 'Domínguez', '1983-07-21', 'Calle Entre Ríos 9012, Santa Fe', '0342-4999000', 'maximiliano.dominguez@email.com'),
('10111213', 'Silvia Rosa', 'Mendoza', '1972-03-10', 'Bv. Gálvez 1234, Santa Fe', '0342-4000111', 'silvia.mendoza@email.com'),
-- Titulares extranjeros (casos especiales)
('95123456', 'Carlos Alberto', 'Mendez', '1980-05-15', 'Av. Pellegrini 567, Santa Fe', '0342-4567123', 'carlos.mendez@email.com'),
('96234567', 'María del Carmen', 'Valdez', '1985-12-08', 'Calle San Martín 890, Santa Fe', '0342-4567234', 'maria.valdez@email.com'),
-- Titulares con antecedentes complejos
('13579135', 'Ramón Alberto', 'Guerrero', '1959-04-12', 'Av. Facundo Zuviría 1357, Santa Fe', '0342-4135792', 'ramon.guerrero@email.com'),
('24681357', 'Estela Marta', 'Romero', '1976-10-25', 'Calle Córdoba 2468, Santa Fe', '0342-4246813', 'estela.romero@email.com'),
('97531864', 'Diego Fernando', 'Acosta', '1992-01-07', 'Av. Blas Parera 9753, Santa Fe', '0342-4975318', 'diego.acosta@email.com');

-- Insertar trámites con casos complejos y particulares
INSERT INTO tramites (titular_id, tipo, clase_solicitada, estado, agente_responsable, observaciones, fecha_creacion) VALUES
-- Casos de trámites con rechazos múltiples
((SELECT id FROM titulares WHERE dni = '11223344'), 'EMISION', 'B', 'EX_TEO_RECHAZADO', 'agente1', 'Tercer intento de examen teórico', '2025-07-15 08:30:00'),
((SELECT id FROM titulares WHERE dni = '22334455'), 'RENOVACION', 'C', 'EX_PRA_RECHAZADO', 'agente2', 'Segundo intento de examen práctico', '2025-08-01 10:15:00'),
((SELECT id FROM titulares WHERE dni = '33445566'), 'EMISION', 'A', 'APTO_MED_RECHAZADO', 'agente1', 'Requiere examen médico especializado', '2025-08-20 14:30:00'),
-- Casos de múltiples licencias
((SELECT id FROM titulares WHERE dni = '44556677'), 'EMISION', 'E', 'EMITIDA', 'agente2', 'Segunda licencia - transporte especial', '2025-06-10 09:45:00'),
((SELECT id FROM titulares WHERE dni = '55667788'), 'EMISION', 'D', 'EX_PRA_OK', 'agente1', 'Tercera licencia - transporte carga', '2025-08-25 11:10:00'),
((SELECT id FROM titulares WHERE dni = '66778899'), 'DUPLICADO', 'B', 'EMITIDA', 'agente2', 'Duplicado por robo de licencia', '2025-09-01 13:40:00'),
-- Casos urgentes y excepcionales
((SELECT id FROM titulares WHERE dni = '77889900'), 'EMISION', 'B', 'DOCS_RECHAZADAS', 'supervisor1', 'Caso urgente - documentación irregular', '2025-09-15 07:30:00'),
((SELECT id FROM titulares WHERE dni = '88990011'), 'RENOVACION', 'A', 'RECHAZADA', 'agente1', 'Inhabilitación vigente no resuelta', '2025-08-28 16:15:00'),
-- Trámites de extranjeros
((SELECT id FROM titulares WHERE dni = '95123456'), 'EMISION', 'B', 'DOCS_OK', 'agente2', 'Convalidación licencia extranjera', '2025-09-18 10:20:00'),
((SELECT id FROM titulares WHERE dni = '96234567'), 'EMISION', 'C', 'PAGO_OK', 'agente1', 'Documentación especial validada', '2025-09-20 14:45:00'),
-- Casos de reactivación
((SELECT id FROM titulares WHERE dni = '13579135'), 'RENOVACION', 'B', 'APTO_MED', 'agente2', 'Reactivación post inhabilitación', '2025-09-22 08:15:00'),
((SELECT id FROM titulares WHERE dni = '24681357'), 'EMISION', 'A', 'EX_TEO_OK', 'agente1', 'Primera licencia post resolución judicial', '2025-09-05 12:30:00'),
-- Casos complejos adicionales
((SELECT id FROM titulares WHERE dni = '99001122'), 'CAMBIO_DOMICILIO', 'C', 'EMITIDA', 'agente2', 'Cambio interprovincial procesado', '2025-08-15 15:50:00'),
((SELECT id FROM titulares WHERE dni = '10111213'), 'DUPLICADO', 'E', 'INICIADO', 'agente1', 'Duplicado por deterioro extremo', '2025-09-23 09:25:00'),
((SELECT id FROM titulares WHERE dni = '97531864'), 'EMISION', 'B', 'DOCS_RECHAZADAS', 'agente2', 'Documentación apócrifa detectada', '2025-09-12 13:10:00');

-- Insertar licencias existentes para casos complejos
INSERT INTO licencias (titular_id, tramite_id, numero_licencia, clase, estado, fecha_emision, fecha_vencimiento, observaciones) VALUES
-- Múltiples licencias para el mismo titular
((SELECT id FROM titulares WHERE dni = '44556677'), NULL, 'SF044556677B', 'B', 'VIGENTE', '2020-06-10', '2025-06-10', 'Primera licencia clase B'),
((SELECT id FROM titulares WHERE dni = '44556677'), NULL, 'SF044556677C', 'C', 'VIGENTE', '2022-03-15', '2027-03-15', 'Segunda licencia clase C'),
((SELECT id FROM titulares WHERE dni = '55667788'), NULL, 'SF055667788A', 'A', 'VIGENTE', '2018-11-20', '2023-11-20', 'Licencia A vencida'),
((SELECT id FROM titulares WHERE dni = '55667788'), NULL, 'SF055667788B', 'B', 'VIGENTE', '2021-07-10', '2026-07-10', 'Licencia B vigente'),
-- Licencias con historiales complejos
((SELECT id FROM titulares WHERE dni = '99001122'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '99001122') AND tipo = 'CAMBIO_DOMICILIO'), 'SF099001122C', 'C', 'VIGENTE', '2025-08-22', '2028-08-22', 'Cambio domicilio aplicado'),
((SELECT id FROM titulares WHERE dni = '66778899'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '66778899') AND tipo = 'DUPLICADO'), 'SF066778899B', 'B', 'VIGENTE', '2025-09-10', '2030-09-10', 'Duplicado por robo'),
-- Licencias de extranjeros
((SELECT id FROM titulares WHERE dni = '13579135'), NULL, 'SF013579135B', 'B', 'SUSPENDIDA', '2019-04-12', '2024-04-12', 'Suspendida por inhabilitación'),
((SELECT id FROM titulares WHERE dni = '44556677'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '44556677') AND clase_solicitada = 'E'), 'SF044556677E', 'E', 'VIGENTE', '2025-07-15', '2027-07-15', 'Licencia especial emitida');

-- Insertar pagos con casos complejos
INSERT INTO pagos (tramite_id, monto, medio, estado, fecha, fecha_acreditacion, numero_transaccion, numero_comprobante, cajero) VALUES
-- Pagos fraccionados y complejos
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '44556677') AND clase_solicitada = 'E'), 27500.00, 'TRANSFERENCIA', 'ACREDITADO', '2025-06-10 10:30:00', '2025-06-10 11:15:00', 'TRX998877', 'COMP101', NULL),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '44556677') AND clase_solicitada = 'E'), 27500.00, 'CAJA', 'ACREDITADO', '2025-06-15 14:20:00', '2025-06-15 14:20:00', NULL, 'COMP102', 'cajero1'),
-- Pagos con diferentes medios
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '66778899') AND tipo = 'DUPLICADO'), 15000.00, 'PASARELA_ONLINE', 'ACREDITADO', '2025-09-01 16:45:00', '2025-09-01 16:50:00', 'PAY445566', 'COMP103', NULL),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '99001122') AND tipo = 'CAMBIO_DOMICILIO'), 10000.00, 'CAJA', 'ACREDITADO', '2025-08-15 11:30:00', '2025-08-15 11:30:00', NULL, 'COMP104', 'cajero2'),
-- Pagos pendientes y vencidos
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '95123456') AND clase_solicitada = 'B'), 35000.00, 'TRANSFERENCIA', 'PENDIENTE', '2025-09-18 12:00:00', NULL, 'TRX556677', 'COMP105', NULL),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '96234567') AND clase_solicitada = 'C'), 45000.00, 'CAJA', 'ACREDITADO', '2025-09-20 09:15:00', '2025-09-20 09:15:00', NULL, 'COMP106', 'cajero1');

-- Insertar exámenes teóricos con casos de reprobación
INSERT INTO examenes_teoricos (tramite_id, fecha, examinador, puntaje, cantidad_preguntas, respuestas_correctas, aprobado, observaciones) VALUES
-- Múltiples intentos fallidos
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '11223344') AND estado = 'EX_TEO_RECHAZADO'), '2025-07-15 10:00:00', 'Prof. García', 45, 40, 18, FALSE, 'Primer intento - insuficiente'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '11223344') AND estado = 'EX_TEO_RECHAZADO'), '2025-08-01 14:30:00', 'Prof. Martínez', 52, 40, 21, FALSE, 'Segundo intento - aún insuficiente'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '11223344') AND estado = 'EX_TEO_RECHAZADO'), '2025-09-15 11:15:00', 'Prof. López', 68, 40, 27, FALSE, 'Tercer intento - mejorando pero insuficiente'),
-- Exámenes exitosos de casos complejos
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '44556677') AND clase_solicitada = 'E'), '2025-06-12 09:45:00', 'Prof. García', 88, 50, 44, TRUE, 'Examen especial clase E aprobado'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '24681357') AND estado = 'EX_TEO_OK'), '2025-09-05 15:20:00', 'Prof. Martínez', 82, 40, 33, TRUE, 'Post resolución judicial - aprobado'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '55667788') AND clase_solicitada = 'D'), '2025-08-26 10:30:00', 'Prof. López', 91, 45, 41, TRUE, 'Examen profesional clase D - excelente');

-- Insertar exámenes prácticos con casos complejos
INSERT INTO examenes_practicos (tramite_id, fecha, examinador, vehiculo_utilizado, pista_utilizada, faltas_leves, faltas_graves, aprobado, observaciones) VALUES
-- Reprobación múltiple
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '22334455') AND estado = 'EX_PRA_RECHAZADO'), '2025-08-05 16:00:00', 'Instructor Silva', 'Mercedes Sprinter 2020', 'Pista Principal', 5, 2, FALSE, 'Primer intento - faltas graves en maniobras'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '22334455') AND estado = 'EX_PRA_RECHAZADO'), '2025-09-10 14:15:00', 'Instructor Pérez', 'Mercedes Sprinter 2019', 'Pista Principal', 3, 1, FALSE, 'Segundo intento - falta grave en estacionamiento'),
-- Exámenes exitosos complejos
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '44556677') AND clase_solicitada = 'E'), '2025-07-01 11:30:00', 'Instructor Silva', 'Volvo FH16 2021', 'Pista Principal', 1, 0, TRUE, 'Vehículo especial - conducción profesional'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '55667788') AND clase_solicitada = 'D'), '2025-09-05 08:45:00', 'Instructor Pérez', 'Scania R450 2020', 'Pista Secundaria', 2, 0, TRUE, 'Transporte de carga - aprobado satisfactoriamente');

-- Insertar aptos médicos con casos complejos
INSERT INTO aptos_medicos (tramite_id, fecha, profesional, matricula_profesional, apto, fecha_vencimiento, presion_sistolica, presion_diastolica, agudeza_visual, observaciones) VALUES
-- Rechazos médicos
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '33445566') AND estado = 'APTO_MED_RECHAZADO'), '2025-08-22 10:15:00', 'Dr. Fernández', 'MP34567', FALSE, NULL, 145.0, 95.0, '20/40', 'Hipertensión no controlada - requiere tratamiento'),
-- Aptos con restricciones
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '44556677') AND clase_solicitada = 'E'), '2025-06-15 14:30:00', 'Dr. Rodríguez', 'MP12345', TRUE, '2026-06-15', 128.0, 84.0, '20/25', 'Apto con anteojos - examen cada 2 años'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '13579135') AND estado = 'APTO_MED'), '2025-09-22 11:45:00', 'Dra. González', 'MP56789', TRUE, '2025-12-22', 132.0, 87.0, '20/30', 'Apto condicional - control en 3 meses'),
-- Aptos normales para casos especiales
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '55667788') AND clase_solicitada = 'D'), '2025-08-28 09:20:00', 'Dr. Rodríguez', 'MP12345', TRUE, '2027-08-28', 118.0, 76.0, '20/20', 'Apto profesional sin restricciones'),
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '24681357') AND estado = 'EX_TEO_OK'), '2025-09-07 13:15:00', 'Dra. Fernández', 'MP23456', TRUE, '2026-09-07', 125.0, 82.0, '20/25', 'Apto post resolución - sin restricciones adicionales');

-- Insertar inhabilitaciones complejas
INSERT INTO inhabilitaciones (titular_id, fecha_inicio, fecha_fin, motivo, autoridad, numero_expediente) VALUES
-- Inhabilitaciones activas
((SELECT id FROM titulares WHERE dni = '33445566'), '2025-03-10', '2025-12-10', 'Conducción temeraria - Inhabilitación por 9 meses', 'Juzgado de Faltas Provincial', 'EXP2025012'),
((SELECT id FROM titulares WHERE dni = '88990011'), '2024-12-15', '2025-12-15', 'Negativa a control de alcoholemia - Un año de inhabilitación', 'Fiscalía de Tránsito', 'EXP2024089'),
-- Inhabilitaciones levantadas
((SELECT id FROM titulares WHERE dni = '13579135'), '2022-04-12', '2024-04-12', 'Conducción bajo efectos de alcohol - Inhabilitación cumplida, apto para reactivación', 'Tribunal de Faltas', 'EXP2022033'),
-- Inhabilitaciones con apelación
((SELECT id FROM titulares WHERE dni = '24681357'), '2023-01-20', '2023-07-20', 'Exceso velocidad extremo - Reducida por apelación exitosa', 'Juzgado de Tránsito', 'EXP2023005');

-- Insertar turnos para casos complejos
INSERT INTO turnos (titular_id, tramite_id, recurso_id, tipo, tipo_recurso, estado, inicio, fin, profesional_asignado, observaciones) VALUES
-- Turnos para casos especiales
((SELECT id FROM titulares WHERE dni = '11223344'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '11223344') AND estado = 'EX_TEO_RECHAZADO'), 2, 'EXAMEN_TEORICO', 'AULA_TEORICO', 'RESERVADO', '2025-10-15 10:00:00', '2025-10-15 11:00:00', 'Prof. García', 'Cuarto intento programado'),
-- Turnos médicos especializados
((SELECT id FROM titulares WHERE dni = '33445566'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '33445566') AND estado = 'APTO_MED_RECHAZADO'), 8, 'APTO_MEDICO', 'CONSULTORIO_MEDICO', 'RESERVADO', '2025-10-25 14:00:00', '2025-10-25 15:00:00', 'Dr. Especialista', 'Control cardiológico especializado'),
-- Turnos de seguimiento
((SELECT id FROM titulares WHERE dni = '13579135'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '13579135') AND estado = 'APTO_MED'), 9, 'DOCUMENTACION', 'BOX', 'CONFIRMADO', '2025-10-02 09:00:00', '2025-10-02 09:30:00', 'supervisor1', 'Revisión documentación reactivación'),
-- Turnos para extranjeros
((SELECT id FROM titulares WHERE dni = '95123456'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '95123456') AND estado = 'DOCS_OK'), 5, 'DOCUMENTACION', 'BOX', 'RESERVADO', '2025-10-10 11:00:00', '2025-10-10 11:45:00', 'agente2', 'Validación documentación extranjera'),
((SELECT id FROM titulares WHERE dni = '22334455'), (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '22334455') AND estado = 'EX_PRA_RECHAZADO'), 11, 'EXAMEN_PRACTICO', 'PISTA', 'RESERVADO', '2025-10-20 15:30:00', '2025-10-20 16:15:00', 'Instructor Silva', 'Tercer intento examen práctico');

-- Insertar auditoría para casos complejos
INSERT INTO auditoria (entidad, entidad_id, operacion, usuario, fecha, ip_origen, detalles) VALUES
('tramites', (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '88990011') AND estado = 'RECHAZADA'), 'UPDATE', 'supervisor1', '2025-09-12 17:30:00', '192.168.1.105', 'Trámite rechazado por inhabilitación vigente'),
('inhabilitaciones', (SELECT id FROM inhabilitaciones WHERE titular_id = (SELECT id FROM titulares WHERE dni = '13579135') AND fecha_fin = '2024-04-12'), 'UPDATE', 'admin', '2024-04-12 16:00:00', '192.168.1.100', 'Inhabilitación levantada - cumplimiento de plazo'),
('licencias', (SELECT id FROM licencias WHERE numero_licencia = 'SF044556677E'), 'INSERT', 'agente2', '2025-07-15 16:20:00', '192.168.1.103', 'Emisión licencia clase E - caso especial'),
('pagos', (SELECT id FROM pagos WHERE numero_comprobante = 'COMP101'), 'INSERT', 'cajero1', '2025-06-10 10:30:00', '192.168.1.104', 'Pago fraccionado 1/2'),
('pagos', (SELECT id FROM pagos WHERE numero_comprobante = 'COMP102'), 'INSERT', 'cajero1', '2025-06-15 14:20:00', '192.168.1.104', 'Pago fraccionado 2/2'),
('titulares', (SELECT id FROM titulares WHERE dni = '95123456'), 'INSERT', 'agente2', '2025-09-18 10:00:00', '192.168.1.103', 'Registro titular extranjero - documentación especial'),
('examenes_teoricos', (SELECT id FROM examenes_teoricos WHERE tramite_id = (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '11223344')) ORDER BY fecha DESC LIMIT 1), 'INSERT', 'Prof. López', '2025-09-15 11:15:00', '192.168.1.106', 'Tercer intento examen teórico - reprobado'),
('examenes_practicos', (SELECT id FROM examenes_practicos WHERE tramite_id = (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '22334455')) ORDER BY fecha DESC LIMIT 1), 'INSERT', 'Instructor Pérez', '2025-09-10 14:15:00', '192.168.1.107', 'Segundo intento examen práctico - reprobado');