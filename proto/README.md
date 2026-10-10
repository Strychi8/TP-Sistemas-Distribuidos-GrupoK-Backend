# Contratos gRPC (Protobuf)

Este directorio contiene los archivos `.proto` que definen los contratos de comunicación entre el API Gateway (Node.js) y los microservicios internos (Java).

## Estructura
- `vehicle.proto`: Definición del servicio de catálogo y disponibilidad de vehículos.
- `customer.proto`: Definición del servicio de gestión de clientes y validación de tokens.
- `rental.proto`: Definición del servicio de reservas e historial de alquileres.

## Versionado y Compatibilidad
Para mantener la compatibilidad hacia atrás y evitar romper los clientes:
1. **Nunca cambiar los números de los campos (`tags`)** una vez que el servicio está en producción.
2. **Nunca cambiar el tipo de un campo existente.**
3. **Nunca borrar un campo.** Si un campo ya no se usa, usar la palabra clave `reserved` con su número o nombre, para evitar que alguien más lo use en el futuro accidentalmente.
4. Todos los campos nuevos deben ser opcionales o simplemente añadirse respetando los números disponibles.

## Compilación
Para validar que los contratos están correctos, puedes ejecutar:
```bash
protoc --descriptor_set_out=descriptor.pb vehicle.proto customer.proto rental.proto
```
