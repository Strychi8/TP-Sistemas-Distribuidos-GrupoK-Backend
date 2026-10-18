import grpc from '@grpc/grpc-js';

/**
 * Middleware global para manejar errores y mapear códigos de estado gRPC a HTTP.
 * Replica el comportamiento del GlobalExceptionHandler del Hito 1.
 */
export const errorMiddleware = (err, req, res, next) => {
  console.error('Error atrapado en el middleware global:', err);

  // Si el error tiene un 'code' numérico, asumimos que es un error de gRPC
  if (err && typeof err.code === 'number') {
    let httpStatus = 500;
    
    // Mapeo según la checklist
    switch (err.code) {
      case grpc.status.NOT_FOUND:
        httpStatus = 404;
        break;
      case grpc.status.INVALID_ARGUMENT:
        httpStatus = 400;
        break;
      case grpc.status.ALREADY_EXISTS:
        httpStatus = 409;
        break;
      case grpc.status.FAILED_PRECONDITION:
        httpStatus = 409;
        break;
      case grpc.status.UNAVAILABLE:
        httpStatus = 503;
        break;
      case grpc.status.UNAUTHENTICATED:
        httpStatus = 401;
        break;
      case grpc.status.PERMISSION_DENIED:
        httpStatus = 403;
        break;
      default:
        httpStatus = 500;
        break;
    }

    // gRPC suele enviar el mensaje descriptivo en err.details
    const errorMessage = err.details || err.message || 'Error interno en el microservicio';

    return res.status(httpStatus).json({ 
      error: errorMessage,
      grpcCode: err.code // Opcional, útil para debug
    });
  }

  // Si no es un error de gRPC (ej: un error sintáctico de Node o Express)
  const status = err.status || 500;
  res.status(status).json({ 
    error: err.message || 'Error interno del servidor en el API Gateway' 
  });
};
