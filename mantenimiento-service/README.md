# mantenimiento-service

Microservicio reactivo a eventos para la **gestión de mantenimiento preventivo de vehículos asociados a reservas** del sistema **Rentar**. Diseñado en el marco del **Hito 3** para procesar solicitudes de mantenimiento de forma asincrónica mediante **Apache Kafka**, desacoplando el flujo principal de reservas y garantizando idempotencia y consistencia eventual.

---

## 1. Descripción general

En el ecosistema distribuido de Rentar, cuando un cliente confirma una reserva en `rental-service`, no debe bloquearse esperando que se efectúe la revisión mecánica o verificación preventiva del vehículo. 

`mantenimiento-service` es el componente responsable de:
1. **Consumir eventos de reservas confirmadas (`reserva.confirmada`)**: recibe el identificador de reserva, vehículo y fechas del alquiler.
2. **Registrar la solicitud de mantenimiento**: persiste de forma transaccional e idempotente la solicitud en estado `PENDIENTE`.
3. **Simular el procesamiento técnico del mantenimiento**: ejecuta un proceso no bloqueante (`@Async`) que emula la revisión preventiva del vehículo (2 a 3 segundos).
4. **Publicar el resultado del mantenimiento (`mantenimiento.resultado`)**: mediante el patrón Transactional Outbox / Event-Driven (`@TransactionalEventListener`), publica el resultado (`REALIZADO` o `NO_REALIZADO`) a Kafka para que `rental-service` actualice la reserva.
5. **Endpoints de auditoría y healthcheck**: expone endpoints HTTP en el puerto `9095` para monitoreo de salud (Docker healthcheck) y consulta de solicitudes.

---

## 2. Arquitectura y componentes

El microservicio implementa una arquitectura en capas orientada a eventos (*Event-Driven Architecture*):

```
                   ┌────────────────────────────────────────────────────────┐
                   │                 Apache Kafka (Broker)                  │
                   └──────┬──────────────────────────────────────────▲──────┘
                          │                                          │
                          │ reserva.confirmada                       │ mantenimiento.resultado
                          │                                          │
                          ▼                                          │
     ┌──────────────────────────────────────────┐                    │
     │         ReservaConfirmadaConsumer        │                    │
     └────────────────────┬─────────────────────┘                    │
                          │                                          │
                          ▼                                          │
     ┌──────────────────────────────────────────┐                    │
     │            MantenimientoService          │                    │
     │  - Registra solicitud (PENDIENTE)        │                    │
     │  - Idempotencia por reserva_id UNIQUE    │                    │
     └────────────────────┬─────────────────────┘                    │
                          │                                          │
                          ▼                                          │
     ┌──────────────────────────────────────────┐                    │
     │       MantenimientoSimuladorService      │                    │
     │  - Ejecución @Async no bloqueante        │                    │
     │  - Simulación de proceso preventivo      │                    │
     └────────────────────┬─────────────────────┘                    │
                          │                                          │
                          ▼                                          │
     ┌──────────────────────────────────────────┐                    │
     │      MantenimientoResultadoProducer      │────────────────────┘
     │  - Escucha commit de transacción local   │
     │  - Emite a Kafka con partición reservaId │
     └──────────────────────────────────────────┘
```

### Componentes clave:

| Componente | Rol |
|---|---|
| `kafka/consumer/ReservaConfirmadaConsumer` | Consumidor Kafka que escucha `reserva.confirmada` y maneja duplicados mediante `DataIntegrityViolationException`. |
| `service/MantenimientoService` | Lógica de persistencia transaccional (creación de solicitud y transición a `REALIZADO`). |
| `service/MantenimientoSimuladorService` | Servicio asíncrono (`@Async`) que emula el tiempo de inspección mecánica sin retener conexiones ni transacciones. |
| `kafka/producer/MantenimientoResultadoProducer` | Productor Kafka desacoplado con `@TransactionalEventListener(phase = AFTER_COMMIT)`. |
| `controller/MantenimientoController` | Endpoints REST auxiliares (`/salud`, `/solicitudes`). |
| `repository/SolicitudMantenimientoRepository` | Repositorio Spring Data JPA para la entidad `SolicitudMantenimiento`. |
| `config/KafkaConfig` | Configuración de tópicos, productor (`JacksonJsonSerializer` sin type headers) y consumidor con `StringJacksonJsonMessageConverter` (Jackson 3, soporte nativo de `LocalDateTime`). |

---

## 3. Tópicos y eventos de Kafka

### 3.1. Consumo: `reserva.confirmada`
Emitido por `rental-service` al confirmar una reserva:

```json
{
  "reservaId": 150,
  "vehiculoId": 15,
  "fechaInicio": "2026-10-10T10:00:00",
  "fechaFin": "2026-10-15T10:00:00"
}
```

### 3.2. Publicación: `mantenimiento.resultado`
Emitido hacia `rental-service` tras completarse la revisión preventiva:

```json
{
  "reservaId": 150,
  "vehiculoId": 15,
  "resultado": "REALIZADO"
}
```

---

## 4. Endpoints REST (Puerto 9095)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/mantenimiento/salud` | Healthcheck utilizado por Docker Compose. |
| `GET` | `/api/mantenimiento/solicitudes` | Lista todas las solicitudes de mantenimiento registradas. |
| `GET` | `/api/mantenimiento/solicitudes/reserva/{reservaId}` | Consulta el estado del mantenimiento para una reserva particular. |

---

## 5. Pruebas automatizadas

El microservicio cuenta con pruebas unitarias exhaustivas con Mockito y JUnit 5:

```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

Escenarios cubiertos:
- **`MantenimientoServiceTest`**: creación de solicitud inicial, actualización de estado a `REALIZADO` y emisión de evento interno.
- **`ReservaConfirmadaConsumerTest`**: delegación de solicitudes e idempotencia ante eventos repetidos.
- **`MantenimientoResultadoProducerTest`**: serialización y envío a Kafka con clave de partición por `reservaId`.
- **`MantenimientoControllerTest`**: verificación de endpoints REST de salud y consultas.

