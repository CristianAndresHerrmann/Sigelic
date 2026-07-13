-- Migración V15: Licencia en estado SUSPENDIDA con vencimiento futuro
-- La única licencia SUSPENDIDA del seed (V10, SF013579135B) tenía vencimiento pasado y el
-- proceso automático de vencimientos la marcó VENCIDA, dejando el estado sin representación.
-- Se asocia al titular con inhabilitación temporal vigente (20000004), coherente con la sanción.

INSERT INTO licencias (titular_id, tramite_id, numero_licencia, clase, estado, fecha_emision, fecha_vencimiento, observaciones) VALUES
((SELECT id FROM titulares WHERE dni = '20000004'), NULL, 'SF020000004B', 'B', 'SUSPENDIDA',
 DATE_SUB(CURDATE(), INTERVAL 2 YEAR), DATE_ADD(CURDATE(), INTERVAL 3 YEAR),
 'Suspendida durante la inhabilitación temporal del titular (EXP-2026-00987-S)');
