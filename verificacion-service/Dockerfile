# ---- Stage 1: build ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY verificacion-service/pom.xml ./verificacion-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f verificacion-service/pom.xml dependency:go-offline

COPY verificacion-service/src ./verificacion-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f verificacion-service/pom.xml clean package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/verificacion-service/target/*.jar app.jar

EXPOSE 9094
ENTRYPOINT ["java", "-jar", "app.jar"]
