# vehicle-service

Microservicio de gestión de vehículos del sistema **Rentar**. Expone un contrato **gRPC** para el alta, modificación, baja lógica, consulta y búsqueda de disponibilidad de vehículos, persistiendo en MySQL y comunicándose de forma asincrónica mediante **Apache Kafka** para la verificación de vehículos (detección de vehículos robados / inhabilitados).

---

## Descripción general

`vehicle-service` es responsable del ciclo de vida de los vehículos (ABM y consultas). Su interfaz principal de integración sincrónica es gRPC (consumido por el API Gateway y otros microservicios a través del contrato definido en `proto/vehicle.proto`), y cuenta con integración asincrónica mediante eventos de Kafka.

Responsabilidades:
- Crear, actualizar, dar de baja lógica y consultar vehículos.
- Actualizar el estado operativo de un vehículo (`DISPONIBLE`, `RESERVADO`, `EN_ALQUILER`).
- Filtrar disponibilidad por tipo, marca, modelo y rango de precio (excluyendo vehículos inhabilitados o dados de baja).
- Verificación asincrónica de patentes e inhabilitación en caso de robo (vía Kafka con `verificacion-service`).
- Servir lecturas de gestión y por `id` o `patente`.

---

## Arquitectura y componentes

El servicio sigue una arquitectura en capas con separación de responsabilidades y comunicación orientada a eventos:

```
gRPC (VehicleGrpcServiceImpl) ──► Service (VehiculoServiceImpl) ──► Repository (Spring Data JPA) ──► MySQL
                                        │
                                        ├─► Publica evento Kafka (`vehiculos.verificacion.solicitada`)
                                        │
Kafka Listener (VehiculoVerificacionConsumer) ◄─ Escucha resultado (`vehiculos.verificacion.resultado`)
```

Componentes principales:

| Componente | Rol |
|-----| --- |
| `grpc/VehicleGrpcServiceImpl` | Endpoint gRPC; adapta proto ↔ DTO, invoca validación y delega en el servicio |
| `grpc/mapper/VehiculoGrpcMapper` | Traducción entre mensajes protobuf y DTOs de dominio (incluye enums y campo inhabilitado) |
| `grpc/validation/RequestValidator` | Validación Bean Validation sobre los DTOs antes de la lógica de negocio |
| `validation/Patente` / `PatenteValidator` | Validación de patentes con formato viejo y Mercosur |
| `validation/PatenteNormalizer` | Normalización de patentes (mayúsculas, sin espacios ni guiones) |
| `service/IVehiculoService` + `impl/VehiculoServiceImpl` | Reglas de negocio, transaccionalidad y disparo de eventos Kafka |
| `kafka/producer/VehiculoVerificacionProducer` | Publicador de eventos de solicitud de verificación de vehículos |
| `kafka/consumer/VehiculoVerificacionConsumer` | Consumidor de resultados de verificación para actualizar el estado `inhabilitado` |
| `repository/IVehiculoRepository` | Acceso a datos (JPA + consultas de disponibilidad y actualización atómica de inhabilitación) |
| `model/Vehiculo` | Entidad JPA mapeada a la tabla `vehiculos` |
| `mapper/VehiculoMapper` | Traducción entre DTOs y entidad |
| `config/GrpcSecurityConfig` | Seguridad/autorización de los RPC |
| `exception/*` | Manejo de errores gRPC (`GrpcExceptionAdvice`) y excepciones de dominio (`custom/`) |

---

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.1** (Maven)
- **Spring Data JPA / Hibernate** - persistencia
- **MySQL** (`mysql-connector-j`) - base de datos
- **Apache Kafka** (`spring-boot-starter-kafka`) - mensajería asincrónica
- **gRPC** (`spring-boot-starter-grpc-server`) - interfaz expuesta
- **Protocol Buffers** - contrato y generación de stubs (`io.github.ascopes:protobuf-maven-plugin`)
- **Spring Security** - autorización de RPC por rol
- **Bean Validation** (`spring-boot-starter-validation`) - validación de entrada
- **Lombok** - reducción de boilerplate
- **spring-dotenv** - carga de variables desde `.env`

---

## Estructura del proyecto

```
vehicle-service/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/rentar/vehicle_service/
    │   │   ├── VehicleServiceApplication.java
    │   │   ├── config/          # GrpcSecurityConfig, KafkaConsumerConfig y KafkaProducerConfig
    │   │   ├── dto/             # request/ y response/
    │   │   ├── enums/           # TipoVehiculo, EstadoVehiculo
    │   │   ├── exception/       # GrpcExceptionAdvice + custom/
    │   │   ├── grpc/            # VehicleGrpcServiceImpl, mapper, validation
    │   │   ├── kafka/           # producer/, consumer/, event/
    │   │   ├── mapper/          # VehiculoMapper
    │   │   ├── model/           # Vehiculo (entidad)
    │   │   ├── repository/      # IVehiculoRepository
    │   │   ├── service/         # IVehiculoService + impl
    │   │   └── validation/      # Patente, PatenteValidator, PatenteNormalizer
    │   └── resources/
    │       ├── application.properties
    │       └── data.sql         # datos iniciales (seed)
    └── test/java/com/rentar/vehicle_service/
        ├── kafka/               # Tests de consumidores/productores Kafka
        ├── repository/          # Tests de repositorio JPA
        ├── service/             # Tests de lógica de negocio
        └── validation/          # Tests de validación y normalización de patentes
```

El contrato protobuf vive fuera del módulo, en `../proto/vehicle.proto`, y el plugin de Maven genera las clases en `com.rentar.vehicle.grpc`.

---

## Requisitos previos

- **JDK 21**
- **MySQL 8** accesible (o el `docker-compose` del repositorio raíz)
- **Apache Kafka** (KRaft broker para mensajería asincrónica)
- No requiere Maven instalado: se incluye el wrapper (`mvnw` / `mvnw.cmd`)

---

## Configuración y variables

La configuración se resuelve combinando `application.properties` con variables de entorno. No hardcodear credenciales.

Variables consumidas por el servicio:

| Variable | Descripción |
|----|----|
| `VEHICLE_SERVER_PORT` | Puerto del servidor HTTP embebido |
| `GRPC_VEHICLE_SERVER_PORT` | Puerto del servidor gRPC |
| `DB_HOST` | Host de MySQL |
| `DB_PORT` | Puerto de MySQL |
| `VEHICLE_DB_NAME` | Nombre de la base de datos |
| `DB_USERNAME` | Usuario de MySQL |
| `DB_PASSWORD` | Contraseña de MySQL |
| `KAFKA_BOOTSTRAP_SERVERS` | Servidores bootstrap de Kafka (ej. `localhost:9092`) |
| `KAFKA_VERIFICACION_GROUP_ID` | ID de grupo de consumidores para verificación de vehículos |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia Hibernate (`update`, `create`, etc.) |

Configuración relevante (`application.properties`):

- `spring.sql.init.mode=always` + `spring.jpa.defer-datasource-initialization=true`: ejecuta `data.sql` en cada arranque (usa `INSERT IGNORE`, segura de repetir).
- `spring.jpa.show-sql=true` y `format_sql=true`: SQL formateado en logs.
- Dialecto MySQL.

---

## Instalación y ejecución

1. **Generar stubs y compilar** (el plugin de protobuf genera desde `../proto`):

   ```bash
   ./mvnw clean package
   ```

2. **Levantar dependencias** (MySQL y Kafka) — utilizando el compose del repositorio raíz:

   ```bash
   docker compose up -d mysql-db kafka
   ```

3. **Ejecutar el servicio**:

   ```bash
   ./mvnw spring-boot:run
   ```

   En Windows PowerShell: `.\mvnw.cmd spring-boot:run`

Al arrancar, Hibernate crea/actualiza el esquema y `data.sql` carga vehículos de ejemplo iniciales.

---

## Integración Asincrónica (Kafka)

`vehicle-service` participa en un flujo asincrónico junto con `verificacion-service`:

1. **Solicitud de Verificación (`vehiculos.verificacion.solicitada`)**:
   - Al registrar exitosamente un nuevo vehículo (`crearVehiculo`), se publica un evento `VerificacionVehiculoEvent` con el ID y la patente del vehículo.
2. **Resultado de Verificación (`vehiculos.verificacion.resultado`)**:
   - `verificacion-service` procesa la patente y emite un `VerificacionResultadoEvent`.
   - `VehiculoVerificacionConsumer` procesa el resultado:
     - Si el resultado es `ROBADO`, se actualiza de manera atómica el campo `inhabilitado` a `true`.
     - Si el resultado es `VALIDADO`, se asegura que `inhabilitado` sea `false`.

---

## Funciones RPC

Servicio `rentar.vehicle.VehicleService`:

| RPC | Request → Response | Autorización |
| --- | --- | --- |
| `CrearVehiculo` | `CrearVehiculoRequest` → `Vehicle` | `ADMINISTRADOR` |
| `ActualizarVehiculo` | `ActualizarVehiculoRequest` → `Vehicle` | `ADMINISTRADOR` |
| `ActualizarEstadoVehiculo` | `ActualizarEstadoRequest` → `Vehicle` | `CLIENTE`, `ADMINISTRADOR` |
| `EliminarVehiculo` | `EliminarVehiculoRequest` → `EliminarVehiculoResponse` | `ADMINISTRADOR` |
| `GetVehiculos` | `Empty` → `VehicleList` | `CLIENTE`, `ADMINISTRADOR` |
| `GetVehiculosActivos` | `Empty` → `VehicleList` | `ADMINISTRADOR` |
| `GetVehiculoById` | `GetVehiculoByIdRequest` → `Vehicle` | `CLIENTE`, `ADMINISTRADOR` |
| `GetVehiculoByPatente` | `GetVehiculoByPatenteRequest` → `Vehicle` | `ADMINISTRADOR` |
| `ConsultarDisponibilidad` | `FiltroDisponibilidad` → `VehicleList` | Cualquier identidad autenticada |

Tipos relevantes:
- **`Vehicle`**: `idVehiculo`, `patente`, `marca`, `modelo`, `anio`, `color`, `tipoVehiculo`, `precioDiario`, `estado`, `activo`, `inhabilitado`.
- **`TipoVehiculo`**: `SEDAN`, `SUV`, `PICKUP`, `COUPE`, `HATCHBACK`.
- **`EstadoVehiculo`**: `DISPONIBLE`, `RESERVADO`, `EN_ALQUILER`.
- **`FiltroDisponibilidad`**: todos los campos opcionales (`tipoVehiculo`, `marca`, `modelo`, `precioMin`, `precioMax`).
- El campo `activo` permite al Gateway distinguir entre recurso inexistente (404) e inactivo (400).

---

## Seguridad y autenticación

La seguridad se centraliza en `GrpcSecurityConfig` mediante un `AuthenticationProcessInterceptor` global:

- **Este servicio NO revalida el JWT.** La validación de la firma la realiza la **API Gateway**; `vehicle-service` solo consume el resultado.
- El Gateway inyecta identidad en la metadata gRPC:
  - `roles`: cadena separada por comas, p. ej. `CLIENTE` o `CLIENTE,ADMINISTRADOR`.
  - `sub`: email del solicitante.
- Si `roles` falta o está vacío, la llamada se trata como anónima y es rechazada por `denyAll()`.
- Autorización **deny-by-default**: las reglas se evalúan de específica a general y cualquier RPC sin regla explícita se rechaza.
- `grpc.*/*` (reflection/healthcheck) queda permitido para permitir `grpcurl list`.

---

## Manejo de errores

**gRPC** (`GrpcExceptionAdvice`) traduce excepciones de dominio a estados gRPC:

| Excepción | Estado gRPC |
| --- | --- |
| `ResourceNotFoundException` | `NOT_FOUND` |
| `ValidationException` | `INVALID_ARGUMENT` |
| `BadRequestException` | `INVALID_ARGUMENT` |
| `ConflictException` | `ALREADY_EXISTS` |
| `IllegalArgumentException` | `INVALID_ARGUMENT` |
| `StatusRuntimeException` / `StatusException` | Respeta el estado lanzado |
| `Exception` | `INTERNAL` |

---

## Pruebas

El servicio incluye una suite completa de pruebas unitarias e de integración ubicadas en `src/test`:
- Pruebas de validación y normalización de patentes (`PatenteValidatorTest`).
- Pruebas de repositorio JPA (`VehiculoRepositoryTest`).
- Pruebas de lógica de negocio y servicios (`VehiculoServiceImplTest`).
- Pruebas de consumidores/productores de Kafka (`VehiculoVerificacionConsumerTest`).

Para ejecutar los tests:
```bash
./mvnw test
```
