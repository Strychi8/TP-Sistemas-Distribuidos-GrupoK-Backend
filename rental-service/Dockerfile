# ---- Stage 1: build ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# pom.xml excluye vehicle+customer: solo rental.proto es necesario,
# y proto/ debe estar como hermano del directorio del servicio.
COPY proto/rental.proto ./proto/rental.proto
COPY rental-service/pom.xml ./rental-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f rental-service/pom.xml dependency:go-offline

# Copiar el código fuente del servicio + caché de dependencias de Maven para acelerar la construcción
COPY rental-service/src ./rental-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f rental-service/pom.xml clean package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/rental-service/target/*.jar app.jar
EXPOSE 9093
ENTRYPOINT ["java", "-jar", "app.jar"]