# Contratos gRPC (Protocol Buffers v3)

Este directorio contiene las definiciones formales de contratos `.proto` que rigen la comunicación sincrónica interna del sistema distribuido **Rentar**. Estos esquemas actúan como la fuente única de verdad (*Single Source of Truth*) entre el **API Gateway** (desarrollado en Node.js / Express / Apollo Server) y los **microservicios de backend** (desarrollados en Java 21 / Spring Boot 4.1).

---

## 📐 Estándares de Diseño y Convenciones (Buf Lint)

Los contratos han sido diseñados y adecuados rigurosamente bajo las directivas del **Google Protocol Buffers Style Guide** y las reglas de linting de **Buf Lint**:

1. **Prefijos en valores de Enums (`ENUM_VALUE_PREFIX`):**
   - Para evitar colisiones en lenguajes como C++ o Go y respetar el estándar de Buf, todos los valores de enumeraciones llevan como prefijo el nombre del propio enum en mayúsculas:
     - `TipoVehiculo`: `TIPO_VEHICULO_UNSPECIFIED`, `TIPO_VEHICULO_SEDAN`, `TIPO_VEHICULO_SUV`, etc.
     - `EstadoVehiculo`: `ESTADO_VEHICULO_UNSPECIFIED`, `ESTADO_VEHICULO_DISPONIBLE`, `ESTADO_VEHICULO_RESERVADO`, etc.
     - `EstadoReserva`: `ESTADO_RESERVA_UNSPECIFIED`, `ESTADO_RESERVA_CONFIRMADA`, `ESTADO_RESERVA_CANCELADA`, etc.
     - `EstadoVehiculoExterno`: `ESTADO_VEHICULO_EXTERNO_UNSPECIFIED`, `ESTADO_VEHICULO_EXTERNO_PENDIENTE`, etc.
   - Cada enum incluye un valor neutro inicial `= 0` (`*_UNSPECIFIED`).

2. **Nombres de campos en `snake_case` (`FIELD_LOWER_SNAKE_CASE`):**
   - Todos los identificadores de campos en los mensajes utilizan minúsculas y guiones bajos (`id_cliente`, `id_vehiculo`, `id_reserva`, `fecha_inicio`, `costo_diario`, `importe_total`).
   - El compilador de Protobuf genera automáticamente los getters/setters en `camelCase` para Java (ej: `getIdCliente()`), mientras que los clientes dinámicos en Node.js pueden preservarlo o mapearlo de forma transparente.

3. **Mensajes de Solicitud y Respuesta Exclusivos (`RPC_REQUEST_RESPONSE_UNIQUE`):**
   - Cada método RPC dispone de su propio par de mensajes dedicados `[NombreMetodo]Request` y `[NombreMetodo]Response` (siguiendo las reglas `RPC_REQUEST_STANDARD_NAME` y `RPC_RESPONSE_STANDARD_NAME`).
   - Esto evita el acoplamiento directo entre endpoints y permite evolucionar los parámetros de entrada o salida de una operación sin provocar efectos colaterales en otras.

4. **Tipos de Datos Consistentes y Tipado Fuerte:**
   - **Fechas calendario:** Se utiliza el tipo estándar `google.type.Date` (`year`, `month`, `day`) en `customer.proto` (para `fecha_nacimiento`) garantizando consistencia semántica entre operaciones de consulta y modificación.
   - **Marcas temporales:** Se utiliza `google.protobuf.Timestamp` (`seconds`, `nanos`) en `vehicle.proto` y `rental.proto` para registrar instantes exactos en UTC.

5. **Documentación Exhaustiva:**
   - Todos los servicios, métodos RPC, mensajes, campos y enumeraciones contienen comentarios en línea que detallan su propósito, obligatoriedad y reglas de negocio aplicadas.

---

## 📂 Catálogo de Servicios y Contratos

| Archivo Proto | Package Protobuf | Package Java Generado | Servicio gRPC | Métodos RPC | Descripción de Responsabilidad |
| :--- | :--- | :--- | :--- | :---: | :--- |
| [`customer.proto`](customer.proto) | `rentar.customer` | `com.rentar.customer.grpc` | `CustomerService` | 11 | Gestión de clientes, verificación de unicidad (DNI/email), perfil autenticado y blacklist de tokens JWT revocados. |
| [`vehicle.proto`](vehicle.proto) | `rentar.vehicle` | `com.rentar.vehicle.grpc` | `VehicleService` | 11 | Catálogo general de vehículos, disponibilidad, filtros de búsqueda, gestión de estados y registro de vehículos externos. |
| [`rental.proto`](rental.proto) | `rentar.rental` | `com.rentar.rental.grpc` | `RentalService` | 6 | Creación y cancelación de reservas, validación de solapamiento de fechas y consulta del historial consolidado de alquileres. |

### Detalle de Métodos RPC por Servicio

#### 👤 `CustomerService` (`customer.proto`)
- `GetClientes(GetClientesRequest) -> GetClientesResponse`
- `GetClienteById(GetClienteByIdRequest) -> GetClienteByIdResponse`
- `GetMiPerfil(GetMiPerfilRequest) -> GetMiPerfilResponse`
- `VerificarClienteExistente(VerificarClienteExistenteRequest) -> VerificarClienteExistenteResponse`
- `VerificarClienteActivo(VerificarClienteActivoRequest) -> VerificarClienteActivoResponse`
- `VerificarTokenBlacklist(VerificarTokenBlacklistRequest) -> VerificarTokenBlacklistResponse`
- `Login(LoginRequest) -> LoginResponse`
- `Logout(LogoutRequest) -> LogoutResponse`
- `CrearCliente(CrearClienteRequest) -> CrearClienteResponse`
- `ActualizarCliente(ActualizarClienteRequest) -> ActualizarClienteResponse`
- `EliminarCliente(EliminarClienteRequest) -> EliminarClienteResponse`

#### 🚗 `VehicleService` (`vehicle.proto`)
- `CrearVehiculo(CrearVehiculoRequest) -> CrearVehiculoResponse`
- `ActualizarVehiculo(ActualizarVehiculoRequest) -> ActualizarVehiculoResponse`
- `ActualizarEstadoVehiculo(ActualizarEstadoVehiculoRequest) -> ActualizarEstadoVehiculoResponse`
- `EliminarVehiculo(EliminarVehiculoRequest) -> EliminarVehiculoResponse`
- `GetVehiculos(GetVehiculosRequest) -> GetVehiculosResponse`
- `GetVehiculosActivos(GetVehiculosActivosRequest) -> GetVehiculosActivosResponse`
- `GetVehiculoById(GetVehiculoByIdRequest) -> GetVehiculoByIdResponse`
- `GetVehiculoByPatente(GetVehiculoByPatenteRequest) -> GetVehiculoByPatenteResponse`
- `ConsultarDisponibilidad(ConsultarDisponibilidadRequest) -> ConsultarDisponibilidadResponse`
- `RegistrarVehiculoExterno(RegistrarVehiculoExternoRequest) -> RegistrarVehiculoExternoResponse`
- `GetVehiculosExternosByCliente(GetVehiculosExternosByClienteRequest) -> GetVehiculosExternosByClienteResponse`

#### 📅 `RentalService` (`rental.proto`)
- `CrearReserva(CrearReservaRequest) -> CrearReservaResponse`
- `ConsultarReservas(ConsultarReservasRequest) -> ConsultarReservasResponse`
- `GetReservaById(GetReservaByIdRequest) -> GetReservaByIdResponse`
- `CancelarReserva(CancelarReservaRequest) -> CancelarReservaResponse`
- `ConsultarHistorialAlquileres(ConsultarHistorialAlquileresRequest) -> ConsultarHistorialAlquileresResponse`
- `VerificarSolapamientoVehiculo(VerificarSolapamientoVehiculoRequest) -> VerificarSolapamientoVehiculoResponse`

---

## 🛠️ Compilación y Consumo

### 1. Validación de Sintaxis y Descriptores
Para comprobar que los contratos compilan limpiamente sin errores sintácticos ni dependencias circulares:
```bash
protoc -I. --descriptor_set_out=descriptor.pb customer.proto vehicle.proto rental.proto
```

### 2. Microservicios Java (Generación con Maven Wrapper)
Cada microservicio (`customer-service`, `vehicle-service`, `rental-service`) compila los `.proto` ubicados en esta carpeta raíz mediante el plugin `protobuf-maven-plugin`. Para regenerar los stubs y modelos Java:
```bash
cd customer-service  # o vehicle-service / rental-service
.\mvnw.cmd clean compile
```
Los artefactos generados se depositan automáticamente en:
`target/generated-sources/protobuf/java/` y `target/generated-sources/protobuf/grpc-java/`.

### 3. API Gateway (Carga Dinámica en Node.js)
El API Gateway carga los contratos dinámicamente en tiempo de ejecución utilizando `@grpc/proto-loader` y `@grpc/grpc-js`:
```javascript
const protoLoader = require('@grpc/proto-loader');
const grpc = require('@grpc/grpc-js');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true
});
```

---

## 🔄 Versionado y Compatibilidad hacia Atrás

Para preservar la interoperabilidad y no romper clientes en producción:
1. **Inmutabilidad de Tags:** Nunca alterar los números de campo (`tags`) asignados a un mensaje.
2. **Inmutabilidad de Tipos:** No cambiar el tipo de datos de un campo ya existente.
3. **Deprecación con `reserved`:** Nunca eliminar físicamente un campo en uso previo; si un campo queda obsoleto, se debe marcar su número o nombre con la palabra clave `reserved` para evitar reutilizaciones accidentales:
   ```protobuf
   message Customer {
     reserved 9, 10;
     reserved "campo_antiguo";
   }
   ```
4. **Adición no destructiva:** Todo nuevo campo debe añadirse con un tag numérico no utilizado previamente.

