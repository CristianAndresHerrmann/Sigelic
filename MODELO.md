# SIGELIC - Modelo de Datos

## Descripción
Este documento describe las clases de modelo del Sistema Integral de Gestión de Licencias de Conducir (SIGELIC).

## Entidades Principales

### 1. Titular
Representa a una persona que puede solicitar licencias de conducir.
- **Campos principales**: nombre, apellido, DNI, fecha de nacimiento, domicilio
- **Relaciones**: inhabilitaciones, licencias, trámites, turnos
- **Métodos utilitarios**: cálculo de edad, verificación de inhabilitaciones activas

### 2. Licencia
Representa una licencia de conducir emitida.
- **Campos principales**: clase, fecha de emisión/vencimiento, estado, número
- **Relaciones**: titular, trámite
- **Lógica de negocio**: cálculo de vigencia según edad, fechas de vencimiento ajustadas al cumpleaños

### 3. Tramite
Controla el flujo de emisión/renovación de licencias.
- **Estados**: INICIADO → DOCS_OK → APTO_MED → EX_TEO_OK → EX_PRA_OK → PAGO_OK → EMITIDA
- **Tipos**: EMISION, RENOVACION, DUPLICADO, CAMBIO_DOMICILIO
- **Seguimiento**: requisitos cumplidos, auditoría de cambios

### 4. ExamenTeorico
Registra los exámenes teóricos realizados.
- **Aprobación**: ≥ 80% de respuestas correctas
- **Vigencia**: máximo 6 meses
- **Campos**: puntaje, cantidad de preguntas, examinador

### 5. ExamenPractico
Registra los exámenes prácticos realizados.
- **Aprobación**: sin faltas graves, máximo 3 faltas leves
- **Vigencia**: máximo 6 meses
- **Campos**: faltas leves/graves, vehículo utilizado, pista

### 6. AptoMedico
Registra los exámenes médicos y aptitudes.
- **Campos**: profesional, apto, restricciones, datos médicos
- **Vigencia**: configurable, por defecto 1 año
- **Observaciones**: presión arterial, agudeza visual

### 7. Pago
Gestiona los pagos de los trámites.
- **Medios**: CAJA, TRANSFERENCIA, PASARELA_ONLINE
- **Estados**: PENDIENTE, ACREDITADO, RECHAZADO, VENCIDO
- **Vencimiento**: por defecto 48 horas para órdenes de pago

### 8. Turno
Gestiona la agenda de turnos.
- **Tipos**: DOCUMENTACION, APTO_MEDICO, EXAMEN_TEORICO, EXAMEN_PRACTICO, EMISION
- **Estados**: RESERVADO, CONFIRMADO, COMPLETADO, CANCELADO, AUSENTE
- **Validaciones**: no solapamiento para el mismo titular y tipo

### 9. Inhabilitacion
Registra sanciones e inhabilitaciones.
- **Campos**: motivo, fechas de inicio/fin, autoridad, expediente
- **Lógica**: verificación de inhabilitaciones activas por fecha

## Entidades Auxiliares

### 10. Recurso
Define los recursos disponibles para turnos (boxes, pistas, consultorios).
- **Tipos**: BOX, PISTA, CONSULTORIO_MEDICO, AULA_TEORICO
- **Configuración**: horarios, capacidad, duración de turnos

### 11. CostoTramite
Define los costos de cada tipo de trámite por clase de licencia.
- **Vigencia**: por fechas
- **Flexibilidad**: diferentes costos por tipo y clase

### 12. RegistroAuditoria
Registra todos los cambios para trazabilidad.
- **Operaciones**: CREATE, UPDATE, DELETE
- **Seguimiento**: usuario, fecha, valores anteriores y nuevos

## Enums Utilizados

- **ClaseLicencia**: A, B, C, D, E (con edad mínima requerida)
- **TipoTramite**: EMISION, RENOVACION, DUPLICADO, CAMBIO_DOMICILIO
- **EstadoTramite**: INICIADO, DOCS_OK, APTO_MED, EX_TEO_OK, EX_PRA_OK, PAGO_OK, EMITIDA, RECHAZADA
- **EstadoLicencia**: VIGENTE, VENCIDA, SUSPENDIDA, INHABILITADA, DUPLICADA
- **MedioPago**: CAJA, TRANSFERENCIA, PASARELA_ONLINE
- **EstadoPago**: PENDIENTE, ACREDITADO, RECHAZADO, VENCIDO
- **TipoTurno**: DOCUMENTACION, APTO_MEDICO, EXAMEN_TEORICO, EXAMEN_PRACTICO, EMISION
- **EstadoTurno**: RESERVADO, CONFIRMADO, COMPLETADO, CANCELADO, AUSENTE
- **TipoRecurso**: BOX, PISTA, CONSULTORIO_MEDICO, AULA_TEORICO

## Reglas de Negocio Implementadas

1. **Elegibilidad por edad**: Validada en ClaseLicencia
2. **Vigencia por edad**: Implementada en Licencia.calcularVigenciaEnAnios()
3. **Fecha de vencimiento ajustada**: Licencia.calcularFechaVencimiento()
4. **Prerrequisitos de emisión**: Controlados en Tramite.todosLosRequisitosCumplidos()
5. **Aprobación de exámenes**: Lógica en ExamenTeorico y ExamenPractico
6. **Inhabilitaciones**: Verificación automática en Titular
7. **Duplicados**: No requieren exámenes si la licencia original estaba vigente
8. **Turnos no solapados**: Validación en Turno.seSolapaCon()
9. **Trazabilidad**: Mediante RegistroAuditoria
10. **Política de pagos**: Vencimiento automático en 48h

## Tecnologías Utilizadas

- **JPA/Hibernate**: Para persistencia
- **Bean Validation**: Para validaciones
- **Lombok**: Para reducir código boilerplate
- **MySQL**: Base de datos
- **Spring Boot**: Framework principal

## Próximos Pasos

Las clases de modelo están listas para continuar con:
1. Repositorios JPA
2. Servicios de negocio
3. Controladores REST
4. Interface de usuario con Vaadin
