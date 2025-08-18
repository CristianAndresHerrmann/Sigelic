# SIGELIC - Sistema Integral de Gestión de Licencias de Conducir

Sistema web para digitalizar el ciclo completo de emisión y renovación de licencias de conducir de un municipio.

## 🎯 Descripción

SIGELIC automatiza todo el proceso de gestión de licencias de conducir, desde la solicitud inicial hasta la emisión final, incluyendo turnos, exámenes, pagos y seguimiento completo del trámite.

## 🏗️ Stack Tecnológico

### Backend
- **Java 21** - Lenguaje principal
- **Spring Boot 3.3.2** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring Security** - Seguridad y autenticación
- **MySQL 8.x** - Base de datos principal
- **Flyway** - Versionado de base de datos
- **Lombok** - Reducción de código boilerplate

### Frontend
- **Vaadin 24** - Framework UI Java

### Herramientas
- **Maven** - Gestión de dependencias
- **H2 Database** - Base de datos para testing

## 🚀 Cómo levantar el proyecto

### Prerrequisitos
- Java 21+
- MySQL 8.x
- Maven 3.8+

### Pasos

```bash
# 1. Clonar repositorio
git clone https://github.com/CristianAndresHerrmann/Sigelic.git
cd sigelic

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales reales

# 3. Crear base de datos MySQL
mysql -u root -p
CREATE DATABASE sigelic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 4. Ejecutar aplicación
mvn spring-boot:run

# 5. Acceder a la aplicación
# http://localhost:8080
```

### Para desarrollo/testing con H2
```bash
mvn spring-boot:run -Dspring.profiles.active=test
```

---

*Desarrollado para modernizar la gestión de licencias de conducir*
