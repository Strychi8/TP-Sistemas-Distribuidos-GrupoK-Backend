# Rental Service

`rental-service` es un microservicio del sistema Rentar, desarrollado en Java con Spring Boot, responsable de gestionar las reservas de alquiler de vehículos. Expone una interfaz gRPC y delega las reglas de negocio y el acceso a los datos en las capas de servicio y persistencia.

## 1. Descripción general

El servicio es un backend gRPC puro: su única interfaz de integración es gRPC. Sus principales funcionalidades son:

- Crear reservas de alquiler.
- Consultar reservas aplicando filtros.
- Obtener una reserva por su identificador.
- Cancelar reservas.
- Consultar el historial de alquileres de un cliente.
- Verificar el solapamiento de reservas confirmadas para un vehículo en un período determinado.

## 2. Arquitectura y componentes

El microservicio utiliza una arquitectura por capas:

- **Capa de comunicación gRPC:** `grpc/RentalGrpcServiceImpl` recibe las solicitudes y devuelve las respuestas. La traducción entre los mensajes Protocol Buffers y los DTOs del dominio se realiza en `grpc/mapper/ReservaGrpcMapper`.
- **Capa de servicios:** `IReservaService` y su implementación `ReservaServiceImpl` implementan las reglas de negocio, las validaciones y las operaciones sobre las reservas.
- **Capa de persistencia:** `IReservaRepository` (Spring Data JPA) consulta y almacena las reservas en la tabla `reservas`.
- **Capa de dominio y DTOs:** la entidad `Reserva`, los DTOs de entrada (`dto/request`) y de salida (`dto/response`).
- **Manejo de errores:** `exception/GrpcExceptionAdvice` traduce las excepciones de dominio a estados gRPC.
- **Seguridad:** `config/GrpcSecurityConfig` autoriza cada RPC según los roles propagados en la metadata.

El servicio gRPC no captura las excepciones de negocio con bloques `try/catch`: las excepciones se lanzan en la capa de servicios y suben hasta `GrpcExceptionAdvice`, que las convierte en estados gRPC.

## 3. Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.1** (Maven)
- **Spring gRPC** (`spring-boot-starter-grpc-server`) — servidor e interceptor de seguridad
- **Protocol Buffers** — contrato y generación de stubs vía `io.github.ascopes:protobuf-maven-plugin`
- **Spring Data JPA / Hibernate** — persistencia
- **MySQL** (`mysql-connector-j`) — base de datos
- **Spring Security** — autorización de RPC por rol
- **Bean Validation** (`spring-boot-starter-validation`) — validación de entrada
- **Lombok** — reducción de boilerplate
- **spring-dotenv** — carga de variables de entorno desde `.env`

## 4. Estructura del proyecto

Estructura principal del microservicio:

```text
rental-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/rentar/rental_service/
│   │   │       ├── RentalServiceApplication.java
│   │   │       ├── config/            # GrpcSecurityConfig
│   │   │       ├── dto/
│   │   │       │   ├── request/       # ReservaRequestDTO, FiltroReservaDTO
│   │   │       │   └── response/      # ReservaResponseDTO
│   │   │       ├── enums/             # EstadoReserva
│   │   │       ├── exception/         # GrpcExceptionAdvice + custom/
│   │   │       ├── grpc/              # RentalGrpcServiceImpl + mapper/
│   │   │       ├── mapper/            # ReservaMapper
│   │   │       ├── model/             # Reserva (entidad)
│   │   │       ├── repository/        # IReservaRepository
│   │   │       └── service/           # IReservaService + impl/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql               # datos iniciales (seed)
│   └── test/
├── pom.xml
└── README.md
```

La definición del contrato gRPC se encuentra en `proto/rental.proto`, en el directorio `proto` del repositorio. El plugin de Maven genera las clases en el paquete `com.rentar.rental.grpc` y excluye `vehicle.proto` y `customer.proto` (pertenecen a otros servicios).

## 5. Requisitos previos

Para ejecutar el microservicio se requiere:

- **JDK 21**.
- **MySQL 8** accesible (o el servicio `mysql-db` del `docker-compose` del repositorio raíz).
- Un cliente compatible con gRPC, como Postman, `grpcurl` o Docker.
- Git para obtener el código fuente.

No hace falta tener Maven instalado: se incluye el wrapper (`mvnw` / `mvnw.cmd`).

## 6. Configuración y variables de entorno

La configuración de la aplicación se encuentra en `src/main/resources/application.properties` y combina valores por defecto con variables de entorno (cargadas desde `.env` mediante `spring-dotenv`). No se deben hardcodear credenciales.

Variables identificadas en la configuración:

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `GRPC_RENTAL_SERVER_PORT` | Puerto del servidor gRPC. | `9093` |
| `DB_HOST` | Host de la base de datos MySQL. | `localhost` |
| `DB_PORT` | Puerto de MySQL. | `3306` |
| `RENTAL_DB_NAME` | Nombre de la base de datos de reservas. | `rentar_rental_db` |
| `DB_USERNAME` | Usuario de MySQL. | `root` |
| `DB_PASSWORD` | Contraseña de MySQL. | `root` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de Hibernate (`update`, `create`, etc.). | `update` |

Configuración relevante (`application.properties`):

- `spring.config.import=optional:file:.env` — carga las variables definidas en el archivo `.env`.
- `spring.sql.init.mode=always` junto con `spring.jpa.defer-datasource-initialization=true` — ejecuta `data.sql` en cada arranque para cargar los datos de ejemplo.
- `spring.jpa.show-sql=true` y `hibernate.format_sql=true` — muestra el SQL formateado en los registros.
- `spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect` — dialecto de MySQL.

Las variables deben definirse en el archivo `.env` correspondiente al entorno de ejecución. Si se utiliza Docker Compose, hay que verificar qué variables corresponden al contenedor y cuáles consume directamente Spring Boot.

## 7. Instalación y ejecución

### 7.1. Obtener el código fuente

Clonar el repositorio y ubicarse en el módulo:

```bash
git clone <URL_DEL_REPOSITORIO>
cd TP-Sistemas-Distribuidos-GrupoK/rental-service
```

### 7.2. Configurar el entorno

Crear o completar el archivo `.env` y verificar que la configuración de MySQL coincida con la utilizada por el microservicio. Si se utiliza el entorno de contenedores del proyecto, levantar únicamente la dependencia de base de datos:

```bash
docker compose up -d mysql-db
```

### 7.3. Compilar el proyecto

Desde el directorio `rental-service/` (donde se encuentran el wrapper y el `pom.xml`), ejecutar:

```bash
.\mvnw.cmd clean package
```

En Linux o macOS: `./mvnw clean package`. El plugin de protobuf genera los stubs desde `../proto`.

### 7.4. Ejecutar el microservicio

Iniciar el servicio con el wrapper:

```bash
.\mvnw.cmd spring-boot:run
```

Al arrancar, Hibernate crea o actualiza el esquema y `data.sql` carga las reservas de ejemplo. Verificar en los registros que el servidor gRPC esté escuchando en el puerto configurado (por defecto `9093`).

El módulo se empaqueta como una aplicación JVM estándar mediante `spring-boot-maven-plugin`; no se configura una imagen nativa (GraalVM).

## 8. Funciones RPC disponibles

El contrato definido en `proto/rental.proto` expone las siguientes operaciones:

| RPC | Request → Response | Autorización |
|---|---|---|
| `CrearReserva` | `CrearReservaRequest` → `Reservation` | `CLIENTE` |
| `ConsultarReservas` | `FiltroReserva` → `ReservationList` | `CLIENTE` o `ADMINISTRADOR` |
| `GetReservaById` | `GetReservaByIdRequest` → `Reservation` | `CLIENTE` |
| `CancelarReserva` | `CancelarReservaRequest` → `Reservation` | `CLIENTE` |
| `ConsultarHistorialAlquileres` | `ConsultarHistorialRequest` → `RentalHistoryList` | `CLIENTE` |
| `VerificarSolapamientoVehiculo` | `SolapamientoRequest` → `SolapamientoResponse` | Cualquier identidad autenticada |

Tipos relevantes:

- **`Reservation`**: `idReserva`, `idCliente`, `idVehiculo`, `fechaInicio`, `fechaFin`, `importeTotal`, `precioDiario`, `estado`.
- **`EstadoReserva`**: `CONFIRMADA`, `CANCELADA`, `FINALIZADA`.
- **`FiltroReserva`**: `idCliente`, `idVehiculo`, `estado`, `fechaDesde`, `fechaHasta` (todos opcionales).
- **`CrearReservaRequest`**: `idCliente`, `idVehiculo`, `fechaInicio`, `fechaFin`, `precioDiario`, `importeTotal`.
- El campo `importeTotal` de `CrearReservaRequest` se ignora: el servicio recalcula el importe como `precioDiario * cantidad de días`.
- **`SolapamientoRequest`**: `idVehiculo`, `fechaInicio`, `fechaFin`. **`SolapamientoResponse`**: `solapado` (booleano).

Las operaciones aplican las validaciones de la lógica de negocio. Por ejemplo, la creación de reservas valida que las fechas sean futuras y que no exista solapamiento con una reserva confirmada, mientras que la cancelación verifica que la reserva pueda cancelarse.

## 9. Seguridad y autenticación

La seguridad se centraliza en `GrpcSecurityConfig` mediante un interceptor global (`AuthenticationProcessInterceptor`):

- **El servicio no revalida el JWT.** La validación de la firma la realiza el API Gateway; `rental-service` solo consume el resultado.
- El Gateway inyecta la identidad en la metadata gRPC. Las claves van en minúscula:
  - `roles`: cadena separada por comas, por ejemplo `CLIENTE` o `CLIENTE,ADMINISTRADOR`.
  - `sub`: email del solicitante.
- Si `roles` falta o está vacío, la llamada se trata como anónima y es rechazada.
- Autorización **deny-by-default**: cualquier RPC sin regla explícita se rechaza.
- `grpc.*/*` (reflection y healthcheck) queda permitido, lo que habilita `grpcurl list` y el descubrimiento por reflection en Postman.

Las reglas por RPC se detallan en la tabla de la sección 8.

## 10. Manejo de errores

Las excepciones de dominio se lanzan en `ReservaServiceImpl` y suben hasta `GrpcExceptionAdvice`, que las traduce a estados gRPC:

| Excepción | Estado gRPC | Mensaje de ejemplo |
|---|---|---|
| `ResourceNotFoundException` | `NOT_FOUND` | `Reserva no encontrada con ID: 999` |
| `BadRequestException` | `INVALID_ARGUMENT` | `La fecha de inicio debe ser futura` |
| `IllegalArgumentException` | `INVALID_ARGUMENT` | Argumento inválido |
| `ConflictException` | `ALREADY_EXISTS` | `El vehículo ya tiene una reserva confirmada en ese período` |
| `BusinessException` | `FAILED_PRECONDITION` | `No se puede cancelar una reserva cancelada o finalizada` |
| `StatusRuntimeException` / `StatusException` | Respeta el estado lanzado | — |
| Cualquier otra excepción | `INTERNAL` | `Error interno del servicio - RentalService gRPC` |

Entre las situaciones que generan errores se encuentran:

- Fechas de reserva inválidas o no futuras.
- Precio diario nulo o menor o igual a cero.
- Intentos de cancelar reservas canceladas o finalizadas.
- Reservas que no pueden cancelarse porque su período ya comenzó.
- Conflictos por solapamiento con reservas confirmadas.
- Identificadores de reservas inexistentes.

## 11. Pruebas manuales

El contrato puede ejercitarse con cualquier cliente gRPC. A continuación se listan payloads listos para copiar, junto con la metadata y la respuesta esperada de cada RPC.

> Los RPC de escritura (`CrearReserva`, `CancelarReserva`) modifican datos en la base. Los de lectura y los casos de error no alteran el estado.

### 11.1. Preparación

- **Endpoint**: `localhost:<GRPC_RENTAL_SERVER_PORT>` (en los ejemplos, `9093`).
- **Sin TLS**: las llamadas locales usan conexión en texto plano.
- **Reflection habilitado**: Spring gRPC lo activa por defecto, por lo que el cliente descubre el contrato sin recibir el `.proto`. Si el cliente no usa reflection, importar `../proto/rental.proto`.
- **Metadata obligatoria** en cada llamada (pestaña *Metadata* en Postman; cabecera `-H` en grpcurl). Las claves van en minúscula:
  - `roles`: `CLIENTE` o `ADMINISTRADOR` según el RPC.
  - `sub`: email del solicitante (opcional).
- **Enums**: se envían por nombre (`CONFIRMADA`, `CANCELADA`, `FINALIZADA`).
- **Timestamps**: `fechaInicio`, `fechaFin`, `fechaDesde` y `fechaHasta` se envían en formato RFC3339, por ejemplo `"2026-11-15T10:00:00Z"`.

Postman (cliente gRPC): request de tipo **gRPC** → URL `localhost:9093` → habilitar *Server Reflection* (import service from reflection) → el JSON va en la pestaña **Message** y la metadata en **Metadata**.

grpcurl:

```bash
# Linux / macOS
grpcurl -plaintext -H "roles: CLIENTE" -d '{"idReserva":1}' \
  localhost:9093 rentar.rental.RentalService/GetReservaById
```

```powershell
# Windows PowerShell: el payload va por stdin para evitar problemas de comillas
'{"idReserva":1}' | grpcurl -plaintext -H "roles: CLIENTE" -d @ `
  localhost:9093 rentar.rental.RentalService/GetReservaById
```

### 11.2. Datos de ejemplo (seed)

`data.sql` carga las siguientes reservas; sus `idReserva` son válidos para las consultas. Los `idCliente` (1-5) y `idVehiculo` (1-5) corresponden a los datos iniciales de los servicios de clientes y vehículos.

| idReserva | idCliente | idVehiculo | fechaInicio | fechaFin | precioDiario | importeTotal | estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 1 | 2026-09-01T10:00:00Z | 2026-09-04T10:00:00Z | 50000 | 150000 | FINALIZADA |
| 2 | 2 | 2 | 2026-09-05T09:00:00Z | 2026-09-10T09:00:00Z | 60000 | 300000 | FINALIZADA |
| 3 | 3 | 3 | 2026-09-12T14:00:00Z | 2026-09-14T14:00:00Z | 40000 | 80000 | CANCELADA |
| 4 | 4 | 4 | 2026-09-15T11:00:00Z | 2026-09-19T11:00:00Z | 130000 | 520000 | FINALIZADA |
| 5 | 5 | 5 | 2026-09-20T08:00:00Z | 2026-09-23T08:00:00Z | 40000 | 120000 | FINALIZADA |
| 6 | 1 | 2 | 2026-10-10T10:00:00Z | 2026-10-14T10:00:00Z | 60000 | 240000 | CONFIRMADA |
| 7 | 2 | 1 | 2026-10-15T09:00:00Z | 2026-10-22T09:00:00Z | 50000 | 350000 | CONFIRMADA |
| 8 | 3 | 4 | 2026-10-20T12:00:00Z | 2026-10-23T12:00:00Z | 130000 | 390000 | CONFIRMADA |
| 9 | 4 | 3 | 2026-10-25T10:00:00Z | 2026-10-30T10:00:00Z | 40000 | 200000 | CONFIRMADA |
| 10 | 5 | 5 | 2026-10-28T14:00:00Z | 2026-10-30T14:00:00Z | 40000 | 80000 | CONFIRMADA |

> `CrearReserva` exige que `fechaInicio` sea futura. El seed usa fechas fijas de septiembre y octubre de 2026, por lo que para crear reservas conviene usar períodos posteriores a dichas fechas. El solapamiento solo se evalúa contra reservas en estado `CONFIRMADA`.

### 11.3. Ejemplos por RPC

#### GetReservaById — `roles: CLIENTE`

Request:

```json
{ "idReserva": 1 }
```

Respuesta esperada (`Reservation`), reserva 1 (FINALIZADA):

```json
{
  "idReserva": "1",
  "idCliente": "1",
  "idVehiculo": "1",
  "fechaInicio": "2026-09-01T10:00:00Z",
  "fechaFin": "2026-09-04T10:00:00Z",
  "importeTotal": 150000,
  "precioDiario": 50000,
  "estado": "FINALIZADA"
}
```

Caso de error: `{ "idReserva": 999 }` responde `NOT_FOUND` con el mensaje `Reserva no encontrada con ID: 999`.

#### ConsultarReservas — `roles: CLIENTE` o `ADMINISTRADOR`

Request (todos los campos del filtro son opcionales):

```json
{ "idCliente": 1 }
```

Respuesta esperada (`ReservationList`): las reservas 1 (FINALIZADA) y 6 (CONFIRMADA). Otros filtros admitidos son `idVehiculo`, `estado`, `fechaDesde` y `fechaHasta`. El filtro no admite `tipoVehiculo`.

Sin filtros, el payload `{}` devuelve todas las reservas.

#### CrearReserva — `roles: CLIENTE`

Request (caso exitoso, vehículo 3 sin solapamiento en el período indicado):

```json
{
  "idCliente": 1,
  "idVehiculo": 3,
  "fechaInicio": "2026-11-15T10:00:00Z",
  "fechaFin": "2026-11-18T10:00:00Z",
  "precioDiario": 40000,
  "importeTotal": 0
}
```

Respuesta esperada (`Reservation`): `idReserva` autogenerado, `estado` `CONFIRMADA` e `importeTotal` recalculado como 3 días x 40000 = 120000. El `importeTotal` enviado en la request (0) se ignora.

Caso de solapamiento: un período que se superponga con una reserva `CONFIRMADA` del mismo vehículo devuelve `ALREADY_EXISTS`. Por ejemplo, el vehículo 1 tiene la reserva 7 confirmada del `2026-10-15` al `2026-10-22`:

```json
{
  "idCliente": 1,
  "idVehiculo": 1,
  "fechaInicio": "2026-10-15T09:00:00Z",
  "fechaFin": "2026-10-22T09:00:00Z",
  "precioDiario": 50000,
  "importeTotal": 0
}
```

Resultado: `ALREADY_EXISTS` — `El vehículo ya tiene una reserva confirmada en ese período`.

#### CancelarReserva — `roles: CLIENTE`

Request (caso exitoso, reserva 8 en estado `CONFIRMADA` cuyo inicio es futuro):

```json
{ "idReserva": 8 }
```

Respuesta esperada (`Reservation`): la reserva 8 con `estado` `CANCELADA`.

Caso de error: cancelar una reserva `CANCELADA` (por ejemplo, la reserva 3) o `FINALIZADA` devuelve `FAILED_PRECONDITION` — `No se puede cancelar una reserva cancelada o finalizada`. Intentar cancelar una reserva cuyo período ya comenzó también devuelve `FAILED_PRECONDITION`.

#### ConsultarHistorialAlquileres — `roles: CLIENTE`

Request:

```json
{ "idCliente": 1 }
```

Respuesta esperada (`RentalHistoryList`): solo las reservas `FINALIZADA` o `CANCELADA` del cliente. Para el cliente 1, la reserva 1:

```json
{
  "history": [
    {
      "idVehiculo": "1",
      "fechaInicio": "2026-09-01T10:00:00Z",
      "fechaFinalizacion": "2026-09-04T10:00:00Z",
      "cantidadDias": 3,
      "importeTotal": 150000,
      "estado": "FINALIZADA"
    }
  ]
}
```

#### VerificarSolapamientoVehiculo — cualquier identidad autenticada

Request sin solapamiento (vehículo 1, período posterior a sus reservas confirmadas):

```json
{
  "idVehiculo": 1,
  "fechaInicio": "2026-12-01T10:00:00Z",
  "fechaFin": "2026-12-05T10:00:00Z"
}
```

Respuesta esperada (`SolapamientoResponse`):

```json
{ "solapado": false }
```

Request con solapamiento (vehículo 1, mismo período que la reserva confirmada 7):

```json
{
  "idVehiculo": 1,
  "fechaInicio": "2026-10-15T09:00:00Z",
  "fechaFin": "2026-10-22T09:00:00Z"
}
```

Respuesta esperada:

```json
{ "solapado": true }
```

> Este RPC informa el resultado mediante el booleano `solapado`; no devuelve un error de conflicto. El estado `ALREADY_EXISTS` se produce únicamente al intentar crear una reserva solapada.

### 11.4. Casos de error esperados

| Situación | RPC (roles) | Resultado |
| --- | --- | --- |
| Reserva inexistente | `GetReservaById` (`CLIENTE`) | `NOT_FOUND` — `Reserva no encontrada con ID: 999` |
| Solapamiento al crear | `CrearReserva` (`CLIENTE`) | `ALREADY_EXISTS` — `El vehículo ya tiene una reserva confirmada en ese período` |
| Cancelar reserva cancelada o finalizada | `CancelarReserva` (`CLIENTE`) | `FAILED_PRECONDITION` — `No se puede cancelar una reserva cancelada o finalizada` |
| Cancelar reserva ya iniciada | `CancelarReserva` (`CLIENTE`) | `FAILED_PRECONDITION` — `No se puede cancelar una reserva cuyo período ya comenzó` |
| Fecha de inicio no futura | `CrearReserva` (`CLIENTE`) | `INVALID_ARGUMENT` — `La fecha de inicio debe ser futura` |
| Fecha de fin anterior al inicio | `CrearReserva` (`CLIENTE`) | `INVALID_ARGUMENT` — `La fecha de fin debe ser posterior al inicio` |
| Precio diario menor o igual a cero | `CrearReserva` (`CLIENTE`) | `INVALID_ARGUMENT` — `El precio diario debe ser mayor que cero` |
| RPC sin `roles` o con rol insuficiente | Cualquier RPC | Rechazada por la autorización (`denyAll`) |

## 12. Decisiones técnicas

- **gRPC como única interfaz:** el servicio expone exclusivamente el contrato gRPC; no publica API REST.
- **Mapeo explícito proto ↔ dominio:** `ReservaGrpcMapper` traduce request y response entre los mensajes protobuf y los DTOs del dominio, incluyendo timestamps y enums.
- **Recálculo del importe:** el `importeTotal` de la request se ignora; el servicio lo calcula como `precioDiario * cantidad de días`.
- **Errores centralizados:** las excepciones de dominio se lanzan en la capa de servicios y se traducen en `GrpcExceptionAdvice`; el servicio gRPC no usa `try/catch`.
- **Seguridad delegada al Gateway:** no se revalida el JWT; el servicio confía en la metadata (`roles`, `sub`) con autorización por rol por RPC y deny-by-default.
- **Verificación de disponibilidad:** el solapamiento se evalúa contra reservas `CONFIRMADA` mediante una consulta de intersección de intervalos.
- **Configuración externa:** las variables de entorno permiten adaptar la ejecución a distintos entornos sin modificar el código fuente.
