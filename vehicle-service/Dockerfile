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

ARG TARGETARCH
# grpcurl es requerido por el healthcheck de compose
RUN case "${TARGETARCH}" in \
      amd64) GRPCURL_ARCH=x86_64 ;; \
      arm64) GRPCURL_ARCH=arm64 ;; \
      *) echo "unsupported arch: ${TARGETARCH}"; exit 1 ;; \
    esac \
 && apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates \
 && curl -fsSL "https://github.com/fullstorydev/grpcurl/releases/download/v1.9.3/grpcurl_1.9.3_linux_${GRPCURL_ARCH}.tar.gz" \
    | tar -xz -C /usr/local/bin grpcurl \
 && apt-get purge -y curl \
 && apt-get autoremove -y \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/vehicle-service/target/*.jar app.jar
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar"]