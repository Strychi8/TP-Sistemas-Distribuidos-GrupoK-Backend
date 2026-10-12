# ---- Stage 1: build ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# pom.xml ahora excluye rental+vehicle: solo customer.proto es necesario,
# y proto/ debe estar como hermano del directorio del servicio.
COPY proto/customer.proto ./proto/customer.proto
COPY customer-service/pom.xml ./customer-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f customer-service/pom.xml dependency:go-offline

# Copiar el código fuente del servicio + caché de dependencias de Maven para acelerar la construcción
COPY customer-service/src ./customer-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f customer-service/pom.xml clean package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/customer-service/target/*.jar app.jar
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "app.jar"]