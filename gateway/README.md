# API Gateway

Esta carpeta implementa el API Gateway, actuando como el punto de entrada principal (orquestador) del Frontend. Expone una API unificada mediante **REST** y **GraphQL**, y se comunica internamente con los microservicios del Backend (desarrollados en Java) a través de **gRPC**.

## 🏗 Arquitectura y Componentes

El Gateway cumple el rol de **BFF (Backend for Frontend)** y **Orquestador**:
1. **Punto único de entrada:** Recibe peticiones HTTP externas.
2. **Traducción de protocolos:** Expone REST y GraphQL hacia el exterior y se comunica internamente vía gRPC con los microservicios.
3. **Orquestación de operaciones:** Coordina llamadas a múltiples microservicios gestionando compensaciones o rollbacks manuales en caso de fallos.
4. **Validación de Autenticación:** Verifica los tokens JWT y los permisos antes de delegar la petición a los microservicios, previniendo accesos no autorizados a servicios internos.

No contiene lógica de negocio pesada, no accede directamente a ninguna base de datos y su estado es puramente transaccional durante el request.

## 🚀 Tecnologías

- **Entorno de ejecución:** Node.js
- **Framework Web:** Express.js
- **API GraphQL:** `express-graphql` / `graphql`
- **RPC:** `@grpc/grpc-js` y `@grpc/proto-loader`
- **Autenticación:** `jsonwebtoken` (JWT)
- **Testing:** Jest + Supertest (Mocks para gRPC)

## 📁 Estructura del Proyecto

```text
gateway/
├── clients/          # Clientes gRPC para cada microservicio backend.
├── graphql/          # Esquemas (TypeDefs) y Resolvers de GraphQL.
├── middleware/       # Middlewares de Express (Autenticación, Manejo Global de Errores).
├── routes/           # Definición de rutas REST (ej. /api/reservas).
├── tests/            # Suite completa de pruebas unitarias y de integración (Jest + Supertest).
├── utils/            # Utilidades generales (ej. cargadores de archivos .proto).
├── index.js          # Punto de entrada de la aplicación y configuración del servidor Express.
└── package.json      # Dependencias y scripts del proyecto.
```

## 📋 Requisitos Previos

- **Node.js** (v18 o superior recomendado)
- **npm** o **yarn**
- Los archivos `.proto` compartidos deben estar accesibles (usualmente en un directorio compartido o copiados localmente) para que los clientes gRPC puedan compilar los contratos.
- Los microservicios Java (gRPC) deben estar en ejecución para realizar pruebas end-to-end.

## ⚙️ Configuración y Variables de Entorno

El gateway requiere ciertas variables de entorno para funcionar. Crea un archivo `.env` en la raíz de `gateway/` (o configúralo en tu entorno de despliegue) basándote en un posible `.env.example`:

```env
# Puerto del Gateway
PORT=3000

# Clave secreta para validación de JWT (Debe coincidir con la del backend Java)
JWT_SECRET=tu_clave_secreta_super_segura

# URLs de los servidores gRPC (Ejemplos)
GRPC_USUARIOS_URL=localhost:9090
GRPC_VEHICULOS_URL=localhost:9091
GRPC_ALQUILERES_URL=localhost:9092
```

## 🛠️ Instalación y Ejecución

1. **Instalar dependencias:**
   ```bash
   cd gateway
   npm install
   ```

2. **Ejecutar en modo desarrollo:**
   ```bash
   npm run dev
   ```

3. **Ejecutar en producción:**
   ```bash
   npm start
   ```

4. **Ejecutar la suite de pruebas (Testing):**
   ```bash
   npm test
   ```

## 🛣️ Endpoints Principales

### REST
- Base URL sugerida: `/api`
- `POST /api/auth/login` y `POST /api/auth/logout`: Gestión de sesión y JWT con verificación de lista negra.
- `POST /api/reservas`: Orquesta la creación de reserva, validación de fechas futuras, no solapamiento, verificación de que el vehículo esté activo, no inhabilitado y en estado `DISPONIBLE`, con rollback de compensación automático si falla la actualización del vehículo.
- `GET /api/reservas/:id`, `PUT /api/reservas/:id/cancelar`: Consulta y cancelación de reservas.
- `POST /api/vehiculos/externos` *(Hito 3)*: Registro de solicitud de vehículo propio para clientes autenticados. Resuelve `clienteId` y `documento` (DNI) desde el perfil autenticado (`GetMiPerfil`), ignorando cualquier suplantación en el payload.
- `GET /api/vehiculos/externos` *(Hito 3)*: Listado de solicitudes de incorporación de vehículos propios del cliente autenticado.
- `GET /api/vehiculos`, `GET /api/vehiculos/activos`, `GET /api/vehiculos/patente/:patente`, `GET /api/vehiculos/:id`: Consulta de vehículos.

### GraphQL
- Endpoint: `/graphql` (Swagger en `/api-docs`).
- Consultas principales:
  - `vehiculos`: Consulta general de flota, incluye campo `inhabilitado: Boolean`.
  - `vehiculosDisponibles`: Filtro avanzado por tipo, marca, modelo, precio y fechas con chequeo de solapamiento.
  - `reservas`: Consulta con filtros y detalle hidratado de cliente, vehículo y `resultadoMantenimiento: String`.
  - `historialAlquileres`: Consulta de historial de alquileres finalizados/cancelados por cliente con resolución de vehículos y patentes.


## 🔒 Seguridad y Autenticación

- **JWT (JSON Web Tokens):** Todas las rutas protegidas requieren un token JWT válido enviado en el header HTTP `Authorization`: `Bearer <token>`.
- **Roles:** Se validan roles como `CLIENTE` y `ADMINISTRADOR`.
- **Middleware `auth.middleware.js`:** 
  - Verifica la firma del token usando la clave secreta.
  - Verifica la fecha de expiración del token.
  - Valida si el token está en alguna lista negra (consultando al servicio gRPC correspondiente, si aplica).
  - Rechaza solicitudes sin token con error `401 Unauthorized`.
  - Inyecta el usuario (`req.user`) para su uso en los controladores.

## ⚠️ Manejo de Errores

Se implementa un `errorMiddleware` global que captura las excepciones de toda la aplicación (tanto de rutas REST como de resolvers GraphQL) y devuelve respuestas estandarizadas.

- **Formato estándar de error:** `{ "error": "Mensaje de error descriptivo" }`
- **Manejo de códigos HTTP:** Mapeo de errores gRPC a códigos HTTP adecuados (ej. `NOT_FOUND` a `404`, `UNAUTHENTICATED` a `401`, errores internos a `500`).

## 🧠 Decisiones Técnicas Destacadas

1. **Rollback Manual:** Dado que el Gateway orquesta múltiples microservicios (ej. crear reserva en Servicio Alquileres, actualizar vehículo en Servicio Vehículos), las transacciones distribuidas no se manejan con commits de base de datos directa. Si el paso 2 (actualizar vehículo) falla mediante gRPC, el Gateway captura el error y emite explícitamente un comando de compensación (ej. `CancelarReserva`) al Servicio de Alquileres para asegurar la consistencia.
2. **ES Modules y Mocking:** El proyecto utiliza ECMAScript Modules (`"type": "module"`). Para los tests unitarios con Jest, se emplea `jest.unstable_mockModule` permitiendo interceptar e inyectar mocks a los clientes gRPC antes de que las rutas Express sean inicializadas.
3. **Desacoplamiento de Lógica de Negocio:** El Gateway es estrictamente un enrutador y adaptador de protocolos. Toda regla de negocio compleja (ej. calculo de tarifas, cruce de fechas) reside en los microservicios Java.
