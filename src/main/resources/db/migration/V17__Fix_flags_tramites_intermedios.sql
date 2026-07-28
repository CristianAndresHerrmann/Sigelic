-- Migración V17: Corrige los flags de requisitos de los trámites en estados INTERMEDIOS
-- El seed (V2/V10) insertó trámites en estados avanzados (DOCS_OK, APTO_MED, EX_TEO_OK,
-- EX_PRA_OK, PAGO_OK) sin setear los flags de las etapas ya superadas. Eso deja la máquina
-- de estados inconsistente: el trámite no muestra el botón de la etapa siguiente y no puede avanzar
-- (p. ej. un EX_TEO_OK con examen_teorico_aprobado=0 nunca habilita el examen práctico).
-- Se completan los flags según el estado alcanzado y el tipo de trámite.
--   apto médico aplica salvo DUPLICADO / CAMBIO_DOMICILIO
--   exámenes aplican solo a EMISION / RENOVACION

-- DOCS_OK: documentación validada
UPDATE tramites SET documentacion_validada = TRUE WHERE estado = 'DOCS_OK';

-- APTO_MED: documentación + apto médico
UPDATE tramites SET documentacion_validada = TRUE, apto_medico_vigente = TRUE WHERE estado = 'APTO_MED';

-- EX_TEO_OK: documentación + apto médico (si aplica) + teórico aprobado
UPDATE tramites
SET documentacion_validada = TRUE,
    apto_medico_vigente = CASE WHEN tipo IN ('DUPLICADO','CAMBIO_DOMICILIO') THEN apto_medico_vigente ELSE TRUE END,
    examen_teorico_aprobado = TRUE
WHERE estado = 'EX_TEO_OK';

-- EX_PRA_OK: documentación + apto médico (si aplica) + teórico + práctico
UPDATE tramites
SET documentacion_validada = TRUE,
    apto_medico_vigente = CASE WHEN tipo IN ('DUPLICADO','CAMBIO_DOMICILIO') THEN apto_medico_vigente ELSE TRUE END,
    examen_teorico_aprobado = TRUE,
    examen_practico_aprobado = TRUE
WHERE estado = 'EX_PRA_OK';

-- PAGO_OK y EMITIDA: todos los requisitos según el tipo + pago acreditado
UPDATE tramites
SET documentacion_validada = TRUE,
    pago_acreditado = TRUE,
    apto_medico_vigente = CASE WHEN tipo IN ('DUPLICADO','CAMBIO_DOMICILIO') THEN apto_medico_vigente ELSE TRUE END,
    examen_teorico_aprobado = CASE WHEN tipo IN ('EMISION','RENOVACION') THEN TRUE ELSE examen_teorico_aprobado END,
    examen_practico_aprobado = CASE WHEN tipo IN ('EMISION','RENOVACION') THEN TRUE ELSE examen_practico_aprobado END
WHERE estado IN ('PAGO_OK','EMITIDA');
