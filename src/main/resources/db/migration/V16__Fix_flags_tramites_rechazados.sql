-- Migración V16: Corrige los flags de requisitos de los trámites en estados de rechazo
-- El seed (V10) insertó los estados de rechazo sin setear los flags de las etapas ya superadas,
-- lo que dejaba inconsistente la máquina de estados y ocultaba el botón de reintento en la UI
-- (p. ej. un trámite en EX_PRA_RECHAZADO debe tener el examen teórico aprobado para poder reintentar el práctico).

-- Rechazo de examen práctico: se llegó hasta el práctico → documentación, apto médico (si aplica) y teórico aprobados
UPDATE tramites
SET documentacion_validada = TRUE,
    examen_teorico_aprobado = TRUE,
    apto_medico_vigente = CASE WHEN tipo IN ('DUPLICADO','CAMBIO_DOMICILIO') THEN apto_medico_vigente ELSE TRUE END
WHERE estado = 'EX_PRA_RECHAZADO';

-- Rechazo de examen teórico: se pasó la documentación y el apto médico (si aplica)
UPDATE tramites
SET documentacion_validada = TRUE,
    apto_medico_vigente = CASE WHEN tipo IN ('DUPLICADO','CAMBIO_DOMICILIO') THEN apto_medico_vigente ELSE TRUE END
WHERE estado = 'EX_TEO_RECHAZADO';

-- Rechazo de apto médico: se pasó la documentación
UPDATE tramites
SET documentacion_validada = TRUE
WHERE estado = 'APTO_MED_RECHAZADO';
