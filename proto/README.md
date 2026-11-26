# Contratos Protocol Buffers y gRPC

Esta carpeta contiene los contratos que usan el API Gateway y los microservicios para comunicarse mediante gRPC. Los archivos `.proto` definen los servicios, sus métodos y los mensajes que intercambian.

El Gateway carga estos contratos en tiempo de ejecución con `@grpc/proto-loader`. Los microservicios Java generan código a partir de ellos durante la compilación.

## Estructura

```text
proto/
├── customer.proto
├── rental.proto
├── vehicle.proto
└── google/
    └── type/
        └── date.proto
```

`google/type/date.proto` define el tipo `google.type.Date`, que usa `customer.proto` para representar fechas de calendario, como la fecha de nacimiento. Se encuentra versionado localmente para que `@grpc/proto-loader` en el API Gateway pueda resolverlo sin dependencias externas. En cambio, `google/protobuf/timestamp.proto` (usado en `rental.proto`) es parte de los *well-known types* de Protobuf y es provisto automáticamente por el compilador `protoc`, el plugin de Maven y `@grpc/proto-loader`.

## Servicios y Contratos

| Archivo | Package Protobuf | Package Java | Servicio | Responsabilidad |
|---|---|---|---|---|
| [`customer.proto`](customer.proto) | `rentar.customer` | `com.rentar.customer.grpc` | `CustomerService` | Clientes, autenticación, perfil y verificación de tokens revocados. |
| [`vehicle.proto`](vehicle.proto) | `rentar.vehicle` | `com.rentar.vehicle.grpc` | `VehicleService` | Vehículos, disponibilidad y solicitudes de vehículos externos. |
| [`rental.proto`](rental.proto) | `rentar.rental` | `com.rentar.rental.grpc` | `RentalService` | Reservas, validación de disponibilidad e historial de alquileres. |

### `CustomerService`

Definido en [`customer.proto`](customer.proto). Expone estos métodos:

- `GetClientes`: Lista los clientes.
- `GetClienteById`: Obtiene un cliente por su identificador.
- `GetMiPerfil`: Obtiene el perfil asociado al usuario autenticado.
- `VerificarClienteExistente`: Comprueba si existe un cliente por DNI.
- `VerificarClienteActivo`: Comprueba si el cliente puede operar.
- `VerificarTokenBlacklist`: Comprueba si se revocó un token.
- `Login`: Autentica las credenciales y devuelve un JWT.
- `Logout`: Revoca el JWT.
- `CrearCliente`: Registra un cliente.
- `ActualizarCliente`: Modifica los datos de un cliente.
- `EliminarCliente`: Realiza la baja lógica de un cliente.

### `VehicleService`

Definido en [`vehicle.proto`](vehicle.proto). Expone estos métodos:

- `CrearVehiculo` y `ActualizarVehiculo`: Registran y modifican vehículos.
- `ActualizarEstadoVehiculo`: Cambia el estado operativo.
- `EliminarVehiculo`: Realiza la baja lógica de un vehículo.
- `GetVehiculos` y `GetVehiculosActivos`: Consultan la flota.
- `GetVehiculoById` y `GetVehiculoByPatente`: Buscan un vehículo.
- `ConsultarDisponibilidad`: Consulta vehículos disponibles para un período.
- `RegistrarVehiculoExterno`: Registra una solicitud de incorporación.
- `GetVehiculosExternosByCliente`: Lista solicitudes de un cliente.

### `RentalService`

Definido en [`rental.proto`](rental.proto). Expone estos métodos:

- `CrearReserva`: Crea una reserva.
- `ConsultarReservas`: Consulta reservas con filtros opcionales.
- `GetReservaById`: Obtiene una reserva por su identificador.
- `CancelarReserva`: Cancela una reserva.
- `ConsultarHistorialAlquileres`: Consulta el historial de un cliente.
- `VerificarSolapamientoVehiculo`: Comprueba si un período se superpone con otra reserva.

## Convenciones de los Contratos

- Los archivos usan `proto3`.
- Los campos usan `snake_case`, por ejemplo `id_cliente` y `fecha_inicio`.
- Cada enum comienza con un valor `*_UNSPECIFIED = 0`. Los demás valores llevan el nombre del enum como prefijo.
- Cada método RPC usa sus propios mensajes de solicitud y respuesta.
- `customer.proto` usa `google.type.Date` para fechas de calendario.
- `vehicle.proto` y `rental.proto` usan `google.protobuf.Timestamp` para fechas y horas.
- Los servicios, métodos, mensajes, campos y enums incluyen comentarios sobre su propósito.

## Validar los Contratos con `protoc`

Desde esta carpeta, compila los contratos y genera un descriptor:

```bash
protoc -I . --include_imports --descriptor_set_out=descriptor.pb customer.proto vehicle.proto rental.proto
```

El comando requiere `protoc` instalado y accesible desde el `PATH`. El archivo `descriptor.pb` contiene las definiciones compiladas y sus dependencias importadas.

## Generar Código Java

Los módulos `customer-service`, `vehicle-service` y `rental-service` usan `protobuf-maven-plugin`. Cada módulo compila el contrato que le corresponde desde esta carpeta y excluye los otros contratos.

Desde la raíz del repositorio, ejecuta el Maven Wrapper del módulo correspondiente:

```bash
# En Windows (PowerShell / CMD)
cd customer-service
.\mvnw.cmd clean compile

# En Linux / macOS
cd customer-service
./mvnw clean compile
```

Para compilar los otros servicios, usa `vehicle-service` o `rental-service` en lugar de `customer-service`. Maven genera el código en la carpeta `target` de cada módulo.

## Cargar los Contratos en el API Gateway

El Gateway usa `@grpc/proto-loader` y `@grpc/grpc-js` para cargar los archivos al iniciar. `gateway/clients/grpc.js` resuelve los archivos desde esta carpeta y configura el loader con estas opciones:

- `keepCase: true`: Conserva los nombres de campos en `snake_case`.
- `longs: String`: Representa los valores `int64` como cadenas.
- `enums: String`: Representa los enums por su nombre.
- `defaults: true`: Incluye valores predeterminados.
- `oneofs: true`: Conserva la información de campos `oneof`.

El archivo `google/type/date.proto` debe estar disponible en esta carpeta para resolver la importación de `customer.proto`.

## Compatibilidad al Cambiar un Contrato

Los números de campo forman parte del formato de los mensajes. Respeta estas reglas al modificar un contrato:

1. No cambies el número ni el tipo de un campo existente.
2. Asigna un número no utilizado a cada campo nuevo.
3. Si eliminas un campo, reserva su número y su nombre con `reserved`.
4. No reutilices números o nombres reservados.

Por ejemplo:

```protobuf
message Customer {
  reserved 9, 10;
  reserved "campo_antiguo";
}
```
