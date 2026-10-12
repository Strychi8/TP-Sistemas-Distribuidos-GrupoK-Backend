export const swaggerDocument = {
  openapi: '3.0.0',
  info: {
    title: 'Rentar API Gateway',
    version: '2.0.0',
    description: 'API Gateway REST & GraphQL para la plataforma Rentar (Hito 2 - RPC con gRPC)',
    contact: {
      name: 'Grupo K - Sistemas Distribuidos'
    }
  },
  servers: [
    {
      url: 'http://localhost:3000',
      description: 'Servidor local API Gateway'
    }
  ],
  components: {
    securitySchemes: {
      bearerAuth: {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
        description: 'Ingrese el token JWT obtenido en /api/auth/login'
      }
    },
    schemas: {
      LoginRequest: {
        type: 'object',
        required: ['email', 'password'],
        properties: {
          email: { type: 'string', example: 'admin@rentar.com' },
          password: { type: 'string', example: 'admin123' }
        }
      },
      LoginResponse: {
        type: 'object',
        properties: {
          token: { type: 'string', example: 'eyJhbGciOiJIUzI1Ni...' }
        }
      },
      Vehiculo: {
        type: 'object',
        properties: {
          idVehiculo: { type: 'integer', example: 1 },
          patente: { type: 'string', example: 'AA123BB' },
          marca: { type: 'string', example: 'Toyota' },
          modelo: { type: 'string', example: 'Corolla' },
          anio: { type: 'integer', example: 2023 },
          color: { type: 'string', example: 'Blanco' },
          tipoVehiculo: {
            type: 'string',
            enum: ['SEDAN', 'SUV', 'PICKUP', 'COUPE', 'HATCHBACK'],
            example: 'SEDAN'
          },
          precioDiario: { type: 'number', example: 45000.0 },
          estado: {
            type: 'string',
            enum: ['DISPONIBLE', 'RESERVADO', 'EN_ALQUILER'],
            example: 'DISPONIBLE'
          },
          activo: { type: 'boolean', example: true }
        }
      },
      VehiculoCrearRequest: {
        type: 'object',
        required: ['patente', 'marca', 'modelo', 'anio', 'precioDiario', 'tipoVehiculo'],
        properties: {
          patente: { type: 'string', example: 'AA123BB' },
          marca: { type: 'string', example: 'Toyota' },
          modelo: { type: 'string', example: 'Corolla' },
          anio: { type: 'integer', example: 2023 },
          color: { type: 'string', example: 'Blanco' },
          tipoVehiculo: {
            type: 'string',
            enum: ['SEDAN', 'SUV', 'PICKUP', 'COUPE', 'HATCHBACK'],
            example: 'SEDAN'
          },
          precioDiario: { type: 'number', example: 45000.0 }
        }
      },
      Cliente: {
        type: 'object',
        properties: {
          idCliente: { type: 'integer', example: 1 },
          nombre: { type: 'string', example: 'Juan' },
          apellido: { type: 'string', example: 'Pérez' },
          dni: { type: 'string', example: '35123456' },
          email: { type: 'string', example: 'juan.perez@example.com' },
          activo: { type: 'boolean', example: true }
        }
      },
      ClienteCrearRequest: {
        type: 'object',
        required: ['email', 'password', 'dni', 'nombre', 'apellido'],
        properties: {
          email: { type: 'string', example: 'juan.perez@example.com' },
          password: { type: 'string', example: 'password123' },
          dni: { type: 'string', example: '35123456' },
          nombre: { type: 'string', example: 'Juan' },
          apellido: { type: 'string', example: 'Pérez' },
          telefono: { type: 'string', example: '1122334455' },
          fechaNacimiento: { type: 'string', example: '1990-05-15' }
        }
      },
      ReservaCrearRequest: {
        type: 'object',
        required: ['idCliente', 'idVehiculo', 'fechaInicio', 'fechaFin'],
        properties: {
          idCliente: { type: 'integer', example: 1 },
          idVehiculo: { type: 'integer', example: 1 },
          fechaInicio: { type: 'string', format: 'date-time', example: '2026-11-01T10:00:00.000Z' },
          fechaFin: { type: 'string', format: 'date-time', example: '2026-11-05T10:00:00.000Z' }
        }
      },
      ReservaResponse: {
        type: 'object',
        properties: {
          idReserva: { type: 'integer', example: 1 },
          idCliente: { type: 'integer', example: 1 },
          idVehiculo: { type: 'integer', example: 1 },
          fechaInicio: { type: 'string', example: '2026-11-01T10:00:00.000Z' },
          fechaFin: { type: 'string', example: '2026-11-05T10:00:00.000Z' },
          precioDiario: { type: 'number', example: 45000.0 },
          importeTotal: { type: 'number', example: 180000.0 },
          estadoReserva: { type: 'string', example: 'CONFIRMADA' }
        }
      },
      ErrorResponse: {
        type: 'object',
        properties: {
          error: { type: 'string', example: 'Descripción detallada del error' }
        }
      }
    }
  },
  paths: {
    '/api/auth/login': {
      post: {
        tags: ['Autenticación'],
        summary: 'Iniciar sesión y obtener JWT',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/LoginRequest' }
            }
          }
        },
        responses: {
          200: {
            description: 'Login exitoso',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/LoginResponse' }
              }
            }
          },
          400: { description: 'Credenciales inválidas o faltantes' }
        }
      }
    },
    '/api/auth/logout': {
      post: {
        tags: ['Autenticación'],
        summary: 'Cerrar sesión (revocar token en blacklist)',
        security: [{ bearerAuth: [] }],
        responses: {
          200: { description: 'Sesión cerrada correctamente' },
          401: { description: 'No autenticado o token revocado' }
        }
      }
    },
    '/api/vehiculos': {
      get: {
        tags: ['Vehículos'],
        summary: 'Listar todos los vehículos (Admin)',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Lista de vehículos',
            content: {
              'application/json': {
                schema: {
                  type: 'array',
                  items: { $ref: '#/components/schemas/Vehiculo' }
                }
              }
            }
          }
        }
      },
      post: {
        tags: ['Vehículos'],
        summary: 'Alta de vehículo (Admin)',
        security: [{ bearerAuth: [] }],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/VehiculoCrearRequest' }
            }
          }
        },
        responses: {
          201: {
            description: 'Vehículo creado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Vehiculo' }
              }
            }
          }
        }
      }
    },
    '/api/vehiculos/activos': {
      get: {
        tags: ['Vehículos'],
        summary: 'Listar solo vehículos activos (Catálogo)',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Lista de vehículos activos',
            content: {
              'application/json': {
                schema: {
                  type: 'array',
                  items: { $ref: '#/components/schemas/Vehiculo' }
                }
              }
            }
          }
        }
      }
    },
    '/api/vehiculos/{id}': {
      get: {
        tags: ['Vehículos'],
        summary: 'Obtener vehículo por ID',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: {
            description: 'Vehículo encontrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Vehiculo' }
              }
            }
          },
          404: { description: 'Vehículo no encontrado' }
        }
      },
      put: {
        tags: ['Vehículos'],
        summary: 'Modificar vehículo (Admin)',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/VehiculoCrearRequest' }
            }
          }
        },
        responses: {
          200: { description: 'Vehículo actualizado' }
        }
      },
      delete: {
        tags: ['Vehículos'],
        summary: 'Baja lógica de vehículo (Admin)',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: { description: 'Vehículo inhabilitado' }
        }
      }
    },
    '/api/clientes': {
      get: {
        tags: ['Clientes'],
        summary: 'Listar clientes (Admin)',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Lista de clientes',
            content: {
              'application/json': {
                schema: {
                  type: 'array',
                  items: { $ref: '#/components/schemas/Cliente' }
                }
              }
            }
          }
        }
      },
      post: {
        tags: ['Clientes'],
        summary: 'Alta de cliente (Registro/Admin)',
        security: [{ bearerAuth: [] }],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/ClienteCrearRequest' }
            }
          }
        },
        responses: {
          201: { description: 'Cliente creado' }
        }
      }
    },
    '/api/clientes/perfil': {
      get: {
        tags: ['Clientes'],
        summary: 'Obtener mi perfil (Cliente logueado)',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Perfil del cliente actual',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Cliente' }
              }
            }
          }
        }
      }
    },
    '/api/clientes/{id}': {
      get: {
        tags: ['Clientes'],
        summary: 'Obtener cliente por ID',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: { description: 'Cliente obtenido' },
          404: { description: 'Cliente no encontrado' }
        }
      },
      put: {
        tags: ['Clientes'],
        summary: 'Modificar cliente',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: { description: 'Cliente actualizado' }
        }
      },
      delete: {
        tags: ['Clientes'],
        summary: 'Baja lógica de cliente',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: { description: 'Cliente eliminado' }
        }
      }
    },
    '/api/reservas': {
      post: {
        tags: ['Reservas'],
        summary: 'Crear una nueva reserva (Orquestación gRPC + Rollback)',
        security: [{ bearerAuth: [] }],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/ReservaCrearRequest' }
            }
          }
        },
        responses: {
          201: {
            description: 'Reserva confirmada exitosamente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ReservaResponse' }
              }
            }
          },
          400: { description: 'Error de validación (inactivo, fechas inválidas, solapamiento)' },
          404: { description: 'Cliente o vehículo no encontrado' }
        }
      }
    },
    '/api/reservas/{id}/cancelar': {
      put: {
        tags: ['Reservas'],
        summary: 'Cancelar una reserva (Cliente)',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: { description: 'Reserva cancelada exitosamente' },
          400: { description: 'No se puede cancelar (ya inició o estado incompatible)' },
          404: { description: 'Reserva no encontrada' }
        }
      }
    }
  }
};
