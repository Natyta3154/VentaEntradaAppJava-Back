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

# Etapa 2: Imagen ligera de ejecución (JRE 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear un usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Ejecutar la aplicación inyectando el puerto dinámico de Render
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
