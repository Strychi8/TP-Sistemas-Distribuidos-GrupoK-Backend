# ---- Stage 1: build ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY mantenimiento-service/pom.xml ./mantenimiento-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f mantenimiento-service/pom.xml dependency:go-offline

COPY mantenimiento-service/src ./mantenimiento-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -f mantenimiento-service/pom.xml clean package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/mantenimiento-service/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT["java", "-jar", "app.jar"]