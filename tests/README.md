# SIGELIC - Pruebas de API REST

Este directorio contiene pruebas completas para todos los endpoints de la API REST de SIGELIC (Sistema Integral de Gestión de Licencias de Conducir).

## Archivos de Prueba

### 📁 `api-tests.http`
Archivo principal con **46+ pruebas** organizadas por funcionalidad:
- ✅ Flujo completo de tramitación
- ✅ Pruebas de todos los endpoints
- ✅ Casos de error y validación
- ✅ Compatible con VS Code REST Client

### 📁 `controller-tests.http`
Pruebas organizadas **por controlador específico**:
- 🎯 TitularController (7 endpoints)
- 🎯 TramiteController (10 endpoints)
- 🎯 TurnoController (9 endpoints)
- 🎯 PagoController (8 endpoints)
- 🎯 LicenciaController (11 endpoints)

### 📁 `postman-collection.json`
Colección de Postman con:
- 📊 Variables de entorno predefinidas
- 🔄 Scripts de validación automática
- 📈 Tests de rendimiento
- 🧪 Configuración para diferentes entornos

## Cómo Usar las Pruebas

### Opción 1: VS Code (Recomendado)

1. **Instalar extensión REST Client**:
   ```
   Ctrl+Shift+X → Buscar "REST Client" → Instalar
   ```

2. **Ejecutar la aplicación**:
   ```bash
   cd sigelic
   mvn spring-boot:run
   ```

3. **Abrir archivo de pruebas**:
   - `api-tests.http` para flujo completo
   - `controller-tests.http` para pruebas específicas

4. **Ejecutar pruebas**:
   - Hacer clic en "Send Request" sobre cada prueba
   - O usar `Ctrl+Alt+R` para ejecutar

### Opción 2: Postman

1. **Importar colección**:
   - Abrir Postman
   - Import → Upload Files → `postman-collection.json`

2. **Configurar variables**:
   ```
   baseUrl: http://localhost:8080/api
   titularId: 1
   tramiteId: 1
   ```

3. **Ejecutar colección completa** o pruebas individuales

### Opción 3: cURL (Terminal)

```bash
# Ejemplo: Crear titular
curl -X POST http://localhost:8080/api/titulares \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "dni": "12345678",
    "fechaNacimiento": "1990-05-15",
    "domicilio": "Av. San Martín 1234",
    "email": "juan@email.com"
  }'
```

## Flujo de Prueba Recomendado

### 🎯 Flujo Completo (Pasos 1-13 en api-tests.http)

1. **Crear titular** → Obtener ID
2. **Iniciar trámite** → Obtener ID de trámite
3. **Validar documentación** → Cambio a estado válido
4. **Reservar turno médico** → Programar cita
5. **Completar turno médico** → Aprobar apto
6. **Reservar examen teórico** → Programar examen
7. **Completar examen teórico** → Aprobar examen
8. **Reservar examen práctico** → Programar examen
9. **Completar examen práctico** → Aprobar examen
10. **Crear orden de pago** → Generar pago
11. **Confirmar pago** → Acreditar pago
12. **Emitir licencia** → Crear licencia
13. **Verificar licencia** → Confirmar emisión

## Endpoints Disponibles

### 👥 TITULARES `/api/titulares`
```
POST   /                    - Crear titular
GET    /{id}                - Obtener por ID
GET    /dni/{dni}           - Buscar por DNI
GET    /                    - Listar todos
PUT    /{id}                - Actualizar
DELETE /{id}                - Eliminar
GET    /{id}/puede-tramitar - Verificar elegibilidad
```

### 📋 TRÁMITES `/api/tramites`
```
POST   /                           - Iniciar trámite
GET    /{id}                       - Obtener por ID
PATCH  /{id}/validar-documentacion - Validar docs
PATCH  /{id}/rechazar              - Rechazar trámite
POST   /{id}/emitir-licencia       - Emitir licencia
GET    /estado/{estado}            - Por estado
GET    /titular/{id}               - Por titular
GET    /titular/{id}/activo        - Trámite activo
GET    /contador/estado/{estado}   - Contar por estado
GET    /contador/tipo/{tipo}       - Contar por tipo
```

### 📅 TURNOS `/api/turnos`
```
POST   /                     - Reservar turno
GET    /{id}                 - Obtener por ID
GET    /titular/{id}         - Por titular
GET    /fecha/{fecha}        - Por fecha
GET    /disponibles          - Turnos disponibles
PATCH  /{id}/confirmar       - Confirmar turno
PATCH  /{id}/completar       - Completar turno
PATCH  /{id}/cancelar        - Cancelar turno
PUT    /{id}                 - Actualizar turno
```

### 💳 PAGOS `/api/pagos`
```
POST   /orden               - Crear orden
POST   /manual              - Pago manual
GET    /{id}                - Obtener por ID
GET    /tramite/{id}        - Por trámite
PATCH  /{id}/confirmar      - Confirmar pago
PATCH  /{id}/anular         - Anular pago
GET    /costo/{tipo}        - Costo por tipo
GET    /estadisticas        - Estadísticas
```

### 🪪 LICENCIAS `/api/licencias`
```
GET    /{id}                        - Obtener por ID
GET    /numero/{numero}             - Por número
GET    /titular/{id}                - Por titular
GET    /titular/{id}/vigentes       - Vigentes por titular
GET    /proximas-vencer             - Próximas a vencer
GET    /vencidas                    - Vencidas
PATCH  /{id}/suspender              - Suspender
PATCH  /{id}/inhabilitar            - Inhabilitar
PATCH  /{id}/actualizar-domicilio   - Actualizar domicilio
POST   /actualizar-vencidas         - Actualizar vencidas
GET    /contador/emitidas           - Contador emitidas
```

## Datos de Prueba

### Titulares de Ejemplo
- **Juan Carlos Pérez** - DNI: 12345678
- **María Elena González** - DNI: 87654321
- **Carlos Martínez** - DNI: 33445566

### Tipos de Trámite
- `PRIMERA_LICENCIA` - Primera vez
- `RENOVACION` - Renovación 
- `DUPLICADO` - Por pérdida/robo
- `CAMBIO_DOMICILIO` - Cambio de domicilio

### Clases de Licencia
- `CLASE_A` - Motocicletas
- `CLASE_B` - Automóviles
- `CLASE_C` - Camiones
- `CLASE_D` - Transporte público
- `CLASE_E` - Transporte de carga

### Tipos de Turno
- `EXAMEN_TEORICO` - Examen teórico
- `EXAMEN_PRACTICO` - Examen práctico
- `APTO_MEDICO` - Control médico

## Validaciones y Errores

### ✅ Casos de Éxito (200/201)
- Datos válidos
- Flujo correcto
- Estados permitidos

### ❌ Casos de Error (400/404/500)
- DNI duplicado
- Fechas inválidas
- IDs inexistentes
- Datos faltantes
- Validaciones de negocio

## Configuración de Entornos

```bash
# Desarrollo
baseUrl = http://localhost:8080/api

# Testing
baseUrl = http://localhost:8081/api

# Staging
baseUrl = https://sigelic-staging.example.com/api

# Producción
baseUrl = https://sigelic.example.com/api
```

## Tips de Uso

### 🔧 Configuración Inicial
1. Verificar que la aplicación esté corriendo
2. Comprobar conectividad a la base de datos
3. Ejecutar primero los endpoints GET para verificar

### 📊 Monitoreo
- Revisar logs de la aplicación
- Verificar tiempos de respuesta
- Validar datos en base de datos

### 🚀 Optimización
- Usar variables para IDs dinámicos
- Ejecutar pruebas en orden lógico
- Limpiar datos de prueba periódicamente

## Troubleshooting

### Error de Conexión
```
Connection refused → Verificar que la app esté corriendo
```

### Error 404
```
Not Found → Verificar URL y que el recurso exista
```

### Error 400
```
Bad Request → Verificar formato JSON y validaciones
```

### Error 500
```
Internal Server Error → Revisar logs de la aplicación
```

---

**¡Las pruebas están listas para usar!** 🚀

Para comenzar, ejecuta la aplicación con `mvn spring-boot:run` y empieza con el flujo completo en `api-tests.http`.
