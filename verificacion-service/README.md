# verificacion-service

Microservicio reactivo a eventos para la **verificación policial de patentes** y **validación de titularidad vehicular** del sistema **Rentar**. Actúa como el **registro externo simulado**, procesando solicitudes asíncronas a través de **Apache Kafka** y proveyendo endpoints auxiliares REST para administración, auditoría y healthchecks.

---

## 1. Descripción general

En el ecosistema distribuido de Rentar, `verificacion-service` es el componente responsable de validar la legitimidad jurídica y antecedentes de los vehículos antes o durante su incorporación y operación. 

### Responsabilidades principales:
1. **Verificación policial de vehículos propios (H3-1 / H3-3):**
   - Consume solicitudes asíncronas de verificación emitidas cuando se registra un vehículo en el catálogo.
   - Contrasta la patente contra el registro policial (simulado en memoria y MySQL) para determinar si posee pedido de captura por robo o hurto.
   - Emite el resultado como `VALIDADO` o `ROBADO`.
2. **Validación integral de vehículos externos (H3-2 / H3-3):**
   - Consume eventos de registro de vehículos aportados por propietarios externos (estado inicial `PENDIENTE`).
   - Ejecuta en paralelo dos validaciones independientes:
     - **Control policial:** valida que la patente no esté reportada como robada (`VALIDADO` o `RECHAZADO`).
     - **Control de titularidad:** valida que el solicitante (`clienteId` o número de documento) coincida con el titular registral oficial del vehículo (`VALIDADO` o `RECHAZADO`).
3. **Persistencia dual y alta disponibilidad:**
   - Posee un mecanismo de verificación híbrido: consulta en caché concurrente en memoria y persiste/audita en base de datos relacional MySQL (`rentar_verificacion_db`).
4. **Endpoints REST de monitoreo e inspección:**
   - Expone endpoints HTTP en el puerto `9094` para chequeos de salud (Docker healthcheck), consulta de patentes robadas y registro de titularidades simuladas.

---

## 2. Arquitectura y componentes

El microservicio está diseñado siguiendo una arquitectura en capas orientada a eventos (*Event-Driven Architecture*), desacoplada y reactiva:

```
                  ┌────────────────────────────────────────────────────────┐
                  │                 Apache Kafka (Broker)                  │
                  └──────┬──────────────────────────────────────────▲──────┘
                         │                                          │
                         │ vehiculos.verificacion.solicitada        │ vehiculos.verificacion.resultado
                         │ vehiculos.externos.registrados           │ vehiculos.externos.validacion.patente
                         │                                          │ vehiculos.externos.validacion.titularidad
                         ▼                                          │
    ┌──────────────────────────────────────────┐                    │
    │         VerificacionKafkaConsumer        │                    │
    └────────────────────┬─────────────────────┘                    │
                         │                                          │
                         ▼                                          │
    ┌──────────────────────────────────────────┐                    │
    │       Capa de Servicios de Negocio       │                    │
    │  - RegistroPolicialServiceImpl           │                    │
    │  - RegistroTitularidadServiceImpl        │                    │
    └──────────────┬───────────────────────────┘                    │
                   │                                                │
         ┌─────────┴─────────┐                                      │
         ▼                   ▼                                      │
┌────────────────┐  ┌────────────────┐                              │
│ Caché Memoria  │  │ Repositorios   │                              │
│ (Sets / Maps)  │  │ JPA (MySQL)    │                              │
└────────────────┘  └────────┬───────┘                              │
                             ▼                                      │
                    ┌────────────────┐                              │
                    │ MySQL Database │                              │
                    └────────────────┘                              │
                                                                    │
    ┌──────────────────────────────────────────┐                    │
    │         VerificacionKafkaProducer        ├────────────────────┘
    └──────────────────────────────────────────┘

    ┌──────────────────────────────────────────┐
    │     REST Controller (Puerto 9094)        │ ◄─── Postman / Healthcheck / Docker
    │  - VerificacionController                │
    └──────────────────────────────────────────┘
```

### Componentes clave del servicio:

| Componente | Paquete | Responsabilidad |
| :--- | :--- | :--- |
| **`VerificacionKafkaConsumer`** | `kafka.consumer` | Escucha los tópicos de solicitudes entrantes y delega en los servicios de dominio. |
| **`VerificacionKafkaProducer`** | `kafka.producer` | Publica los eventos con los resultados de las validaciones en sus tópicos correspondientes. |
| **`IRegistroPolicialService`** | `service` | Reglas de negocio para consulta y registro de patentes denunciadas por robo. |
| **`IRegistroTitularidadService`** | `service` | Reglas de negocio para cotejar titularidad registral por `clienteId` o documento. |
| **`IPatenteRobadaRepository`** | `repository` | Acceso a datos JPA de la tabla `patentes_robadas`. |
| **`ITitularidadVehiculoRepository`**| `repository` | Acceso a datos JPA de la tabla `titularidades_vehiculos`. |
| **`KafkaConfig`** | `config` | Definición programática de tópicos, serializadores JSON interoperables y listeners concurrentes. |
| **`DataInitializer`** | `config` | Sembrado inicial automático de patentes y titularidades de prueba en MySQL. |
| **`VerificacionController`** | `controller` | Endpoints HTTP REST para healthcheck y consultas manuales. |

---

## 3. Tecnologías utilizadas

- **Java 21:** Versión estándar del proyecto con soporte para Virtual Threads y Records.
- **Spring Boot 4.1.1:** Núcleo de la aplicación, inyección de dependencias y autoconfiguración.
- **Spring Kafka (`spring-boot-starter-kafka`):** Integración con Kafka, configuración de productores, consumidores y deserializadores tolerantes a fallos.
- **Spring Data JPA / Hibernate ORM:** Mapeo y persistencia objeto-relacional.
- **MySQL 8 (`mysql-connector-j`):** Almacenamiento persistente de patentes robadas y titularidades simuladas.
- **Jackson Databind (`com.fasterxml.jackson.core`):** Serialización y deserialización JSON de eventos y respuestas HTTP.
- **Apache Kafka 3.7.0 (Modo KRaft):** Broker de mensajería asíncrona de alto rendimiento sin dependencia de ZooKeeper.
- **Kafbat UI (ex Kafka-UI):** Interfaz web para inspección de tópicos, particiones y mensajes en tiempo real.
- **Lombok:** Reducción de código repetitivo (Getters, Setters, Builders, Loggers).
- **Spring Dotenv:** Lectura y resolución dinámica de variables de entorno desde `.env`.
- **JUnit 5 & Mockito:** Suite de pruebas unitarias automatizadas para servicios y consumidores.
- **Docker & Docker Compose:** Contenerización y orquestación del microservicio, broker y base de datos.

---

## 4. Estructura del proyecto

```
verificacion-service/
├── pom.xml                                   # Configuración de dependencias Maven
├── Dockerfile                                # Build multistage con eclipse-temurin:21
├── README.md                                 # Documentación técnica del microservicio
├── .env                                      # Variables locales del microservicio
├── mvnw / mvnw.cmd                           # Maven Wrapper
└── src/
    ├── main/
    │   ├── java/com/rentar/verificacion_service/
    │   │   ├── VerificacionServiceApplication.java   # Clase principal Spring Boot
    │   │   ├── config/
    │   │   │   ├── KafkaConfig.java                  # Tópicos, Producer/Consumer factories y converters
    │   │   │   └── DataInitializer.java              # Semillas de patentes y titulares en arranque
    │   │   ├── controller/
    │   │   │   └── VerificacionController.java       # Endpoints REST (/api/verificaciones)
    │   │   ├── kafka/
    │   │   │   ├── consumer/
    │   │   │   │   └── VerificacionKafkaConsumer.java # @KafkaListener de eventos entrantes
    │   │   │   ├── event/                            # DTOs de eventos Kafka inmutables
    │   │   │   │   ├── VerificacionVehiculoEvent.java
    │   │   │   │   ├── VerificacionResultadoEvent.java
    │   │   │   │   ├── VehiculoExternoRegistradoEvent.java
    │   │   │   │   ├── ValidacionPatenteEvent.java
    │   │   │   │   └── ValidacionTitularidadEvent.java
    │   │   │   └── producer/
    │   │   │       └── VerificacionKafkaProducer.java # Métodos de emisión hacia Kafka
    │   │   ├── model/
    │   │   │   ├── PatenteRobada.java                # Entidad JPA: patentes_robadas
    │   │   │   └── TitularidadVehiculo.java          # Entidad JPA: titularidades_vehiculos
    │   │   ├── repository/
    │   │   │   ├── IPatenteRobadaRepository.java
    │   │   │   └── ITitularidadVehiculoRepository.java
    │   │   └── service/
    │   │       ├── IRegistroPolicialService.java
    │   │       ├── IRegistroTitularidadService.java
    │   │       └── impl/
    │   │           ├── RegistroPolicialServiceImpl.java
    │   │           └── RegistroTitularidadServiceImpl.java
    │   └── resources/
    │       └── application.properties                # Configuración de puertos, JPA y Kafka
    └── test/java/com/rentar/verificacion_service/
        ├── RegistroPolicialServiceTest.java          # Pruebas unitarias de registro policial
        ├── RegistroTitularidadServiceTest.java       # Pruebas unitarias de titularidades
        ├── VerificacionKafkaConsumerTest.java        # Pruebas unitarias de consumidores Kafka
        └── VerificacionServiceApplicationTests.java  # Test de contexto Spring Boot
```

---

## 5. Requisitos previos

- **JDK 21** instalado y configurado en el sistema.
- **Docker Desktop** (con soporte WSL2 en Windows) activo para ejecutar el broker Kafka y la base de datos MySQL.
- Maven Wrapper (`mvnw` / `mvnw.cmd`) incluido en el repositorio; no es necesario tener Maven instalado globalmente.

---

## 6. Configuración y variables de entorno

La configuración combina `application.properties` con variables cargadas dinámicamente desde el archivo `.env` en la raíz del módulo (o del repositorio):

| Variable | Descripción | Valor por defecto / local | Valor en Docker |
| :--- | :--- | :--- | :--- |
| `VERIFICACION_SERVER_PORT` | Puerto HTTP del microservicio | `9094` | `9094` |
| `DB_HOST` | Host de la base de datos MySQL | `localhost` | `mysql-db` |
| `DB_PORT` | Puerto de conexión a MySQL | `3306` | `3306` |
| `VERIFICACION_DB_NAME` | Nombre de la base de datos | `rentar_verificacion_db` | `rentar_verificacion_db` |
| `DB_USERNAME` | Usuario de MySQL | `verificacion_user` | `${VERIFICACION_DB_USER}` |
| `DB_PASSWORD` | Contraseña de MySQL | `verificacion_pass` | `${VERIFICACION_DB_PASSWORD}` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Modo de actualización DDL | `update` | `update` |
| `KAFKA_BOOTSTRAP_SERVERS` | Dirección del broker Kafka | `localhost:29092` | `kafka:9092` |
| `KAFKA_CONSUMER_GROUP_ID` | Grupo de consumidores Kafka | `verificacion-service-group`| `verificacion-service-group`|

### Esquema de puertos del ecosistema:
- **`9091`**: `vehicle-service` (gRPC / HTTP)
- **`9092`**: `customer-service` (gRPC / HTTP) & listener interno Kafka en red Docker
- **`9093`**: `rental-service` (gRPC / HTTP) & controlador KRaft
- **`9094`**: **`verificacion-service`** (HTTP REST)
- **`9095`**: `mantenimiento-service` (futuro)
- **`9000`**: `kafka-ui` (Kafbat Dashboard)
- **`29092`**: `kafka` (puerto accesible desde la máquina host)

---

## 7. Instalación y ejecución

### Opción A: Desarrollo Local Híbrido (Recomendada para pruebas rápidas)

1. **Iniciar la infraestructura de soporte (MySQL y Kafka) con Docker:**
   Desde la raíz del proyecto (`TP-Sistemas-Distribuidos-GrupoK-Backend`):
   ```powershell
   docker compose up -d mysql-db kafka kafka-ui
   ```
2. **Ejecutar el microservicio localmente con Maven:**
   Desde la carpeta `verificacion-service`:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *(En Linux / macOS: `./mvnw spring-boot:run`)*

### Opción B: Ejecución Completa con Docker Compose

Para levantar todo el stack sin ejecutar procesos locales de Java:
```powershell
docker compose up -d --build verificacion-service
```

### Ejecutar las Pruebas Unitarias
El proyecto cuenta con pruebas unitarias con Mockito que no requieren Docker ni bases de datos activas:
```powershell
.\mvnw.cmd test
```
*Resultado:* 10 tests unitarios ejecutados exitosamente al 100%.

---

## 8. Contrato asíncrono: Tópicos y Eventos Kafka

Todos los tópicos son creados automáticamente por el bean `KafkaConfig` con factor de replicación 1 y 1 partición al iniciar la aplicación.

### 8.1. Verificación Policial de Vehículo Propio

#### Tópico: `vehiculos.verificacion.solicitada`
Solicitud emitida por `vehicle-service` al crear un nuevo vehículo.
```json
{
  "vehiculoId": 15,
  "patente": "ABC123"
}
```

#### Tópico: `vehiculos.verificacion.resultado`
Respuesta emitida por `verificacion-service` con el dictamen policial.
```json
{
  "vehiculoId": 15,
  "patente": "ABC123",
  "resultado": "VALIDADO"
}
```
*(Valores posibles de `resultado`: `"VALIDADO"` o `"ROBADO"`)*.

---

### 8.2. Validación de Vehículos Externos

#### Tópico: `vehiculos.externos.registrados`
Evento entrante cuando un cliente registra su vehículo particular en estado `PENDIENTE`.
```json
{
  "vehiculoExternoId": 50,
  "clienteId": 89,
  "documento": "30123456",
  "patente": "DEF456"
}
```

#### Tópico: `vehiculos.externos.validacion.patente`
Resultado de la verificación policial del vehículo externo.
```json
{
  "vehiculoExternoId": 50,
  "patente": "DEF456",
  "resultado": "VALIDADO"
}
```
*(Valores posibles: `"VALIDADO"` o `"RECHAZADO"`)*.

#### Tópico: `vehiculos.externos.validacion.titularidad`
Resultado del cruce registral entre el solicitante y el titular oficial del vehículo.
```json
{
  "vehiculoExternoId": 50,
  "clienteId": 89,
  "documento": "30123456",
  "patente": "DEF456",
  "resultado": "VALIDADO"
}
```
*(Valores posibles: `"VALIDADO"` o `"RECHAZADO"`)*.

---

## 9. Endpoints REST (Administración y Pruebas en Postman)

El microservicio expone endpoints HTTP en el puerto `9094` para validar el funcionamiento y manipular los registros simulados:

| Método | Endpoint | Descripción | Payload / Parámetros |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/verificaciones/salud` | Chequeo de estado y conectividad (Healthcheck) | Ninguno |
| `GET` | `/api/verificaciones/patentes-robadas` | Listar todas las patentes con denuncia policial activa | Ninguno |
| `POST`| `/api/verificaciones/patentes-robadas` | Dar de alta una nueva denuncia por robo | `{"patente": "ZZ999ZZ", "motivo": "Hurto agravado"}` |
| `GET` | `/api/verificaciones/patentes-robadas/verificar/{patente}` | Consultar manualmente el estado de una patente | Variable en URL `{patente}` |
| `GET` | `/api/verificaciones/titularidades` | Listar todas las titularidades vehiculares registradas | Ninguno |
| `POST`| `/api/verificaciones/titularidades` | Registrar una nueva titularidad simulada | `{"clienteId": 89, "documento": "30123456", "patente": "DEF456", "nombreTitular": "Ana Martinez"}` |

### Ejemplos cURL:

#### Chequeo de Salud:
```bash
curl -X GET http://localhost:9094/api/verificaciones/salud
```
**Respuesta (HTTP 200 OK):**
```json
{
  "status": "UP",
  "servicio": "verificacion_service",
  "mensaje": "Microservicio de Verificación Policial y Titularidad (Kafka) operativo"
}
```

#### Verificar Patente Robada:
```bash
curl -X GET http://localhost:9094/api/verificaciones/patentes-robadas/verificar/AA111BB
```
**Respuesta (HTTP 200 OK):**
```json
{
  "patente": "AA111BB",
  "esRobado": true,
  "resultado": "ROBADO"
}
```

---

## 10. Seguridad y autenticación

- **Aislamiento en red privada:** `verificacion-service` es un microservicio interno de backend que opera dentro de la red Docker privada `rentar-net`. No está expuesto hacia la red pública de internet.
- **Comunicación Asíncrona sin Acoplamiento de Tokens:** A diferencia de los endpoints REST expuestos hacia clientes web, las transacciones de verificación se procesan como eventos independientes desacoplados en Kafka. No se transmiten credenciales de usuario en los payloads de los eventos.
- **Segregación de Base de Datos:** La base de datos `rentar_verificacion_db` cuenta con su propio usuario dedicado (`verificacion_user`), garantizando el principio de menor privilegio sobre los esquemas de los otros microservicios.

---

## 11. Manejo de errores y resiliencia

1. **Evitar Poison Pills en Kafka con `ErrorHandlingDeserializer`:**
   En `KafkaConfig.java`, el deserializador está envuelto en `ErrorHandlingDeserializer`. Si entra un mensaje con formato corrupto o JSON inválido en un tópico, el consumidor no se bloquea indefinidamente ni interrumpe la lectura de las particiones.
2. **Interoperabilidad Jackson sin Type-Headers:**
   Se configuró `JsonSerializer.ADD_TYPE_INFO_HEADERS = false` y `JsonDeserializer.USE_TYPE_INFO_HEADERS = false` con `TRUSTED_PACKAGES = "*"`. Esto permite que microservicios escritos en diferentes paquetes Java (o diferentes lenguajes) puedan intercambiar eventos JSON sin fallos por `ClassNotFoundException`.
3. **Resiliencia de Datos Híbrida (Memoria + BD):**
   Tanto `RegistroPolicialServiceImpl` como `RegistroTitularidadServiceImpl` mantienen listas concurrentes precargadas en memoria. Si por algún motivo la base de datos MySQL presenta latencia o una falla transitoria, el servicio puede responder inmediatamente contra el caché seguro en memoria sin interrumpir el flujo del broker.

---

## 12. Decisiones técnicas

- **Apache Kafka en modo KRaft:** Se prescindió de Apache ZooKeeper utilizando KRaft nativo en Kafka 3.7.0, reduciendo el consumo de memoria del clúster y simplificando el despliegue a un solo nodo autocontenido.
- **Doble Listener en Broker Kafka:**
  - `PLAINTEXT://kafka:9092`: comunicación a máxima velocidad dentro de la red Docker (`rentar-net`).
  - `EXTERNAL://localhost:29092`: acceso directo desde la máquina anfitriona para desarrollo local con IDE, Postman o scripts de prueba.
- **Selección del Puerto 9094:**
  Se asignó el puerto `9094` para conservar la convención numérica correlativa de los microservicios del sistema (`9091` vehículos, `9092` clientes, `9093` reservas/alquileres, `9094` verificación, `9095` mantenimiento).
- **Cumplimiento estricto del enunciado de negocio:**
  El microservicio dictamina `ROBADO` o `VALIDADO`, permitiendo que `vehicle-service` inhabilite el vehículo para nuevas operaciones sin inventar un estado operativo inválido (`DISPONIBLE` y `EN_ALQUILER` siguen siendo los únicos estados operativos según las directivas del enunciado).
