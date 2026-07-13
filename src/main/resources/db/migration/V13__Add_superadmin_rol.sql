-- Agrega el rol SUPERADMIN al enum de roles de usuarios
-- Cuenta técnica de soporte con todos los permisos (excepción documentada al mínimo privilegio).
-- Extender un ENUM agregando el valor al final es una operación aditiva y segura en MySQL.

ALTER TABLE usuarios MODIFY COLUMN rol ENUM('ADMINISTRADOR','SUPERVISOR','AGENTE','MEDICO','EXAMINADOR','CAJERO','AUDITOR','CIUDADANO','SUPERADMIN') NOT NULL;
