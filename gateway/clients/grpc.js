import path from 'path';
import { fileURLToPath } from 'url';
import grpc from '@grpc/grpc-js';
import protoLoader from '@grpc/proto-loader';
import dotenv from 'dotenv';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Configuración general para cargar los protos
const protoOptions = {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
};

// Rutas a los archivos proto
const PROTO_PATH_CUSTOMER = path.resolve(__dirname, '../../proto/customer.proto');
const PROTO_PATH_VEHICLE = path.resolve(__dirname, '../../proto/vehicle.proto');
const PROTO_PATH_RENTAL = path.resolve(__dirname, '../../proto/rental.proto');

// Cargar las definiciones
const packageDefinitionCustomer = protoLoader.loadSync(PROTO_PATH_CUSTOMER, protoOptions);
const packageDefinitionVehicle = protoLoader.loadSync(PROTO_PATH_VEHICLE, protoOptions);
const packageDefinitionRental = protoLoader.loadSync(PROTO_PATH_RENTAL, protoOptions);

// Obtener los objetos grpc cargados
const customerProto = grpc.loadPackageDefinition(packageDefinitionCustomer).rentar.customer;
const vehicleProto = grpc.loadPackageDefinition(packageDefinitionVehicle).rentar.vehicle;
const rentalProto = grpc.loadPackageDefinition(packageDefinitionRental).rentar.rental;

// Obtener las URLs de los servicios desde el archivo .env (con valores por defecto seguros)
const VEHICLE_URL = process.env.VEHICLE_SERVICE_URL || 'localhost:9091';
const CUSTOMER_URL = process.env.CUSTOMER_SERVICE_URL || 'localhost:9092';
const RENTAL_URL = process.env.RENTAL_SERVICE_URL || 'localhost:9093';

// Instanciar los clientes
export const customerClient = new customerProto.CustomerService(
  CUSTOMER_URL,
  grpc.credentials.createInsecure()
);

export const vehicleClient = new vehicleProto.VehicleService(
  VEHICLE_URL,
  grpc.credentials.createInsecure()
);

export const rentalClient = new rentalProto.RentalService(
  RENTAL_URL,
  grpc.credentials.createInsecure()
);

console.log('gRPC Clients initialized');
