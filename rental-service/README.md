# Rental Service

## 1. Descripción general

`rental-service` es un microservicio del sistema Rentar, desarrollado en Java con Spring Boot, responsable de gestionar las operaciones relacionadas con las reservas de alquiler de vehículos.

Expone una interfaz de comunicación mediante gRPC y delega las reglas de negocio y el acceso a los datos en la capa de servicios y repositorios de la aplicación.

Sus principales funcionalidades son:

- Crear reservas de alquiler.
- Consultar reservas aplicando filtros.
- Obtener una reserva por su identificador.
- Cancelar reservas.
- Consultar el historial de alquileres de un cliente.
- Verificar el solapamiento de reservas para un vehículo en un período determinado.

## 2. Arquitectura y componentes

El microservicio utiliza una arquitectura por capas:

- **Capa de comunicación gRPC:** recibe las solicitudes de los clientes, transforma los mensajes Protocol Buffers y devuelve las respuestas correspondientes.
- **Capa de servicios:** implementa las reglas de negocio, las validaciones y las operaciones sobre las reservas.
- **Capa de persistencia:** utiliza repositorios para consultar y almacenar las reservas en la base de datos.
- **Capa de dominio y DTOs:** representa las entidades, los datos de entrada y salida y los objetos utilizados para transferir información entre las capas.

La implementación gRPC se encuentra en `RentalGrpcServiceImpl`, que extiende la clase base generada a partir de la definición del servicio.

La lógica de negocio se delega en `IReservaService` y su implementación `ReservaServiceImpl`.

## 3. Tecnologías utilizadas

- Java 21.
- Spring Boot.
- Maven.
- gRPC.
- Protocol Buffers (protobuf).
- MySQL.
- Docker y Docker Compose para el entorno de ejecución, según la configuración del proyecto.

## 4. Estructura del proyecto

Estructura principal del microservicio:

```text
rental-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/rentar/rental_service/
│   │   │       ├── grpc/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       └── mapper/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   └── test/
├── pom.xml
└── README.md
```

La definición del contrato gRPC se encuentra en `proto/rental.proto`, ubicado en el directorio `proto` del proyecto principal.

## 5. Requisitos previos

Para ejecutar el microservicio se requiere:

- JDK 21.
- Docker y Docker Compose, si se utiliza el entorno de contenedores del proyecto.
- Acceso a una instancia de MySQL.
- Git para obtener el código fuente.
- Un cliente compatible con gRPC, como Postman, para probar las operaciones.

## 6. Configuración y variables de entorno

La configuración de la aplicación se encuentra en `src/main/resources/application.properties`.

El microservicio utiliza variables de entorno para configurar el puerto del servidor, la conexión a la base de datos y otros parámetros de ejecución.

Variables identificadas en la configuración:

| Variable | Descripción |
|---|---|
| `RENTAL_SERVER_PORT` | Puerto HTTP del microservicio. |
| `DB_HOST` | Host de la base de datos MySQL. |
| `DB_PORT` | Puerto de MySQL. |
| `RENTAL_DB_NAME` | Nombre de la base de datos de reservas. |

También deben configurarse las credenciales de conexión que utilice la aplicación y el puerto gRPC, de acuerdo con los archivos de configuración vigentes.

Las variables deben definirse en el archivo `.env` correspondiente al entorno de ejecución. Si se utiliza Docker Compose, hay que verificar cuáles variables corresponden al contenedor y cuáles consume directamente Spring Boot.

## 7. Instalación y ejecución

### 7.1. Obtener el código fuente

Clonar el repositorio del backend:

```bash
git clone <URL_DEL_REPOSITORIO>
cd TP-Sistemas-Distribuidos-GrupoK-Backend
```

### 7.2. Configurar el entorno

Crear o completar los archivos `.env` requeridos y verificar que la configuración de MySQL coincida con la utilizada por el microservicio.

Si se utiliza Docker Compose, iniciar los servicios y dependencias definidos en el archivo de composición del proyecto.

### 7.3. Compilar el proyecto

Desde el directorio raíz del backend, ejecutar:

```bash
.\mvnw.cmd clean install
```

En Windows, este comando utiliza el Maven Wrapper del proyecto. La compilación debe realizarse desde el directorio que contiene el wrapper y el `pom.xml` correspondiente.

### 7.4. Ejecutar el microservicio

Iniciar `rental-service` mediante la configuración de ejecución del proyecto o el mecanismo definido en Docker Compose.

Verificar en los registros que la aplicación se haya iniciado correctamente y que el servidor gRPC esté escuchando en el puerto configurado.

Los puertos deben obtenerse de la configuración efectiva del entorno; no necesariamente serán los mismos en todos los entornos.

## 8. Funciones RPC disponibles

El contrato definido en `proto/rental.proto` expone las siguientes operaciones:

| RPC | Descripción |
|---|---|
| `CrearReserva` | Recibe los datos necesarios para crear una reserva. |
| `ConsultarReservas` | Consulta reservas mediante los filtros admitidos. |
| `GetReservaById` | Obtiene una reserva a partir de su identificador. |
| `CancelarReserva` | Solicita la cancelación de una reserva. |
| `ConsultarHistorialAlquileres` | Consulta el historial de alquileres de un cliente. |
| `VerificarSolapamientoVehiculo` | Verifica si existen reservas confirmadas que se superpongan con un período solicitado. |

Los nombres de los mensajes de solicitud y respuesta están definidos en el archivo `rental.proto`.

Las operaciones aplican las validaciones correspondientes de la lógica de negocio. Por ejemplo, la creación de reservas valida las fechas y la disponibilidad temporal del vehículo, mientras que la cancelación verifica que la reserva pueda cancelarse.

Los filtros admitidos y los campos obligatorios deben consultarse en el contrato protobuf y en la implementación de cada operación.

## 9. Manejo de errores

El microservicio contempla errores derivados de las reglas de negocio y de la consulta de recursos.

En las operaciones gRPC, los errores se traducen a estados de gRPC para informar el resultado de la solicitud al cliente.

Entre las situaciones que pueden generar errores se encuentran:

- Fechas de reserva inválidas.
- Intentos de cancelar reservas que ya fueron canceladas o finalizadas.
- Reservas que no pueden cancelarse porque su período ya comenzó.
- Conflictos por solapamiento de reservas.
- Identificadores de reservas inexistentes.
- Filtros no admitidos por una operación.

El estado gRPC concreto devuelto por cada situación debe verificarse en `RentalGrpcServiceImpl`.

## 10. Decisiones técnicas

- **gRPC y Protocol Buffers:** permiten definir un contrato tipado para la comunicación entre servicios y clientes.
- **Separación por capas:** mantiene diferenciadas la comunicación, las reglas de negocio y la persistencia.
- **Reutilización de servicios:** las operaciones gRPC delegan la lógica de negocio en los servicios de la aplicación, evitando duplicar las validaciones.
- **Validación de disponibilidad:** se verifica el solapamiento temporal de reservas confirmadas para evitar conflictos de disponibilidad.
- **Configuración externa:** las variables de entorno permiten adaptar la ejecución a diferentes entornos sin modificar el código fuente.