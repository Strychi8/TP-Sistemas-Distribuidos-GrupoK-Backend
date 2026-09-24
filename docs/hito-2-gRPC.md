# Enunciado

A partir del sistema desarrollado en el **Hito 1**, la empresa **Rentar** desea evolucionar su arquitectura
incorporando una estructura basada en **servicios independientes**.

El objetivo de este hito **no es desarrollar un sistema nuevo ni reemplazar las funcionalidades implementadas en el Hito 1**, sino **evolucionar la solución existente**, manteniendo las funcionalidades ya desarrolladas y modificando su arquitectura para incorporar comunicación mediante **RPC (Remote Procedure Call), utilizando gRPC como mecanismo de comunicación entrelos servicios**.

La **interfaz web desarrollada en el Hito 1 deberá mantenerse** y continuará siendo el medio desde el cual los usuarios acceden a las funcionalidades del sistema.

Las funcionalidades desarrolladas en el Hito 1 deberán continuar disponibles.

Por lo tanto, al finalizar este hito deberán seguir funcionando, como mínimo:

- Gestión de vehículos.
- Consulta de disponibilidad.
- Gestión de clientes.
- Alta de reservas.
- Consulta de reservas.
- Cancelación de reservas.
- Historial de alquileres.

La diferencia respecto del Hito 1 será principalmente **la arquitectura interna y la forma en que los componentes se comunican**.

# Requerimientos

A partir de este hito se deberá incorporar un **API Gateway** como punto de entrada al sistema.

Las solicitudes realizadas desde la interfaz web deberán llegar al API Gateway, y este deberá comunicarse con los servicios internos correspondientes.

La arquitectura deberá seguir conceptualmente el siguiente esquema:

![Arquitectura de Microservicios](images/arquitectura-de-microservicios.png)

Deberán existir servicios que permitan separar las responsabilidades relacionadas con:

- Vehículos.
- Clientes.
- Reservas y alquileres.

**1. API Gateway (Rest/GraphQL)**

El **API Gateway** será el punto de entrada al sistema.

La interfaz web **no deberá comunicarse directamente con los servicios internos**.

El Gateway deberá:

- Recibir las solicitudes provenientes de la interfaz web.
- Determinar qué servicio o servicios deben intervenir.
- Comunicarse con los servicios internos mediante **gRPC**.
- Coordinar las operaciones que requieran la participación de más de un servicio.
- Recibir las respuestas de los servicios.
- Construir la respuesta correspondiente para la interfaz web.

El Gateway no deberá acceder directamente a las bases de datos pertenecientes a los servicios
internos.

Las reglas correspondientes a cada dominio deberán permanecer dentro del servicio responsable de
dicho dominio.

**2. Vehicle Service — gRPC**

Deberá existir un servicio responsable de las operaciones relacionadas con los vehículos.

Este servicio deberá encargarse, como mínimo, de:

- Consultar vehículos.
- Consultar un vehículo específico.
- Consultar disponibilidad.
- Actualizar el estado de un vehículo cuando corresponda.
- Aplicar las validaciones propias de los vehículos.

La comunicación entre el API Gateway y este servicio deberá realizarse mediante **gRPC**.

**3. Customer Service — gRPC**

Deberá existir un servicio responsable de las operaciones relacionadas con los clientes.

Este servicio deberá encargarse, como mínimo, de:

- Consultar clientes.
- Consultar un cliente específico.
- Verificar la existencia de un cliente.
- Verificar que un cliente se encuentre activo.
- Aplicar las validaciones propias de los clientes.

La comunicación entre el API Gateway y este servicio deberá realizarse mediante **gRPC**.

**4. Rental Service — gRPC**

Deberá existir un servicio responsable de las operaciones relacionadas con las reservas y alquileres.

Este servicio deberá encargarse, como mínimo, de:

- Crear reservas.
- Consultar reservas.
- Cancelar reservas.
- Consultar el historial correspondiente.

La comunicación entre el API Gateway y este servicio deberá realizarse mediante **gRPC**.

Por ejemplo, para realizar una reserva, el sistema deberá poder coordinar la participación de los
servicios correspondientes:

![Flujo Detallado de Creacion de Reserva](images/flujo-detallado-de-creacion-de-reserva.png)

Se pide:

- Los servicios **gRPC deberán estar definidos y documentados mediante contratos .proto**, los cuales deberán formar parte de la entrega.
- El **API Gateway y los servicios internos deberán estar implementados en lenguajes de programación diferentes**.

## Normas de entrega

El trabajo entregado deberá contener un documento incluyendo:

- La estrategia de resolución del trabajo práctico: todo aquello que consideren significativo
para explicar la resolución del trabajo: diagrama del modelo de datos, arquitectura del sistema, etc.
- El código fuente, **DE PROPIA AUTORÍA**, del proyecto subido a un repositorio público de **Github**.
- Las pruebas realizadas con las respectivas capturas de pantalla.
- Integrantes del grupo y las tareas realizadas por cada uno.

**El incumplimiento de cualquiera de las normas de entrega implicará la desaprobación del trabajo práctico.**