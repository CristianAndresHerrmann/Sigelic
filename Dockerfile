# ==========================================
# Fase 1: Compilación de la aplicación
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el descriptor del proyecto y wrappers de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copiar el código fuente de la aplicación
COPY src src

# Otorgar permisos de ejecución al script mvnw y compilar la aplicación.
# Se utiliza el perfil 'production' para compilar el frontend optimizado de Vaadin.
RUN chmod +x mvnw && ./mvnw clean package -Pproduction -DskipTests

# ==========================================
# Fase 2: Imagen final de ejecución (liviana)
# ==========================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el JAR empaquetado desde la fase de build
COPY --from=build /app/target/sigelic-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto por defecto (8080)
EXPOSE 8080

# Ajustes de la JVM para optimizar consumo de RAM en entornos acotados:
# -XX:+UseSerialGC: reduce la sobrecarga de memoria del Garbage Collector.
# -Xss256k: disminuye la memoria reservada por hilo de ejecución.
# -XX:MaxRAMPercentage=75.0: reserva un 75% del contenedor como límite máximo para la JVM.
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xss256k", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
