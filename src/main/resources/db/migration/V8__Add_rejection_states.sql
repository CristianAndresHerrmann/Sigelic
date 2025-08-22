-- =======================================================================
-- SIGELIC - Sistema Integral de Gestión de Licencias de Conducir
-- Migración V8: Agregar estados de rechazo
-- Fecha: 2025-08-21
-- =======================================================================

-- Agregar nuevos estados de rechazo al enum de tramites
-- En MySQL, necesitamos modificar el tipo ENUM para agregar los nuevos valores
ALTER TABLE tramites 
MODIFY COLUMN estado ENUM(
    'INICIADO', 
    'DOCS_OK', 
    'DOCS_RECHAZADAS',
    'PAGO_OK', 
    'EX_TEO_OK', 
    'EX_TEO_RECHAZADO',
    'EX_PRA_OK', 
    'EX_PRA_RECHAZADO',
    'APTO_MED', 
    'APTO_MED_RECHAZADO',
    'EMITIDA', 
    'RECHAZADA'
) NOT NULL DEFAULT 'INICIADO';
