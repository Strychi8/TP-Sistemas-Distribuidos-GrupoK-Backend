# Enunciado
A partir de la solución desarrollada en el **Hito 2**, el sistema **Rentar** deberá evolucionar incorporando un mecanismo de comunicación **asíncrona y basada en eventos**, utilizando **Apache Kafka o RabbitMQ** como intermediario de mensajes.

La solución deberá conservar las funcionalidades desarrolladas en los hitos anteriores, incluyendo la interfaz web, el **API Gateway**, los servicios internos y la comunicación mediante **gRPC**.

En este hito se deberán identificar aquellas operaciones que puedan resolverse mediante comunicación basada en eventos y diseñar una arquitectura que permita desacoplar dichos procesos.

La solución deberá combinar comunicación síncrona mediante gRPC y comunicación asíncrona mediante eventos, de acuerdo con las características de cada operación.

# Requerimientos

**Incorporación de un Broker de Mensajes**

La solución deberá incorporar uno de los siguientes sistemas:

● **Apache Kafka**
● **RabbitMQ**

El broker deberá utilizarse como mecanismo de comunicación asíncrona entre los distintos componentes de la aplicación.

Los estudiantes deberán:

● Configurar el broker.
● Identificar los procesos que utilizarán comunicación basada en eventos.
● Definir los eventos necesarios.
● Implementar productores y consumidores.
● Documentar los eventos y sus estructuras de datos.

Cada evento deberá permitir identificarlo y contener la información necesaria para que el consumidor pueda procesarlo.

---

**Verificación de vehículos**

Al registrar un vehículo, deberá realizarse de manera asíncrona una verificación de su patente contra un registro que permita determinar si el vehículo se encuentra denunciado como robado.

El proceso deberá contemplar que:

1. El vehículo sea registrado en el sistema.
2. Se genere un evento solicitando la verificación.
3. Un componente encargado de realizar la verificación consuma dicho evento.
4. Se consulte un registro propio que simule la información policial.
5. Se genere un nuevo evento informando el resultado de la verificación.
6. El sistema Rentar procese dicho resultado y actualice la información correspondiente del vehículo.

El resultado deberá ser uno de los siguientes:

● **VALIDADO**
● **ROBADO**

El valor ROBADO **no deberá incorporarse como un nuevo estado operativo del vehículo**.

Los estados del vehículo deberán continuar siendo:

● **DISPONIBLE**
● **EN_ALQUILER**

En caso de que el resultado indique que el vehículo fue denunciado como robado, deberá quedar **inhabilitado para nuevas operaciones**.

**vehiculos.verificacion.solicitada**
{
**"vehiculoId": 15,**
**"patente": "ABC123"**
}

**vehiculos.verificacion.resultado**
{
**"vehiculoId": 15,**
**"patente": "ABC123",**
**"resultado": "VALIDADO"**
}

---

**Incorporación de vehículos de propietarios externos**

Rentar deberá permitir que un cliente registre un vehículo de su propiedad con el objetivo de incorporarlo al sistema.

El proceso de incorporación deberá realizarse de manera **asíncrona** y deberá contemplar, como mínimo, las siguientes validaciones:

● Verificar que la patente no se encuentre denunciada como robada.
● Verificar que el cliente que solicita la incorporación sea el titular del vehículo, utilizando un registro externo de titularidad simulado.

El vehículo deberá comenzar el proceso en estado: **PENDIENTE**

Para realizar las validaciones deberá generarse un evento que permita que los componentes responsables de cada verificación procesen la solicitud de manera independiente.

Cada validación deberá generar un evento con su resultado.

Los resultados posibles para cada validación serán:

● **VALIDADO**
● **RECHAZADO**

El sistema deberá correlacionar los resultados correspondientes al mismo proceso de incorporación.

Si alguna de las validaciones resulta rechazada, el vehículo no deberá incorporarse al sistema.

Si todas las validaciones son satisfactorias, deberá generarse un evento que permita completar la incorporación del vehículo.

Una vez incorporado, el vehículo deberá quedar: **DISPONIBLE**

y podrá participar de las operaciones habituales de Rentar.

**vehiculos.externos.registrados**
{
**"vehiculoExternoId": 50,**
**"clienteId": 89,**
**"patente": "DEF456"**
}

**vehiculos.externos.validacion.patente**
{
**"vehiculoExternoId": 50,**
**"patente": "DEF456",**
**"resultado": "VALIDADO"**
}

**vehiculos.externos.validacion.titularidad**
{
**"vehiculoExternoId": 50,**
**"clienteId": 89,**
**"documento": "30123456",**
**"patente": "DEF456",**
**"resultado": "VALIDADO"**
}

**vehiculos.externos.validacion.completada**
{
**"vehiculoExternoId": 50**
}

---

**Mantenimiento preventivo asociado a una reserva**

Al realizarse una reserva, deberá generarse de manera **asíncrona** una solicitud de mantenimiento preventivo asociada al vehículo reservado.

El objetivo es demostrar que una operación puede continuar su flujo principal sin tener que esperar la respuesta de otro componente.

El proceso deberá contemplar:

1. Confirmación de la reserva.
2. Publicación de un evento indicando que la reserva fue confirmada.
3. Consumo del evento por el componente responsable del mantenimiento.
4. Generación y registro de una solicitud de mantenimiento preventivo.
5. Procesamiento del mantenimiento.
6. Publicación de un evento indicando el resultado.
7. Registro del resultado asociado a la reserva.

El resultado del mantenimiento deberá indicar:

● **REALIZADO**
● **NO_REALIZADO**

Consideración importante

La confirmación de la reserva **no deberá quedar bloqueada esperando el resultado del mantenimiento**.

**reserva.confirmada**
{
**"reservaId": 150,**
**"vehiculoId": 15,**
**"fechaInicio": "2026-10-10T10:00:00",**
**"fechaFin": "2026-10-15T10:00:00"**
}

**mantenimiento.resultado**
{
**"reservaId": 150,**
**"vehiculoId": 15,**
**"resultado": "REALIZADO"**
}

---

## Normas de entrega

El trabajo entregado deberá contener un documento incluyendo:

- La estrategia de resolución del trabajo práctico: todo aquello que consideren significativo
para explicar la resolución del trabajo: diagrama del modelo de datos, arquitectura del sistema, etc.
- El código fuente, **DE PROPIA AUTORÍA**, del proyecto subido a un repositorio público de **Github**.
- Las pruebas realizadas con las respectivas capturas de pantalla.
- Integrantes del grupo y las tareas realizadas por cada uno.

**El incumplimiento de cualquiera de las normas de entrega implicará la desaprobación del trabajo práctico.**