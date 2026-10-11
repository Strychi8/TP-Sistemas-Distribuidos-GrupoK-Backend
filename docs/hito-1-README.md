# **Hito 1 (REST/GraphQL)**

Este hito constituye la base del backend del sistema de alquiler de vehículos. Su objetivo principal es exponer una serie de servicios web utilizando **REST** para las operaciones transaccionales (ABM de clientes, vehiculos y reservas) y **GraphQL** para consultas de disponibilidad e historial. La arquitectura está diseñada en capas con Spring Boot, garantizando seguridad mediante JWT y acceso a base de datos con Spring Data JPA.

---

## **Tecnologias**

- **Lenguaje**: Java 21
- **Framework**: Spring Boot 4.1.1
- **Base de Datos**: MySQL
- **Persistencia**: Spring Data JPA + Hibernate
- **Seguridad**: Spring Security + JWT (JSON Web Tokens)
- **APIs**: REST y Spring for GraphQL
- **Documentacion**: Springdoc OpenAPI (Swagger UI)

---

## 🚀 **Como ejecutar el proyecto**

### 1. Requisitos previos
- **Java 21** o superior.
- **Maven** (o utilizar el wrapper `./mvnw` incluido en el proyecto).
- **MySQL** corriendo localmente en el puerto `3306` (o vía Docker).

### 2. Configuración de Variables de Entorno
El proyecto utiliza un archivo `.env` o configuraciones en `application.properties`. Asegúrate de configurar las siguientes variables de entorno (puedes crear un archivo `.env` en la raíz de `web_services`):

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=rentar
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_contraseña
JWT_SECRET=tu_clave_secreta_super_segura_para_firmar_los_tokens_jwt
```

### 3. Ejecución del Backend
Para compilar y levantar la aplicación usando Maven, sitúate en el directorio del proyecto (`web_services`) y ejecuta:

```bash
./mvnw spring-boot:run
```

La API se levantará por defecto en `http://localhost:8081`.

### 4. Acceso a la Documentación (Swagger y GraphiQL)
Una vez que el servidor está corriendo, puedes probar los endpoints y visualizar la documentación interactiva en las siguientes URLs:
- **Swagger UI (API REST):** [http://localhost:8081/docs](http://localhost:8081/docs)
- **GraphiQL (API GraphQL):** [http://localhost:8081/graphiql](http://localhost:8081/graphiql)

---

## 🔐 **Autenticación y roles**

La API está protegida: salvo el login y la documentación, **todos los endpoints requieren un token JWT**. El acceso a las distintas funcionalidades de la API está estrictamente restringido en base a roles.

### Cómo obtener el token

1. Hacer login en `POST /api/auth/login` con email y contraseña:
   ```json
   { "email": "admin@empresarentar.com", "password": "admin123" }
   ```

2. La respuesta devuelve un token:
   ```json
   { "token": "eyJhbGciOiJIUzI1NiJ9..." }
   ```

3. Enviar ese token en el header de las demás llamadas:
   ```text
   Authorization: Bearer <token>
   ```

En **Swagger UI** se usa el botón **Authorize** 🔓 (arriba a la derecha): se pega el token una vez y se envía automáticamente en todas las llamadas.
En **GraphiQL**, se agrega manualmente en la pestaña **Headers**:
```json
{ "Authorization": "Bearer <token>" }
```

---

## ⚙️ **Funcionalidades**

- **Vehículos** — ABM completo (REST), con validación de datos (campos obligatorios, precio > 0) y baja lógica mediante `DELETE /{id}`.
- **Consulta de disponibilidad** — GraphQL (filtrado opcional por tipo, marca, modelo y rango de precio).
- **Clientes** — ABM completo (REST), incluyendo consulta de perfil del usuario autenticado (`/perfil`) y baja lógica.
- **Reservas** — Alta de reservas con cálculo automático de importe total y validación de fechas (REST).
- **Cancelación de reservas** — Cancelación de reservas mediante cambio de estado (REST: `PUT /{id}/cancelar`).
- **Consulta de reservas e historial de alquileres** — GraphQL, permitiendo consultar reservas (con filtros) y revisar el historial de alquileres por cliente.
- **Seguridad** — Autenticación (login/logout) con JWT, encriptación de contraseñas mediante BCrypt y protección de endpoints por rol.

---

##  📂 **Estructura del proyecto**

```
web_services/src/main/java/com/empresa_rentar/web_services/
├── WebServicesApplication.java    # Clase principal para levantar Spring Boot
├── config/                        # Configuraciones de la aplicación
│   ├── ApplicationConfig.java     # Configuración de beans (ej. UserDetailsService, PasswordEncoder)
│   ├── CorsConfig.java            # Configuración de CORS
│   ├── SecurityConfig.java        # Configuración de seguridad y endpoints protegidos
│   └── SwaggerConfig.java         # Configuración de OpenAPI (Swagger UI)
├── controller/                    # Controladores REST (auth, clientes, vehículos, reservas)
├── dto/                           # Data Transfer Objects (request/response)
├── enums/                         # Enumeraciones de dominio (roles, estados, etc.)
├── exception/                     # Manejo de errores y excepciones personalizadas
├── graphql/                       # Controladores para los endpoints de GraphQL
├── mapper/                        # Componentes para transformar entre DTOs y Entidades
├── model/                         # Entidades persistentes de la base de datos (JPA)
├── repository/                    # Interfaces de acceso a datos (Spring Data JPA)
├── security/                      # Componentes JWT (JwtAuthenticationFilter, JwtService)
├── service/                       # Interfaces y lógica de negocio (impl/)
└── validation/                    # Validaciones personalizadas para DTOs

web_services/src/main/resources/
├── application.properties         # Configuración principal de Spring Boot
├── data.sql                       # Script inicial de base de datos
└── graphql/
    └── schema.graphqls            # Esquema y definición de tipos de GraphQL
```

---

### 👥 **Roles y permisos**

| Rol             | Acciones principales                                             |
| --------------- | ---------------------------------------------------------------- |
| **ADMINISTRADOR**  | ABM de vehículos, ABM de clientes (REST y GraphQL), consulta de todas las reservas |
| **CLIENTE** | Consulta de disponibilidad, alta y cancelación de reservas propias, consulta e historial |

---

### Frontend

Link al repositorio: [https://github.com/Strychi8/TP-Sistemas-Distribuidos-GrupoK-Frontend](https://github.com/Strychi8/TP-Sistemas-Distribuidos-GrupoK-Frontend)

### 👉 **Recursos Adicionales**

- [**Documentación de Spring Boot**](https://spring.io/projects/spring-boot)
- [**Documentación de Spring Data JPA**](https://spring.io/projects/spring-data-jpa)
- [**Documentación de Spring Security**](https://spring.io/projects/spring-security)
- [**Documentación de Spring for GraphQL**](https://spring.io/projects/spring-graphql)
- [**Documentación de JWT (JSON Web Tokens)**](https://jwt.io/introduction)