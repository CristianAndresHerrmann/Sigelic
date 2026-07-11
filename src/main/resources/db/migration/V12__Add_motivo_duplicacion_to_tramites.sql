-- Agrega el motivo de duplicación al trámite (aplica solo a trámites de tipo DUPLICADO).
-- Valores según el enum MotivoDuplicacion: ROBO, EXTRAVÍO, DETERIORO.

ALTER TABLE tramites
    ADD COLUMN motivo_duplicacion ENUM('ROBO','EXTRAVÍO','DETERIORO') NULL AFTER clase_solicitada;
