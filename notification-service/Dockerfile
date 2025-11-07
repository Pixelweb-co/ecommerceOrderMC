# ===== STAGE 1: Build =====
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos el pom del servicio y resolvemos dependencias
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copiamos el código fuente del servicio
COPY src ./src

# IMPORTANTE: generar el jar ejecutable de Spring Boot
RUN mvn -q -DskipTests package spring-boot:repackage


# ===== STAGE 2: Runtime =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiamos el boot jar (ejecutable) generado en target
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
