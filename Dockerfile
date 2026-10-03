# Etapa 1: Compilación con Maven y OpenJDK 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar Maven wrapper y pom.xml para cachear dependencias
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Descargar dependencias de Maven en caché
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar el JAR ejecutable
COPY src src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen ligera de ejecución (JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario y grupo no-root con ID explícito
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring

# Copiar el JAR con propiedad del usuario no privilegiado
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Establecer usuario no privilegiado
USER spring:spring

# Exponer el puerto
EXPOSE 8080

# Healthcheck de seguridad para verificar disponibilidad del servicio
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/api/health || exit 1

# Ejecutar con soporte para cgroups de contenedores y memoria controlada
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8080} -jar app.jar"]
