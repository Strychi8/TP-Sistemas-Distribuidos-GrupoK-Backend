# Enunciado

La empresa **Rentar**, dedicada al alquiler de vehículos, desea desarrollar un sistema web que permita administrar su flota de vehículos, sus clientes y los alquileres realizados.

El sistema deberá contar con una **interfaz web desde la cual los usuarios podrán operar las diferentes funcionalidades solicitadas.**

El desarrollo del sistema se realizará de manera **incremental**, incorporando en cada etapa diferentes mecanismos de comunicación entre los componentes que forman parte de la aplicación.

El trabajo práctico estará dividido en tres hitos:

1. **HITO 1 — REST / GRAPHQL**
2. **HITO 2 — RPC**
3. **HITO 3 — APACHE KAFKA / RABBITMQ**

En cada hito se partirá de la solución desarrollada en el hito anterior, incorporando las modificaciones y funcionalidades indicadas.

La interfaz web deberá mantenerse durante los tres hitos, adaptándose a las modificaciones arquitectónicas y funcionales incorporadas en cada etapa.

La elección de los lenguajes de programación, frameworks, motor de base de datos y demás herramientas utilizadas queda a criterio de los integrantes del grupo, salvo las tecnologías de
comunicación indicadas específicamente en cada hito.

# Requerimientos

Desarrollar un sistema web para **Rentar** que permita administrar los vehículos disponibles para alquiler, los clientes y las reservas realizadas.

Las funcionalidades detalladas a continuación deberán desarrollarse utilizando **REST o GRAPHQL según se indique en cada requerimiento.**

**1. Gestión de vehículos (REST)**

**[ADMINISTRADOR]** se deberá contar con una funcionalidad para realizar el ABM de los vehículos pertenecientes a la flota de Rentar.

Cada vehículo contempla los siguientes campos:

- Id: clave primaria.
- Patente: única y obligatoria.
- Marca: obligatoria.
- Modelo: obligatorio.
- Año: obligatorio.
- Color.
- Tipo de vehículo.
- Precio diario.
- Estado.
- Activo/Inactivo.

Los tipos de vehículos disponibles serán:

- SEDAN
- SUV
- PICKUP
- COUPE
- HATCHBACK

Los estados posibles serán:

- DISPONIBLE
- RESERVADO
- EN_ALQUILER

Se deberá permitir:

- Alta.
- Modificación.
- Baja lógica.
- Consulta.

Al realizar el alta, el vehículo deberá quedar inicialmente en estado **DISPONIBLE**.

La patente no podrá modificarse una vez registrado el vehículo.

Los vehículos inactivos no podrán utilizarse para nuevos alquileres.

**2. Consulta de disponibilidad (GRAPHQL)**

**[CLIENTE]** se dispondrá de una nueva pantalla para consultar los vehículos disponibles para alquiler.

Se podrá filtrar por:

- Tipo de vehículo.
- Marca.
- Modelo.
- Rango de precio diario.
- Fecha y hora de inicio.
- Fecha y hora de finalización.

Los filtros serán opcionales, a excepción de la fecha y hora de inicio y finalización.

El sistema deberá mostrar únicamente aquellos vehículos que se encuentren disponibles durante todo el período seleccionado.

El resultado deberá mostrar:

- Patente.
- Marca.
- Modelo.
- Año.
- Color.
- Tipo de vehículo.
- Precio diario.

**3. Gestión de clientes (REST)**

**[ADMINISTRADOR]** se deberá contar con una funcionalidad para realizar el ABM de los clientes de Rentar.

Cada cliente contempla los siguientes campos:

- Id: clave primaria.
- Documento: único y obligatorio.
- Nombre: obligatorio.
- Apellido: obligatorio.
- Email: único y obligatorio.
- Teléfono.
- Fecha de nacimiento.
- Activo/Inactivo.

Se deberá permitir:

- Alta.
- Modificación.
- Baja lógica.
- Consulta.

Los clientes inactivos no podrán realizar nuevos alquileres.

**4. Alta de reserva (REST)**

**[CLIENTE]** se deberá permitir realizar una reserva de un vehículo para un período determinado.

Para realizar una reserva se deberá indicar:

- Vehículo.
- Fecha y hora de inicio.
- Fecha y hora de finalización.

El sistema deberá verificar:

- Que el cliente exista.
- Que el cliente se encuentre activo.
- Que el vehículo exista.
- Que el vehículo se encuentre activo.
- Que el vehículo esté disponible durante el período solicitado.
- Que la fecha de inicio sea futura.
- Que la fecha de finalización sea posterior a la fecha de inicio.

El importe total se calculará en función de la duración del alquiler y el precio diario del vehículo.

Una reserva creada correctamente deberá quedar en estado **CONFIRMADA**.

**5. Consulta de reservas (GRAPHQL)**

**[CLIENTE – ADMINISTRADOR]** se dispondrá de una pantalla para consultar las reservas registradas.

Los clientes solamente podrán consultar sus propias reservas.

El administrador podrá consultar las reservas correspondientes a todos los clientes.

Se podrá filtrar por:

- Cliente.
- Vehículo.
- Tipo de vehículo.
- Estado.
- Rango de fechas.

Los filtros serán opcionales.

El resultado deberá mostrar:

- Cliente.
- Vehículo.
- Patente.
- Fecha de inicio.
- Fecha de finalización.
- Precio diario.
- Importe total.
- Estado.

**6. Cancelación de reserva (REST)**

**[CLIENTE]** se deberá permitir cancelar una reserva.

La cancelación solamente podrá realizarse cuando el período de alquiler todavía no haya comenzado.

La reserva no deberá eliminarse físicamente de la base de datos.
Su estado deberá modificarse a **CANCELADA**.

Una reserva cancelada no deberá impedir que el vehículo pueda volver a ser reservado para dicho período.

**7. Historial de alquileres (GRAPHQL)**

**[CLIENTE]** se deberá disponer de una pantalla para consultar el historial de los alquileres realizados.
El resultado deberá mostrar:

- Vehículo.
- Patente.
- Fecha de inicio.
- Fecha de finalización.
- Cantidad de días.
- Importe total.
- Estado.

Deberán poder visualizarse tanto los alquileres finalizados como las reservas canceladas.

Se pide:

- Se deberá documentar mediante **Swagger** los endpoints REST desarrollados en este hito.
- Las operaciones y tipos utilizados en GraphQL deberán encontrarse debidamente documentados.

## Normas de entrega

El trabajo entregado deberá contener un documento incluyendo:

- La estrategia de resolución del trabajo práctico: todo aquello que consideren significativo para explicar la resolución del trabajo: diagrama del modelo de datos, arquitectura del sistema, etc.
- El código fuente, **DE PROPIA AUTORÍA**, del proyecto subido a un repositorio público de **Github**.
- Las pruebas realizadas con las respectivas capturas de pantalla.
- Integrantes del grupo y las tareas realizadas por cada uno.

**El incumplimiento de cualquiera de las normas de entrega implicará la desaprobación del trabajo práctico.**