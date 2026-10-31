export const swaggerDocument = {
  openapi: '3.0.0',
  info: {
    title: 'Rentar - API REST',
    version: '1.0.0',
    description: 'API REST para la gestión de vehículos, clientes y reservas de alquileres.',
    license: {
      name: 'Apache 2.0',
      url: 'https://www.apache.org/licenses/LICENSE-2.0'
    },
    contact: {
      name: 'Grupo K - Sistemas Distribuidos'
    }
  },
  externalDocs: {
    description: 'Repositorio del proyecto en GitHub',
    url: 'https://github.com/Strychi8/TP-Sistemas-Distribuidos-GrupoK-Backend'
  },
  servers: [
    {
      url: 'http://localhost:3000',
      description: 'Servidor local API Gateway'
    }
  ],
  tags: [
    {
      name: 'Autenticación',
      description: 'Endpoints para login y logout'
    },
    {
      name: 'Vehículos',
      description: 'Endpoints para la gestión de vehículos de la flota de Rentar'
    },
    {
      name: 'Clientes',
      description: 'Endpoints para la gestión de clientes'
    },
    {
      name: 'Reservas',
      description: 'Endpoints para la gestión transaccional de reservas de vehiculos'
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
          email: { type: 'string', example: 'admin@empresarentar.com' },
          password: { type: 'string', example: 'admin123' }
        }
      },
      LoginResponse: {
        type: 'object',
        properties: {
          token: { type: 'string', example: 'eyJhbGciOiJIUzI1Ni...' },
          email: { type: 'string', example: 'admin@empresarentar.com' },
          roles: {
            type: 'array',
            items: { type: 'string' },
            example: ['ADMINISTRADOR']
          }
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
          activo: { type: 'boolean', example: true },
          inhabilitado: { type: 'boolean', example: false }
        }
      },
      VehiculoExterno: {
        type: 'object',
        properties: {
          idVehiculoExterno: { type: 'integer', example: 1 },
          clienteId: { type: 'integer', example: 10 },
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
          estado: {
            type: 'string',
            enum: ['PENDIENTE', 'APROBADO', 'RECHAZADO'],
            example: 'PENDIENTE'
          },
          validacionPatente: {
            type: 'string',
            example: 'PENDIENTE'
          },
          validacionTitularidad: {
            type: 'string',
            example: 'PENDIENTE'
          },
          vehiculoId: { type: 'integer', example: 15 },
          precioDiario: { type: 'number', example: 45000.0 }
        }
      },
      VehiculoExternoCrearRequest: {
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
          telefono: { type: 'string', example: '1122334455' },
          fechaNacimiento: { type: 'string', example: '1990-05-15' },
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
        required: ['idVehiculo', 'fechaInicio', 'fechaFin'],
        properties: {
          idCliente: { type: 'integer', example: 1, description: 'Opcional (se infiere del usuario logueado)' },
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
          estadoReserva: { type: 'string', example: 'CONFIRMADA' },
          resultadoMantenimiento: { type: 'string', example: 'APROBADO', nullable: true }
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
    '/api/clientes/{id}': {
      get: {
        tags: ['Clientes'],
        summary: 'Obtener un cliente por ID',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: {
            description: 'Cliente obtenido correctamente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Cliente' }
              }
            }
          },
          404: { description: 'Cliente no encontrado' }
        }
      },
      put: {
        tags: ['Clientes'],
        summary: 'Actualizar un cliente',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/ClienteCrearRequest' }
            }
          }
        },
        responses: {
          200: {
            description: 'Cliente actualizado correctamente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Cliente' }
              }
            }
          },
          404: { description: 'Cliente no encontrado' }
        }
      },
      delete: {
        tags: ['Clientes'],
        summary: 'Eliminar un cliente',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: { description: 'Cliente eliminado correctamente' },
          404: { description: 'Cliente no encontrado' }
        }
      }
    },
    '/api/clientes': {
      get: {
        tags: ['Clientes'],
        summary: 'Listar todos los clientes',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Listado obtenido correctamente',
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
        summary: 'Crear un cliente',
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
          201: {
            description: 'Cliente creado correctamente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Cliente' }
              }
            }
          },
          400: { description: 'Datos inválidos o cliente ya existente' }
        }
      }
    },
    '/api/clientes/perfil': {
      get: {
        tags: ['Clientes'],
        summary: 'Obtener mi perfil',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Perfil del cliente actual obtenido correctamente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Cliente' }
              }
            }
          },
          404: { description: 'Cliente no encontrado' }
        }
      }
    },
    '/api/clientes/activos': {
      get: {
        tags: ['Clientes'],
        summary: 'Listar clientes activos',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Listado de clientes activos obtenido correctamente',
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
      }
    },
    '/api/auth/logout': {
      post: {
        tags: ['Autenticación'],
        summary: 'Cerrar sesión',
        security: [{ bearerAuth: [] }],
        responses: {
          200: { description: 'Sesión cerrada correctamente' },
          401: { description: 'No autenticado o token revocado' }
        }
      }
    },
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
    '/api/reservas/{id}/cancelar': {
      put: {
        tags: ['Reservas'],
        summary: 'Cancelar Reserva',
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
    },
    '/api/reservas': {
      post: {
        tags: ['Reservas'],
        summary: 'Alta de Reserva',
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
    '/api/reservas/{id}': {
      get: {
        tags: ['Reservas'],
        summary: 'Consultar Reserva por ID',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: {
            description: 'Reserva encontrada',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ReservaResponse' }
              }
            }
          },
          404: { description: 'Reserva no encontrada' }
        }
      }
    },
    '/api/vehiculos/{id}': {
      get: {
        tags: ['Vehículos'],
        summary: 'Buscar vehículo por ID',
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
        summary: 'Actualizar vehículo',
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
          200: {
            description: 'Vehículo actualizado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Vehiculo' }
              }
            }
          },
          404: { description: 'Vehículo no encontrado' }
        }
      },
      delete: {
        tags: ['Vehículos'],
        summary: 'Dar de baja vehículo',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' } }
        ],
        responses: {
          200: { description: 'Vehículo inhabilitado' },
          404: { description: 'Vehículo no encontrado' }
        }
      }
    },
    '/api/vehiculos': {
      get: {
        tags: ['Vehículos'],
        summary: 'Listar todos los vehículos',
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
        summary: 'Crear vehículo',
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
          },
          400: { description: 'Datos inválidos o patente duplicada' }
        }
      }
    },
    '/api/vehiculos/patente/{patente}': {
      get: {
        tags: ['Vehículos'],
        summary: 'Buscar vehículo por patente',
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'patente', in: 'path', required: true, schema: { type: 'string', example: 'AA123BB' } }
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
      }
    },
    '/api/vehiculos/activos': {
      get: {
        tags: ['Vehículos'],
        summary: 'Listar vehículos activos',
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
    '/api/vehiculos/externos': {
      post: {
        tags: ['Vehículos'],
        summary: 'Registrar solicitud de vehículo propio (externo)',
        description: 'Permite a un cliente autenticado solicitar la incorporación de su vehículo propio al catálogo de Rentar.',
        security: [{ bearerAuth: [] }],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/VehiculoExternoCrearRequest' }
            }
          }
        },
        responses: {
          201: {
            description: 'Solicitud de vehículo externo creada exitosamente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/VehiculoExterno' }
              }
            }
          },
          400: { description: 'Datos inválidos o cliente inactivo' },
          409: { description: 'Patente ya registrada en el sistema' }
        }
      },
      get: {
        tags: ['Vehículos'],
        summary: 'Listar solicitudes de vehículos externos del cliente autenticado',
        description: 'Devuelve la lista de solicitudes de vehículos propios realizadas por el cliente autenticado.',
        security: [{ bearerAuth: [] }],
        responses: {
          200: {
            description: 'Lista de solicitudes de vehículos externos del cliente',
            content: {
              'application/json': {
                schema: {
                  type: 'array',
                  items: { $ref: '#/components/schemas/VehiculoExterno' }
                }
              }
            }
          },
          400: { description: 'Cliente inactivo' },
          401: { description: 'No autenticado' }
        }
      }
    }
  }
};
