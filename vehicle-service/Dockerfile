# ---- Stage 1: build ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# pom.xml utiliza <source>${project.basedir}/../proto</source> y excluye rental.proto y customer.proto, por lo que:
# solo vehicle.proto es necesario, y proto/ debe estar como hermano del directorio del servicio.
COPY proto/vehicle.proto ./proto/vehicle.proto
COPY vehicle-service/pom.xml ./vehicle-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f vehicle-service/pom.xml dependency:go-offline

# Copiar el código fuente del servicio + caché de dependencias de Maven para acelerar la construcción
COPY vehicle-service/src ./vehicle-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f vehicle-service/pom.xml clean package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/vehicle-service/target/*.jar app.jar
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar"]