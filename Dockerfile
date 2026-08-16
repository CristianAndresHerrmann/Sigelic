# syntax=docker/dockerfile:1.7
# ==========================================
# Fase 1: Compilación de la aplicación
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Descriptor del proyecto, wrapper de Maven y código fuente
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

# Se utiliza el perfil 'production' para compilar el frontend optimizado de Vaadin.
# Los cache mounts de BuildKit reutilizan el repositorio Maven y el Node.js que
# descarga el plugin de Vaadin, de modo que un cambio de código no obliga a
# volver a bajarlo todo. Requiere BuildKit (por defecto desde Docker 23);
# si no estuviera disponible, quitar las dos líneas --mount.
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/root/.vaadin \
    chmod +x mvnw && ./mvnw -B clean package -Pproduction -DskipTests

# ==========================================
# Fase 2: Imagen final de ejecución (liviana)
# ==========================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# El @Scheduled y los logs deben usar la misma zona que la base de datos
ENV TZ=America/Argentina/Buenos_Aires

# Ajustes de JVM parametrizables, para adaptar la imagen al tamaño de la máquina
# sin reconstruirla. Valor por defecto pensado para la VM Ampere de OCI (12 GB).
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=50.0"

# curl es necesario para el HEALTHCHECK; el usuario sin privilegios corre la app
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tzdata \
 && rm -rf /var/lib/apt/lists/* \
 && useradd -r -u 1001 sigelic

# El nombre del JAR no se fija: sigue la versión declarada en el pom.xml
COPY --from=build --chown=sigelic:sigelic /app/target/*.jar app.jar

USER sigelic
EXPOSE 8080

# Requiere que /actuator/health esté abierto de forma anónima (ver SecurityConfig)
HEALTHCHECK --interval=30s --timeout=3s --start-period=120s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# 'exec' deja a java como PID 1, para que reciba el SIGTERM del apagado.
# El "$@" final reenvia los argumentos del contenedor a la aplicacion, de modo
# que sigue siendo posible sobrescribir propiedades:
#   docker run sigelic:prod --spring.profiles.active=prod
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar \"$@\"", "--"]
