# Hito 2 — Arquitectura de Microservicios y gRPC

Este hito evoluciona la arquitectura monolítica desarrollada en el Hito 1 hacia una **arquitectura distribuida basada en microservicios independientes y un API Gateway** como orquestador y punto único de entrada para el sistema **Rentar**.

---

## 🛠️ Tecnologías

- **API Gateway (BFF):** Node.js, Express.js, GraphQL, REST (OpenAPI / Swagger), gRPC Client, JWT.
- **Microservicios de Dominio:** Java 21, Spring Boot, gRPC Server, Spring Data JPA, Hibernate, MySQL.
- **Contratos de Comunicación:** Protocol Buffers (`.proto`).
- **Frontend Web:** React 19, Vite 8, TailwindCSS, Apollo Client (GraphQL), Axios (REST).
- **Contenerización y Despliegue:** Docker, Docker Compose.
- **Testing:** Jest, Supertest (mocks gRPC en Gateway), JUnit 5, Mockito (Java).

---

## 🏗️ Arquitectura del Sistema

```text
       ┌────────────────────────┐
       │     Frontend React     │
       └───────────┬────────────┘
                   │ HTTP (REST / GraphQL)
                   ▼
       ┌────────────────────────┐
       │   API Gateway (Node)   │  <-- Swagger UI (/docs) & GraphQL (/graphql)
       └─────┬─────┬──────┬─────┘
             │     │      │
     gRPC    │     │      │ gRPC
  (Port 9091)│     │      │ (Port 9093)
             ▼     │      ▼
┌──────────────────┐ │ ┌──────────────────┐
│  Vehicle Service │ │ │  Rental Service  │
│      (Java)      │ │ │      (Java)      │
└────────┬─────────┘ │ └────────┬─────────┘
         │           ▼          │
         │   ┌───────────────┐  │
         │   │CustomerService│  │
         │   │    (Java)     │  │
         │   └───────┬───────┘  │
         │           │gRPC (9092)
         ▼           ▼          ▼
      MySQL       MySQL       MySQL
  (rentar_vehicle) (rentar_customer) (rentar_rental)
```

---

## 🚀 Cómo Ejecutar el Proyecto

Puedes ejecutar el proyecto utilizando **Docker Compose** (recomendado para levantar todo el entorno con un solo comando) o de forma **local en terminales independientes**.

---

### Opción A: Ejecución con Docker (Recomendada)

Requiere tener instalado **Docker Desktop**.

#### 1. Levantar el Backend completo
Desde la raíz del repositorio Backend:
```bash
docker compose up --build -d
```
Este comando construirá y levantará:
- **`mysql-db`:** Base de datos con los tres esquemas inicializados (`3306`).
- **`vehicle-service`:** Microservicio de vehículos en gRPC (`9091`).
- **`customer-service`:** Microservicio de clientes y autenticación en gRPC (`9092`).
- **`rental-service`:** Microservicio de alquileres y reservas en gRPC (`9093`).
- **`gateway`:** API Gateway con endpoints REST, GraphQL y Swagger (`3000`).

#### 2. Comandos útiles de Docker
- **Verificar estado de los contenedores:**
  ```bash
  docker compose ps
  ```
- **Ver logs en tiempo real:**
  ```bash
  docker compose logs -f
  ```
- **Detener el entorno:**
  ```bash
  docker compose down
  ```

#### 3. Iniciar el Frontend Web
En una consola independiente en la carpeta del frontend:
```bash
cd ../TP-Sistemas-Distribuidos-GrupoK-Frontend
npm install
npm run dev
```
La aplicación web quedará accesible en: [http://localhost:5173](http://localhost:5173).

---

### Opción B: Ejecución en Local (Sin Docker)

Si prefieres ejecutar cada componente en consolas separadas:

#### 1. Base de Datos MySQL Local
Asegúrate de tener un servicio MySQL local en el puerto `3306` y crea las bases de datos requeridas:
```sql
CREATE DATABASE IF NOT EXISTS rentar_customer_db;
CREATE DATABASE IF NOT EXISTS rentar_vehicle_db;
CREATE DATABASE IF NOT EXISTS rentar_rental_db;
```

#### 2. Iniciar Microservicios Java (gRPC)
Abre 3 consolas independientes en la carpeta raíz del Backend:

- **Terminal 1 — Customer Service (Puerto gRPC 9092):**
  ```bash
  cd customer-service
  ./mvnw.cmd spring-boot:run     # En Linux/macOS: ./mvnw spring-boot:run
  ```

- **Terminal 2 — Vehicle Service (Puerto gRPC 9091):**
  ```bash
  cd vehicle-service
  ./mvnw.cmd spring-boot:run     # En Linux/macOS: ./mvnw spring-boot:run
  ```

- **Terminal 3 — Rental Service (Puerto gRPC 9093):**
  ```bash
  cd rental-service
  ./mvnw.cmd spring-boot:run     # En Linux/macOS: ./mvnw spring-boot:run
  ```

#### 3. Iniciar el API Gateway (Node.js)
- **Terminal 4 — Gateway (Puerto HTTP 3000):**
  ```bash
  cd gateway
  npm install
  npm run dev
  ```

#### 4. Iniciar el Frontend (React + Vite)
- **Terminal 5 — Frontend (Puerto HTTP 5173):**
  ```bash
  cd ../TP-Sistemas-Distribuidos-GrupoK-Frontend
  npm install
  npm run dev
  ```

---

## 📖 Documentación y Pruebas de APIs

### Swagger UI (REST)
- **URL interactiva:** [http://localhost:3000/docs](http://localhost:3000/docs)
- **JSON de Especificación OpenAPI:** [http://localhost:3000/api-docs](http://localhost:3000/api-docs)
- Permite probar todos los endpoints REST con soporte interactivo para token JWT mediante el botón `Authorize`.

### GraphQL
- **Endpoint:** [http://localhost:3000/graphql](http://localhost:3000/graphql)
- **GraphiQL IDE:** Accesible directamente desde el navegador en [http://localhost:3000/graphql](http://localhost:3000/graphql). Incluye documentación de tipos, campos y argumentos con autocompletado interactivo.

---

## ⚙️ Funcionalidades y Orquestación

1. **Gestión de Vehículos (REST / gRPC):**
   - ABM completo para Administrador y catálogo de vehículos activos para clientes.
2. **Consulta de Disponibilidad (GraphQL / gRPC):**
   - Filtro por rango de fechas y características vehiculares; verifica solapamiento consultando concurrentemente ambos microservicios.
3. **Gestión de Clientes y Autenticación (REST / gRPC):**
   - Registro, login con JWT, revocación de sesión en blacklist y consulta de perfil.
4. **Alta de Reserva (REST / Orquestación Distribuida con Rollback):**
   - El Gateway verifica la existencia y estado activo del cliente y vehículo, valida el no solapamiento de fechas, calcula la tarifa, crea la reserva en Rental Service y actualiza el vehículo a `RESERVADO`. Si falla este último paso, ejecuta un rollback compensatorio cancelando la reserva generada.
5. **Consulta de Reservas (GraphQL):**
   - Clientes visualizan sus reservas; Administrador consulta reservas globales.
6. **Cancelación de Reserva (REST):**
   - Endpoints `PUT /api/reservas/:id/cancelar` y `DELETE /api/reservas/:id`.
7. **Historial de Alquileres (GraphQL):**
   - Reporte con detalle de días, costo acumulado e historial de cancelaciones.

---

## 👥 Roles y Permisos

- **ADMINISTRADOR:** Acceso al ABM de flota vehicular, ABM de clientes y consulta global de reservas.
- **CLIENTE:** Consulta de catálogo y disponibilidad, creación y cancelación de reservas, consulta de perfil propio e historial de alquileres.

---

## 🔗 Repositorio Frontend
- [TP-Sistemas-Distribuidos-GrupoK-Frontend](https://github.com/Strychi8/TP-Sistemas-Distribuidos-GrupoK-Frontend)
