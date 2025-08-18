# SIGELIC - Repositorios y Servicios

## Repositorios JPA

### Repositorios Principales

#### 1. TitularRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por DNI, nombre/apellido, email, nombre completo
- **Consultas especiales**: Titulares con inhabilitaciones activas
- **Validaciones**: Verificación de existencia por DNI y email

#### 2. LicenciaRepository  
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por titular, estado, clase, número de licencia
- **Consultas especiales**: Licencias próximas a vencer, vencidas, vigentes
- **Estadísticas**: Conteo de licencias emitidas por período

#### 3. TramiteRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por titular, estado, tipo
- **Consultas especiales**: Trámite activo por titular, trámites pendientes por estado
- **Estadísticas**: Conteo por estado y tipo en períodos

#### 4. ExamenTeoricoRepository & ExamenPracticoRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por trámite, aprobación, examinador
- **Consultas especiales**: Último examen aprobado, exámenes en período
- **Estadísticas**: Promedios, conteos de aprobados, rendimiento

#### 5. AptoMedicoRepository
- **Operaciones básicas**: CRUD completo  
- **Búsquedas**: Por trámite, profesional, estado de apto
- **Consultas especiales**: Aptos próximos a vencer, aptos vencidos
- **Estadísticas**: Conteos por período

#### 6. PagoRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por trámite, estado, medio, números de transacción/comprobante
- **Consultas especiales**: Pagos acreditados en período, pagos vencidos
- **Estadísticas**: Suma de montos, conteos por medio de pago

#### 7. TurnoRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por titular, recurso, trámite, tipo, estado
- **Consultas especiales**: Turnos conflictivos, turnos solapados, turnos por profesional
- **Estadísticas**: Conteos por estado en períodos

#### 8. InhabilitacionRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por titular, autoridad, número de expediente
- **Consultas especiales**: Inhabilitaciones activas, vencidas
- **Validaciones**: Existencia de inhabilitaciones activas

#### 9. RecursoRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por tipo, estado activo, nombre, ubicación
- **Consultas especiales**: Recursos activos por tipo

#### 10. CostoTramiteRepository
- **Operaciones básicas**: CRUD completo
- **Búsquedas**: Por tipo de trámite, clase de licencia, estado activo
- **Consultas especiales**: Costos vigentes, costos vencidos

## Servicios de Negocio

### Servicios Principales

#### 1. TitularService
**Responsabilidades:**
- Gestión completa de titulares
- Validaciones de negocio (DNI único, email único)
- Verificación de elegibilidad para trámites
- Gestión de inhabilitaciones

**Métodos destacados:**
- `puedeIniciarTramite()`: Verifica inhabilitaciones activas
- `agregarInhabilitacion()`: Gestiona sanciones
- Validaciones automáticas en save/update

#### 2. TramiteService
**Responsabilidades:**
- Flujo completo de trámites
- Validación de prerrequisitos
- Gestión de estados del trámite
- Coordinación con otros servicios

**Métodos destacados:**
- `iniciarTramite()`: Valida edad, inhabilitaciones, trámites activos
- `validarDocumentacion()`: Progresa el estado del trámite
- `registrarExamenTeorico/Practico()`: Gestiona exámenes
- `registrarAptoMedico()`: Gestiona certificaciones médicas
- `emitirLicencia()`: Culmina el proceso

#### 3. LicenciaService  
**Responsabilidades:**
- Emisión y gestión de licencias
- Cálculo de vigencias según edad
- Duplicados y cambios de domicilio
- Suspensiones e inhabilitaciones

**Métodos destacados:**
- `emitirLicencia()`: Calcula vigencia, genera número
- `duplicarLicencia()`: Mantiene vencimiento original
- `actualizarDomicilio()`: Reimpresión con nuevo domicilio
- `actualizarLicenciasVencidas()`: Proceso batch

#### 4. TurnoService
**Responsabilidades:**
- Gestión completa de turnos
- Validación de disponibilidad
- Prevención de solapamientos
- Gestión de recursos

**Métodos destacados:**
- `reservarTurno()`: Valida disponibilidad y conflictos
- `confirmarTurno()`: Progresa el estado
- `getHorariosDisponibles()`: Calcula slots libres
- Validaciones de horarios y recursos

#### 5. PagoService
**Responsabilidades:**
- Gestión completa de pagos
- Cálculo de costos
- Múltiples medios de pago
- Procesamiento de vencimientos

**Métodos destacados:**
- `crearOrdenPago()`: Calcula costo automáticamente
- `procesarPagoOnline()`: Integración con pasarelas
- `procesarPagosVencidos()`: Proceso batch
- `obtenerCostoTramite()`: Consulta costos vigentes

#### 6. ReporteService
**Responsabilidades:**
- Generación de reportes
- Dashboard con estadísticas
- Análisis de rendimiento
- Métricas del sistema

**Métodos destacados:**
- `getDashboard()`: Vista general del sistema
- `getReporteTramitesPorPeriodo()`: Análisis de trámites
- `getReporteRecaudacionPorPeriodo()`: Métricas financieras
- `getReporteRendimientoExaminadores()`: Performance de personal

## Características Técnicas

### Transaccionalidad
- **@Transactional**: Todos los servicios usan transacciones
- **readOnly = true**: Consultas optimizadas
- **Rollback automático**: En caso de errores

### Validaciones
- **Bean Validation**: En las entidades
- **Validaciones de negocio**: En los servicios
- **Verificaciones de estado**: Antes de cambios críticos

### Logging
- **SLF4J + Logback**: Logging estructurado
- **Eventos importantes**: Creación, actualización, eliminación
- **Contexto**: Información relevante en cada log

### Manejo de Errores
- **IllegalArgumentException**: Para parámetros inválidos
- **IllegalStateException**: Para violaciones de reglas de negocio
- **Mensajes descriptivos**: Para facilitar debugging

### Optimizaciones
- **FetchType.LAZY**: Carga perezosa por defecto
- **Consultas específicas**: Para evitar N+1 queries
- **Índices**: En campos de búsqueda frecuente
- **Cacheo**: Para consultas de configuración

## Reglas de Negocio Implementadas

### Titulares
✅ DNI y email únicos  
✅ Verificación de inhabilitaciones  
✅ Validación de edad para clases de licencia  

### Trámites  
✅ Un trámite activo por titular  
✅ Flujo de estados secuencial  
✅ Validación de prerrequisitos  
✅ Trazabilidad completa  

### Licencias
✅ Vigencia según edad del titular  
✅ Fecha de vencimiento ajustada al cumpleaños  
✅ Numeración única automática  
✅ Estados well-defined  

### Turnos
✅ No solapamiento por titular y tipo  
✅ Validación de disponibilidad de recursos  
✅ Respeto de horarios de funcionamiento  

### Pagos
✅ Vencimiento automático de órdenes  
✅ Múltiples medios de pago  
✅ Cálculo automático de costos  
✅ Trazabilidad de transacciones  

## Próximos Pasos

Con los repositorios y servicios implementados, el siguiente paso es crear:

1. **Controladores REST** para la API
2. **DTOs** para transferencia de datos
3. **Tests unitarios** para los servicios
4. **Interface Vaadin** para la UI
5. **Configuración de seguridad**
6. **Documentación de API** con Swagger
