-- Corrige las descripciones de los costos de emisión para las clases C, D y E,
-- que contradecían las definiciones del enum ClaseLicencia:
--   C = Camiones hasta 7500kg, D = Transporte de pasajeros, E = Camiones pesados

UPDATE costos_tramite SET descripcion = 'Emisión licencia clase C - Camiones hasta 7500kg'
WHERE tipo_tramite = 'EMISION' AND clase_licencia = 'C';

UPDATE costos_tramite SET descripcion = 'Emisión licencia clase D - Transporte de pasajeros'
WHERE tipo_tramite = 'EMISION' AND clase_licencia = 'D';

UPDATE costos_tramite SET descripcion = 'Emisión licencia clase E - Camiones pesados'
WHERE tipo_tramite = 'EMISION' AND clase_licencia = 'E';
