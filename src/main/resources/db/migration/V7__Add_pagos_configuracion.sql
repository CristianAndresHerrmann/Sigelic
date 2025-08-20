-- =======================================================================
-- SIGELIC - Sistema Integral de Gestión de Licencias de Conducir
-- Migración V7: Agregar configuraciones específicas para el sistema de pagos
-- Fecha: 2025-08-20
-- =======================================================================

-- Insertar configuraciones para el sistema de pagos
INSERT INTO configuracion (clave, valor, descripcion, categoria, tipo, modificable, actualizado_por) VALUES

-- Configuración de Pagos
('pagos.vencimiento_horas', '48', 'Horas para el vencimiento de órdenes de pago', 'PAGOS', 'INTEGER', TRUE, 'SYSTEM'),
('pagos.mostrar_pendientes_primero', 'true', 'Mostrar pagos pendientes al principio de la lista', 'PAGOS', 'BOOLEAN', TRUE, 'SYSTEM'),
('pagos.permitir_anulacion_acreditados', 'false', 'Permitir anulación de pagos ya acreditados', 'PAGOS', 'BOOLEAN', TRUE, 'SYSTEM'),
('pagos.notificar_vencimiento', 'true', 'Enviar notificaciones de vencimiento de pagos', 'PAGOS', 'BOOLEAN', TRUE, 'SYSTEM'),
('pagos.formato_comprobante', 'SIGELIC-{año}-{numero}', 'Formato para números de comprobante', 'PAGOS', 'TEXT', TRUE, 'SYSTEM'),

-- Configuración de Tasas
('tasas.actualizacion_automatica', 'false', 'Actualización automática de tasas según inflación', 'TASAS', 'BOOLEAN', TRUE, 'SYSTEM'),
('tasas.porcentaje_actualizacion_anual', '0', 'Porcentaje de actualización automática anual', 'TASAS', 'INTEGER', TRUE, 'SYSTEM'),
('tasas.fecha_ultima_actualizacion', '2025-01-01', 'Fecha de la última actualización de tasas', 'TASAS', 'TEXT', FALSE, 'SYSTEM'),

-- Configuración de Medios de Pago
('medios_pago.caja_habilitada', 'true', 'Habilitar pagos en caja', 'MEDIOS_PAGO', 'BOOLEAN', TRUE, 'SYSTEM'),
('medios_pago.transferencia_habilitada', 'true', 'Habilitar pagos por transferencia', 'MEDIOS_PAGO', 'BOOLEAN', TRUE, 'SYSTEM'),
('medios_pago.pasarela_habilitada', 'false', 'Habilitar pagos por pasarela online', 'MEDIOS_PAGO', 'BOOLEAN', TRUE, 'SYSTEM'),
('medios_pago.cbu_recaudacion', '0170329540000001234567', 'CBU para recaudación de pagos', 'MEDIOS_PAGO', 'TEXT', TRUE, 'SYSTEM'),
('medios_pago.alias_recaudacion', 'SIGELIC.PAGOS', 'Alias para recaudación de pagos', 'MEDIOS_PAGO', 'TEXT', TRUE, 'SYSTEM');

-- Registrar en auditoría la creación de configuraciones de pagos
INSERT INTO auditoria (entidad, entidad_id, operacion, usuario, fecha, detalles) VALUES
('configuracion', 0, 'INSERT', 'SYSTEM', CURRENT_TIMESTAMP, 'Configuraciones de pagos agregadas al sistema');
