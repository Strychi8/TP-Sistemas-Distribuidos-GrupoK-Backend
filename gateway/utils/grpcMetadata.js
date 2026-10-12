import grpc from '@grpc/grpc-js';

/**
 * Extrae el contexto del usuario autenticado (inyectado por authMiddleware)
 * y genera el objeto Metadata de gRPC para propagar la identidad.
 * 
 * @param {Object} req - El objeto de request de Express.
 * @returns {grpc.Metadata} - La metadata lista para enviar en llamadas gRPC.
 */
export const createGrpcMetadata = (req) => {
  const metadata = new grpc.Metadata();

  if (req.user) {
    if (req.user.sub) {
      metadata.add('sub', String(req.user.sub));
    }
    if (req.user.roles) {
      metadata.add('roles', String(req.user.roles)); // String separado por comas
    }
  }

  return metadata;
};
