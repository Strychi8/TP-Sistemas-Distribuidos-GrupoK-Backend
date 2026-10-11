package com.rentar.vehicle_service.grpc;

import com.google.protobuf.Empty;
import com.rentar.vehicle.grpc.*;
import com.rentar.vehicle_service.dto.request.FiltroDisponibilidadDTO;
import com.rentar.vehicle_service.dto.request.VehiculoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoUpdateRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoResponseDTO;
import com.rentar.vehicle_service.grpc.mapper.VehiculoGrpcMapper;
import com.rentar.vehicle_service.grpc.validation.RequestValidator;
import com.rentar.vehicle_service.service.IVehiculoService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@GrpcService
public class VehicleGrpcServiceImpl extends VehicleServiceGrpc.VehicleServiceImplBase {
    private final IVehiculoService vehiculoService;
    private final VehiculoGrpcMapper mapper;
    private final RequestValidator validator;

    public VehicleGrpcServiceImpl(IVehiculoService vehiculoService, VehiculoGrpcMapper mapper, RequestValidator validator) {
        this.vehiculoService = vehiculoService;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public void crearVehiculo(CrearVehiculoRequest request, StreamObserver<Vehicle> responseObserver){
        VehiculoRequestDTO dto = mapper.toRequestDTO(request);
        validator.validate(dto);
        VehiculoResponseDTO creado = vehiculoService.crearVehiculo(dto);
        responseObserver.onNext(mapper.toProto(creado));
        responseObserver.onCompleted();
    }

    @Override
    public void actualizarVehiculo(ActualizarVehiculoRequest request, StreamObserver<Vehicle> responseObserver){
        VehiculoUpdateRequestDTO dto = mapper.toUpdateDTO(request);
        validator.validate(dto);
        VehiculoResponseDTO actualizado = vehiculoService.actualizarVehiculo(request.getIdVehiculo(), dto);
        responseObserver.onNext(mapper.toProto(actualizado));
        responseObserver.onCompleted();
    }

    @Override
    public void actualizarEstadoVehiculo(ActualizarEstadoRequest request, StreamObserver<Vehicle> responseObserver){
        VehiculoResponseDTO actualizado = vehiculoService.actualizarEstado(
                request.getIdVehiculo(),
                mapper.toEstado(request.getEstado()));
        responseObserver.onNext(mapper.toProto(actualizado));
        responseObserver.onCompleted();
    }

    @Override
    public void eliminarVehiculo(EliminarVehiculoRequest request, StreamObserver<EliminarVehiculoResponse> responseObserver){
        vehiculoService.eliminarVehiculo(request.getIdVehiculo());
        responseObserver.onNext(EliminarVehiculoResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculos(Empty request, StreamObserver<VehicleList> responseObserver) {
        List<VehiculoResponseDTO> vehiculos = vehiculoService.listarVehiculos();
        responseObserver.onNext(mapper.toProtoList(vehiculos));
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculosActivos(Empty request, StreamObserver<VehicleList> responseObserver) {
        List<VehiculoResponseDTO> vehiculos = vehiculoService.listarVehiculosActivos();
        responseObserver.onNext(mapper.toProtoList(vehiculos));
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculoById(GetVehiculoByIdRequest request, StreamObserver<Vehicle> responseObserver) {
        VehiculoResponseDTO vehiculo = vehiculoService.obtenerVehiculoPorId(request.getIdVehiculo());
        responseObserver.onNext(mapper.toProto(vehiculo));
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculoByPatente(GetVehiculoByPatenteRequest request, StreamObserver<Vehicle> responseObserver) {
        VehiculoResponseDTO vehiculo = vehiculoService.obtenerVehiculoPorPatente(request.getPatente());
        responseObserver.onNext(mapper.toProto(vehiculo));
        responseObserver.onCompleted();
    }

    @Override
    public void consultarDisponibilidad(FiltroDisponibilidad request, StreamObserver<VehicleList> responseObserver) {
        FiltroDisponibilidadDTO filtro = mapper.toFiltroDTO(request);
        List<VehiculoResponseDTO> vehiculos = vehiculoService.consultarDisponibilidad(filtro);
        responseObserver.onNext(mapper.toProtoList(vehiculos));
        responseObserver.onCompleted();
    }
}
