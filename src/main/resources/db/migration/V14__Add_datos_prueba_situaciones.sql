-- Migración V14: Datos de prueba para cubrir todas las situaciones del dominio
-- Completa los huecos del seed (V2/V10): licencias INHABILITADA/DUPLICADA, pagos RECHAZADO/VENCIDO,
-- turnos CANCELADO/AUSENTE y futuros, inhabilitaciones activas, duplicados con motivo,
-- titulares en edades límite, aptos médicos por vencer/vencidos y costo histórico fuera de vigencia.
-- Usa fechas relativas (CURDATE()/NOW()) para que los escenarios sigan vigentes en el tiempo.
-- Las FKs se resuelven por subquery de DNI (los IDs autoincrement no son deterministas).

-- ============================================================
-- Bloque 1: Titulares en situaciones límite (DNIs 20000001-20000006)
-- ============================================================
INSERT INTO titulares (dni, nombre, apellido, fecha_nacimiento, domicilio, telefono, email) VALUES
('20000001', 'Tomás', 'Aguirre', DATE_SUB(CURDATE(), INTERVAL 17 YEAR), 'Calle Salta 1122, Santa Fe', '0342-4600001', 'tomas.aguirre@email.com'),
('20000002', 'Elsa', 'Domínguez', DATE_SUB(CURDATE(), INTERVAL 76 YEAR), 'Av. General Paz 3300, Santa Fe', '0342-4600002', 'elsa.dominguez@email.com'),
('20000003', 'Ramiro', 'Ferreyra', DATE_SUB(CURDATE(), INTERVAL 30 YEAR), 'Calle Urquiza 2450, Santa Fe', '0342-4600003', 'ramiro.ferreyra@email.com'),
('20000004', 'Silvana', 'Ojeda', DATE_SUB(CURDATE(), INTERVAL 40 YEAR), 'Bv. Zavalla 780, Santa Fe', '0342-4600004', 'silvana.ojeda@email.com'),
('20000005', 'Diego', 'Palacios', DATE_SUB(CURDATE(), INTERVAL 35 YEAR), 'Av. Facundo Zuviría 5200, Santa Fe', '0342-4600005', 'diego.palacios@email.com'),
('20000006', 'Marcela', 'Vega', DATE_SUB(CURDATE(), INTERVAL 50 YEAR), 'Calle Lavalle 1560, Santa Fe', '0342-4600006', 'marcela.vega@email.com');

-- ============================================================
-- Bloque 2: Inhabilitaciones ACTIVAS (permanente y temporal vigente)
-- ============================================================
INSERT INTO inhabilitaciones (titular_id, fecha_inicio, fecha_fin, motivo, autoridad, numero_expediente) VALUES
((SELECT id FROM titulares WHERE dni = '20000003'), DATE_SUB(CURDATE(), INTERVAL 3 MONTH), NULL,
 'Alcoholemia positiva reiterada en controles viales. Inhabilitación permanente por resolución judicial.',
 'Juzgado Correccional Nº 3 - Santa Fe', 'EXP-2026-01120-J'),
((SELECT id FROM titulares WHERE dni = '20000004'), DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 6 MONTH),
 'Exceso de velocidad grave en zona escolar. Inhabilitación temporal de 7 meses.',
 'Tribunal de Faltas - Juzgado Nº 2', 'EXP-2026-00987-S');

-- ============================================================
-- Bloque 3: Licencias en estados faltantes (INHABILITADA, DUPLICADA) y casos de vigencia
-- ============================================================
INSERT INTO licencias (titular_id, tramite_id, numero_licencia, clase, estado, fecha_emision, fecha_vencimiento, observaciones) VALUES
-- Titular completo: licencia vigente que habilita duplicado/renovación
((SELECT id FROM titulares WHERE dni = '20000005'), NULL, 'SF020000005B', 'B', 'VIGENTE',
 DATE_SUB(CURDATE(), INTERVAL 1 YEAR), DATE_ADD(CURDATE(), INTERVAL 4 YEAR), 'Licencia vigente - titular con trámite de duplicado en curso'),
-- Licencia inhabilitada judicialmente
((SELECT id FROM titulares WHERE dni = '20000006'), NULL, 'SF020000006A', 'A', 'INHABILITADA',
 DATE_SUB(CURDATE(), INTERVAL 4 YEAR), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 'Licencia inhabilitada por resolución judicial'),
-- Historial de duplicación: la licencia vieja queda DUPLICADA...
((SELECT id FROM titulares WHERE dni = '20000006'), NULL, 'SF020000006B', 'B', 'DUPLICADA',
 DATE_SUB(CURDATE(), INTERVAL 3 YEAR), DATE_ADD(CURDATE(), INTERVAL 2 YEAR), 'Reemplazada por duplicado tras denuncia de robo'),
-- Adulto mayor: vigencia reducida (1 año)
((SELECT id FROM titulares WHERE dni = '20000002'), NULL, 'SF020000002B', 'B', 'VIGENTE',
 DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 'Vigencia reducida por edad del titular');

-- ============================================================
-- Bloque 4: Trámites nuevos (flags coherentes con la máquina de estados)
-- ============================================================
INSERT INTO tramites (titular_id, tipo, clase_solicitada, motivo_duplicacion, estado, agente_responsable, observaciones,
                      documentacion_validada, pago_acreditado, examen_teorico_aprobado, examen_practico_aprobado, apto_medico_vigente) VALUES
-- Duplicado por extravío en curso (con pago pendiente asociado en Bloque 5)
((SELECT id FROM titulares WHERE dni = '20000005'), 'DUPLICADO', 'B', 'EXTRAVÍO', 'DOCS_OK', 'Agent002',
 'Duplicado por extravío - documentación validada, pago pendiente', TRUE, FALSE, FALSE, FALSE, FALSE),
-- Duplicado por robo completado (genera la licencia nueva del Bloque 3)
((SELECT id FROM titulares WHERE dni = '20000006'), 'DUPLICADO', 'B', 'ROBO', 'EMITIDA', 'Agent001',
 'Duplicado por robo con denuncia policial - completado', TRUE, TRUE, FALSE, FALSE, FALSE),
-- Renovación de adulto mayor recién iniciada
((SELECT id FROM titulares WHERE dni = '20000002'), 'RENOVACION', 'B', NULL, 'INICIADO', 'Agent003',
 'Renovación anticipada - titular de 76 años', FALSE, FALSE, FALSE, FALSE, FALSE),
-- Emisión clase A de titular de 17 años (edad mínima justa), con teórico aprobado
((SELECT id FROM titulares WHERE dni = '20000001'), 'EMISION', 'A', NULL, 'EX_TEO_OK', 'Agent001',
 'Primera licencia - titular de 17 años, pendiente examen práctico', TRUE, FALSE, TRUE, FALSE, TRUE);

-- Licencia nueva emitida por el duplicado por robo (mantiene el vencimiento de la original)
INSERT INTO licencias (titular_id, tramite_id, numero_licencia, clase, estado, fecha_emision, fecha_vencimiento, observaciones) VALUES
((SELECT id FROM titulares WHERE dni = '20000006'),
 (SELECT id FROM tramites WHERE tipo = 'DUPLICADO' AND motivo_duplicacion = 'ROBO' ORDER BY id DESC LIMIT 1),
 'SF020000106B', 'B', 'VIGENTE', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 2 YEAR),
 'Duplicado emitido - conserva el vencimiento de la licencia original');

-- ============================================================
-- Bloque 5: Pagos en estados faltantes (RECHAZADO, VENCIDO) y PENDIENTE rechazable
-- ============================================================
INSERT INTO pagos (tramite_id, monto, medio, estado, fecha, fecha_vencimiento, fecha_acreditacion, numero_transaccion, numero_comprobante, cajero, observaciones) VALUES
-- PENDIENTE con vencimiento futuro: habilita el botón "Rechazar" en la vista Pagos
((SELECT id FROM tramites WHERE tipo = 'DUPLICADO' AND motivo_duplicacion = 'EXTRAVÍO' AND estado = 'DOCS_OK' ORDER BY id DESC LIMIT 1),
 15000.00, 'CAJA', 'PENDIENTE', NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), NULL, NULL, NULL, NULL,
 'Orden de pago pendiente de acreditación en ventanilla'),
-- RECHAZADO: transferencia no acreditada (sobre el trámite DOCS_OK de V2)
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '89012345') AND estado = 'DOCS_OK' ORDER BY id LIMIT 1),
 35000.00, 'TRANSFERENCIA', 'RECHAZADO', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL, 'TRX999888', NULL, 'Cajero001',
 'Transferencia no acreditada por el banco - comprobante inválido'),
-- VENCIDO: orden generada hace 40 días que expiró sin pagarse (trámite INICIADO de V2, EMISION D)
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '01234567') AND estado = 'INICIADO' ORDER BY id LIMIT 1),
 50000.00, 'CAJA', 'VENCIDO', DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, NULL, NULL, NULL,
 'Orden de pago vencida sin acreditación'),
-- ACREDITADO del duplicado por robo completado
((SELECT id FROM tramites WHERE tipo = 'DUPLICADO' AND motivo_duplicacion = 'ROBO' ORDER BY id DESC LIMIT 1),
 15000.00, 'CAJA', 'ACREDITADO', DATE_SUB(NOW(), INTERVAL 11 DAY), NULL, DATE_SUB(NOW(), INTERVAL 11 DAY), NULL, 'COMP201', 'Cajero002',
 'Pago en ventanilla del duplicado por robo');

-- ============================================================
-- Bloque 6: Turnos en estados faltantes (CANCELADO, AUSENTE) y agenda futura
-- ============================================================
INSERT INTO turnos (titular_id, tramite_id, recurso_id, tipo, tipo_recurso, estado, inicio, fin, profesional_asignado, observaciones) VALUES
-- CANCELADO (pasado reciente)
((SELECT id FROM titulares WHERE dni = '20000005'), NULL, 4, 'DOCUMENTACION', 'BOX', 'CANCELADO',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:30:00'),
 NULL, 'Cancelado a solicitud del titular'),
-- AUSENTE (pasado reciente)
((SELECT id FROM titulares WHERE dni = '20000001'), NULL, 1, 'EXAMEN_TEORICO', 'AULA_TEORICO', 'AUSENTE',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:30:00'),
 'Ing. Luis Spinetta', 'El titular no se presentó al turno'),
-- RESERVADO futuro (tipo EMISION, nunca usado en el seed)
((SELECT id FROM titulares WHERE dni = '20000005'), NULL, 5, 'EMISION', 'BOX', 'RESERVADO',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00'),
 NULL, 'Retiro de licencia duplicada'),
-- CONFIRMADO futuro (examen práctico en pista)
((SELECT id FROM titulares WHERE dni = '20000001'),
 (SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '20000001') AND estado = 'EX_TEO_OK' ORDER BY id LIMIT 1),
 11, 'EXAMEN_PRACTICO', 'PISTA', 'CONFIRMADO',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:30:00'),
 'Ing. Luis Spinetta', 'Examen práctico confirmado');

-- ============================================================
-- Bloque 7: Exámenes en el límite exacto de aprobación y aptos médicos por vencer/vencidos
-- ============================================================
-- Teórico aprobado justo en el 80% (24/30) del trámite EX_TEO_OK del titular de 17 años
INSERT INTO examenes_teoricos (tramite_id, fecha, examinador, puntaje, cantidad_preguntas, respuestas_correctas, aprobado, observaciones) VALUES
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '20000001') AND estado = 'EX_TEO_OK' ORDER BY id LIMIT 1),
 DATE_SUB(NOW(), INTERVAL 7 DAY), 'Ing. Luis Spinetta', 80, 30, 24, TRUE, 'Aprobado en el límite exacto (80%)');

-- Práctico aprobado en el límite exacto (3 faltas leves, 0 graves) sobre el trámite EX_PRA_OK existente
INSERT INTO examenes_practicos (tramite_id, fecha, examinador, vehiculo_utilizado, pista_utilizada, faltas_leves, faltas_graves, aprobado, observaciones) VALUES
((SELECT id FROM tramites WHERE estado = 'EX_PRA_OK' ORDER BY id LIMIT 1),
 DATE_SUB(NOW(), INTERVAL 5 DAY), 'Ing. Carla Bustos', 'Fiat Cronos', 'Pista Vial A', 3, 0, TRUE,
 'Aprobado en el límite exacto de faltas leves permitidas');

INSERT INTO aptos_medicos (tramite_id, fecha, profesional, matricula_profesional, apto, fecha_vencimiento, presion_sistolica, presion_diastolica, agudeza_visual, observaciones) VALUES
-- Apto PRÓXIMO A VENCER (~20 días) para la alerta de vencimientos
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '20000001') AND estado = 'EX_TEO_OK' ORDER BY id LIMIT 1),
 DATE_SUB(NOW(), INTERVAL 345 DAY), 'Dra. Marta Gómez', 'MP-4521', TRUE, DATE_ADD(CURDATE(), INTERVAL 20 DAY),
 118.0, 76.0, 'OD: 1.0, OI: 1.0', 'Apto próximo a vencer - renovar antes de la emisión'),
-- Apto VENCIDO durante un trámite en curso (trámite APTO_MED de V2)
((SELECT id FROM tramites WHERE titular_id = (SELECT id FROM titulares WHERE dni = '45678901') AND estado = 'APTO_MED' ORDER BY id LIMIT 1),
 DATE_SUB(NOW(), INTERVAL 13 MONTH), 'Dr. Aníbal Soria', 'MP-3310', TRUE, DATE_SUB(CURDATE(), INTERVAL 30 DAY),
 122.0, 80.0, 'OD: 0.9, OI: 1.0', 'Apto médico expirado durante el trámite - requiere nueva evaluación');

-- ============================================================
-- Bloque 8: Costo histórico fuera de vigencia (historial de precios en la vista Costos)
-- ============================================================
INSERT INTO costos_tramite (tipo_tramite, clase_licencia, costo, fecha_vigencia_desde, fecha_vigencia_hasta, descripcion, activo) VALUES
('EMISION', 'B', 28000.00, '2024-01-01', '2024-12-31', 'Emisión licencia clase B - Tarifa 2024 (histórica)', TRUE);

-- ============================================================
-- Bloque 9: Correcciones de consistencia sobre datos previos (V2/V10)
-- ============================================================
-- Licencias marcadas VIGENTE pero con vencimiento pasado -> VENCIDA
UPDATE licencias SET estado = 'VENCIDA' WHERE estado = 'VIGENTE' AND fecha_vencimiento < CURDATE();

-- Duplicados anteriores a V12 sin motivo -> alinear con la regla actual (motivo obligatorio)
UPDATE tramites SET motivo_duplicacion = 'EXTRAVÍO' WHERE tipo = 'DUPLICADO' AND motivo_duplicacion IS NULL;
