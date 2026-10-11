# vehicle-service

Microservicio de gestión de vehículos del sistema **Rentar**. Expone un contrato **gRPC** para el alta, modificación, baja lógica, consulta y búsqueda de disponibilidad de vehículos, persistiendo en MySQL.

---

## Descripción general

`vehicle-service` es responsable del ciclo de vida de los vehículos (ABM y consultas). Su única interfaz de integración implementada es gRPC: el API Gateway y los demás microservicios lo consumen a través del contrato definido en `proto/vehicle.proto`.

Responsabilidades:

- Crear, actualizar, dar de baja lógica y consultar vehículos.
- Actualizar el estado operativo de un vehículo (`DISPONIBLE`, `RESERVADO`, `EN_ALQUILER`).
- Filtrar disponibilidad por tipo, marca, modelo y rango de precio.
- Servir lecturas de gestión (todos / solo activos) y por `id` o `patente`.

---

## Arquitectura y componentes

El servicio sigue una arquitectura en capas con separación de responsabilidades:

```
gRPC (VehicleGrpcServiceImpl)
   │  mapea proto <-> DTO, valida, autoriza
   ▼
Service (IVehiculoService / VehiculoServiceImpl)
   │  reglas de negocio y transacciones
   ▼
Repository (IVehiculoRepository - Spring Data JPA)
   ▼
Model (Vehiculo)  →  MySQL (tabla `vehiculos`)
```

Componentes principales:

| Componente | Rol |
|-----| --- |
| `grpc/VehicleGrpcServiceImpl` | Endpoint gRPC; adapta proto ↔ DTO, invoca validación y delega en el servicio |
| `grpc/mapper/VehiculoGrpcMapper` | Traducción entre mensajes protobuf y DTOs de dominio (incluye enums) |
| `grpc/validation/RequestValidator` | Validación Bean Validation sobre los DTOs antes de la lógica de negocio |
| `service/IVehiculoService` + `impl/VehiculoServiceImpl` | Reglas de negocio y transaccionalidad |
| `repository/IVehiculoRepository` | Acceso a datos (JPA + query de filtros) |
| `model/Vehiculo` | Entidad JPA mapeada a la tabla `vehiculos` |
| `mapper/VehiculoMapper` | Traducción entre DTOs y entidad |
| `config/GrpcSecurityConfig` | Seguridad/autorización de los RPC |
| `exception/*` | Manejo de errores gRPC (`GrpcExceptionAdvice`) y excepciones de dominio (`custom/`) |

---

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.1** (Maven)
- **Spring Data JPA / Hibernate** — persistencia
- **MySQL** (`mysql-connector-j`) — base de datos
- **gRPC** (`spring-boot-starter-grpc-server`) — interfaz expuesta
- **Protocol Buffers** — contrato y generación de stubs (`io.github.ascopes:protobuf-maven-plugin`)
- **Spring Security** — autorización de RPC por rol
- **Bean Validation** (`spring-boot-starter-validation`) — validación de entrada
- **Lombok** — reducción de boilerplate
- **spring-dotenv** — carga de variables desde `.env`

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
    │   │   ├── config/          # GrpcSecurityConfig
    │   │   ├── dto/             # request/ y response/
    │   │   ├── enums/           # TipoVehiculo, EstadoVehiculo
    │   │   ├── exception/       # GrpcExceptionAdvice + custom/
    │   │   ├── grpc/            # VehicleGrpcServiceImpl, mapper, validation
    │   │   ├── mapper/          # VehiculoMapper
    │   │   ├── model/           # Vehiculo (entidad)
    │   │   ├── repository/      # IVehiculoRepository
    │   │   └── service/         # IVehiculoService + impl
    │   └── resources/
    │       ├── application.properties
    │       └── data.sql         # datos iniciales (seed)
    └── test/java/com/rentar/vehicle_service/
        └── VehicleServiceApplicationTests.java
```

El contrato protobuf vive fuera del módulo, en `../proto/vehicle.proto`, y el plugin de Maven genera las clases en `com.rentar.vehicle.grpc`.

---

## Requisitos previos

- **JDK 21**
- **MySQL 8** accesible (o el `docker-compose` del repositorio raíz)
- No requiere Maven instalado: se incluye el wrapper (`mvnw` / `mvnw.cmd`)

---

## Configuración y variables

La configuración se resuelve combinando `application.properties` con variables de entorno (cargadas desde `.env` mediante `spring-dotenv`). No hardcodear credenciales.

Variables consumidas por el servicio:

| Variable | Descripción |
|----|----|
| `GRPC_VEHICLE_SERVER_PORT` | Puerto del servidor gRPC |
| `DB_HOST` | Host de MySQL |
| `DB_PORT` | Puerto de MySQL |
| `VEHICLE_DB_NAME` | Nombre de la base de datos |
| `DB_USERNAME` | Usuario de MySQL |
| `DB_PASSWORD` | Contraseña de MySQL |
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

2. **Levantar dependencias** (MySQL) — por ejemplo con el compose del repositorio raíz:

   ```bash
   docker compose up -d mysql-db
   ```

3. **Ejecutar el servicio**:

   ```bash
   ./mvnw spring-boot:run
   ```

   En Windows PowerShell: `.\mvnw.cmd spring-boot:run`

Al arrancar, Hibernate crea/actualiza el esquema y `data.sql` carga 5 vehículos de ejemplo.

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

- **`Vehicle`**: `idVehiculo`, `patente`, `marca`, `modelo`, `anio`, `color`, `tipoVehiculo`, `precioDiario`, `estado`, `activo`.
- **`TipoVehiculo`**: `SEDAN`, `SUV`, `PICKUP`, `COUPE`, `HATCHBACK`.
- **`EstadoVehiculo`**: `DISPONIBLE`, `RESERVADO`, `EN_ALQUILER`.
- **`FiltroDisponibilidad`**: todos los campos opcionales (`tipoVehiculo`, `marca`, `modelo`, `precioMin`, `precioMax`).
- El campo `activo` permite al Gateway distinguir entre recurso inexistente (404) e inactivo (400).

---

## Seguridad y autenticación

La seguridad se centraliza en `GrpcSecurityConfig` mediante un `AuthenticationProcessInterceptor` global:

- **Este servicio NO revalida el JWT.** La validación de la firma la realiza el **API Gateway**; `vehicle-service` solo consume el resultado.
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

## Pruebas manuales

El contrato puede ejercitarse con cualquier cliente gRPC. A continuación se listan payloads listos para copiar, junto con la metadata y la respuesta esperada de cada RPC.

> Los RPC de escritura (`CrearVehiculo`, `ActualizarVehiculo`, `ActualizarEstadoVehiculo`, `EliminarVehiculo`) modifican datos en la base. Los de lectura y los casos de error no alteran el estado.

### Preparación

- **Endpoint**: `localhost:<GRPC_VEHICLE_SERVER_PORT>` (en los ejemplos, `9091`).
- **Sin TLS**: las llamadas locales usan conexión en texto plano.
- **Reflection habilitado**: Spring gRPC lo activa por defecto (`spring.grpc.server.reflection.enabled`), por lo que el cliente descubre el contrato sin recibir el `.proto`. Si el cliente no usa reflection, importar `../proto/vehicle.proto`.
- **Metadata obligatoria** en cada llamada (pestaña *Metadata* en Postman; cabecera `-H` en grpcurl). Las claves van en minúscula:
  - `roles`: `ADMINISTRADOR` o `CLIENTE` (según el RPC).
  - `sub`: email del solicitante (opcional).
- **Enums**: se envían por nombre (`SEDAN`, `DISPONIBLE`, `EN_ALQUILER`, ...).
- **`google.protobuf.Empty`**: los RPC sin parámetros llevan payload `{}`.

Postman (cliente gRPC): request de tipo **gRPC** → URL `localhost:9091` → *Server reflection* → el JSON va en la pestaña **Message** y la metadata en **Metadata**.

grpcurl:

```bash
# Linux / macOS
grpcurl -plaintext -H "roles: CLIENTE" -d '{"idVehiculo":1}' \
  localhost:9091 rentar.vehicle.VehicleService/GetVehiculoById
```

```powershell
# Windows PowerShell: el payload va por stdin para evitar problemas de comillas
'{"idVehiculo":1}' | grpcurl -plaintext -H "roles: CLIENTE" -d @ `
  localhost:9091 rentar.vehicle.VehicleService/GetVehiculoById
```

### Datos de ejemplo (seed)

`data.sql` carga los siguientes vehículos; sus `id` y `patente` son válidos para las consultas:

| id | patente | marca | modelo | anio | color | tipoVehiculo | estado | precioDiario |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | AD123ZZ | Toyota | Corolla | 2023 | rojo | SEDAN | DISPONIBLE | 50000.00 |
| 2 | AF456XY | Ford | Ranger | 2024 | blanco | SUV | DISPONIBLE | 60000.00 |
| 3 | AF123CC | Volkswagen | Gol Trend | 2021 | plateado | SEDAN | DISPONIBLE | 40000.00 |
| 4 | AG789WX | Honda | CR-V | 2022 | negro | SUV | DISPONIBLE | 130000.00 |
| 5 | AH123YZ | Renault | Sandero | 2020 | blanco | HATCHBACK | DISPONIBLE | 40000.00 |

### Payloads por RPC

#### CrearVehiculo — `roles: ADMINISTRADOR`

Request:

```json
{
  "patente": "AA123BB",
  "marca": "Chevrolet",
  "modelo": "Cruze",
  "anio": 2022,
  "color": "azul",
  "tipoVehiculo": "SEDAN",
  "precioDiario": 55000
}
```

Respuesta esperada (`Vehicle`) — `estado` inicial `DISPONIBLE`, `activo` `true`, `idVehiculo` autogenerado:

```json
{
  "idVehiculo": "6",
  "patente": "AA123BB",
  "marca": "Chevrolet",
  "modelo": "Cruze",
  "anio": 2022,
  "color": "azul",
  "tipoVehiculo": "SEDAN",
  "precioDiario": 55000,
  "estado": "DISPONIBLE",
  "activo": true
}
```

#### ActualizarVehiculo — `roles: ADMINISTRADOR`

Request (`patente` no se modifica):

```json
{
  "idVehiculo": 1,
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2023,
  "color": "rojo",
  "tipoVehiculo": "SEDAN",
  "precioDiario": 55000,
  "estado": "RESERVADO"
}
```

Respuesta esperada (`Vehicle`): misma `patente` e `id`, con los campos actualizados y el nuevo `estado`.

#### ActualizarEstadoVehiculo — `roles: CLIENTE` o `ADMINISTRADOR`

Request:

```json
{ "idVehiculo": 1, "estado": "EN_ALQUILER" }
```

Respuesta esperada (`Vehicle`): el vehículo con el nuevo `estado`.

#### EliminarVehiculo — `roles: ADMINISTRADOR`

Request:

```json
{ "idVehiculo": 1 }
```

Respuesta esperada (`EliminarVehiculoResponse`):

```json
{ "success": true }
```

> Baja lógica: el vehículo queda con `activo = false`. Sigue apareciendo en `GetVehiculos` y desaparece de `GetVehiculosActivos`.

#### GetVehiculos — `roles: CLIENTE` o `ADMINISTRADOR`

Request: `{}`

Respuesta esperada (`VehicleList`): todos los vehículos (ver "Datos de ejemplo"). Sin resultados devuelve `{ "vehicles": [] }`.

#### GetVehiculosActivos — `roles: ADMINISTRADOR`

Request: `{}`

Respuesta esperada (`VehicleList`): solo los vehículos con `activo = true`.

#### GetVehiculoById — `roles: CLIENTE` o `ADMINISTRADOR`

Request:

```json
{ "idVehiculo": 1 }
```

Respuesta esperada (`Vehicle`): el vehículo 1 (Toyota Corolla).

#### GetVehiculoByPatente — `roles: ADMINISTRADOR`

Request:

```json
{ "patente": "AD123ZZ" }
```

Respuesta esperada (`Vehicle`): el vehículo con esa patente.

#### ConsultarDisponibilidad — cualquier identidad autenticada

Request (todos los campos son opcionales):

```json
{
  "tipoVehiculo": "SUV",
  "marca": "Honda",
  "modelo": "CR-V",
  "precioMin": 1000,
  "precioMax": 200000
}
```

Sin filtros, el payload `{}` devuelve todos los activos. Respuesta esperada (`VehicleList`): vehículos activos que cumplan los filtros.

### Casos de error esperados

| Situación | `roles` | Resultado |
| --- | --- | --- |
| `GetVehiculoById` con `id` inexistente | `CLIENTE` | `NOT_FOUND` — `Vehículo no encontrado con ID: <id>` |
| `GetVehiculoByPatente` con patente inexistente | `ADMINISTRADOR` | `NOT_FOUND` — `Vehículo no encontrado con patente: <patente>` |
| `CrearVehiculo` con patente ya existente | `ADMINISTRADOR` | `ALREADY_EXISTS` — `Ya existe un vehículo con la patente: <patente>` |
| `CrearVehiculo` con patente de formato inválido | `ADMINISTRADOR` | `INVALID_ARGUMENT` — `La patente debe tener el formato Mercosur (ej. AB123CD)` |
| Cualquier RPC sin `roles` o con rol insuficiente | — | La llamada es rechazada por la autorización (`denyAll`) |

---

## Decisiones técnicas

- **gRPC + Protocol Buffers como contrato único**: esquema tipado y stubs generados en build, compartido desde `../proto`. El `protobuf-maven-plugin` excluye `rental.proto` y `customer.proto` (pertenecen a otros servicios).
- **Validación**: Bean Validation (`RequestValidator`) sobre los DTOs, para rechazar entradas inválidas antes de tocar la base de datos.
- **Alta sin pre-chequeo de patente**: no se usa `existsByPatente()` para evitar condiciones de carrera; se inserta y se confía en la restricción `UNIQUE`. Una `DataIntegrityViolationException` se traduce a `ConflictException` (409 / `ALREADY_EXISTS`).
- **Baja lógica**: `EliminarVehiculo` marca `activo = false` en lugar de borrar filas, preservando historial y permitiendo distinguir inexistente vs. inactivo.
- **Estado inicial `DISPONIBLE`** en el alta; la patente es inmutable (`updatable = false`) y no se modifica en las actualizaciones.
- **Precio con `BigDecimal`** en el dominio para evitar errores de coma flotante; se convierte a `double` solo en la frontera protobuf.
- **Seguridad delegada al Gateway**: no se duplica la validación de JWT; el servicio confía en la metadata firmada por el Gateway, con autorización por rol por RPC.
