package com.rentar.vehicle_service.grpc;

import com.rentar.vehicle.grpc.*;
import com.rentar.vehicle_service.dto.request.FiltroDisponibilidadDTO;
import com.rentar.vehicle_service.dto.request.VehiculoExternoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoUpdateRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoExternoResponseDTO;
import com.rentar.vehicle_service.dto.response.VehiculoResponseDTO;
import com.rentar.vehicle_service.grpc.mapper.VehiculoGrpcMapper;
import com.rentar.vehicle_service.grpc.validation.RequestValidator;
import com.rentar.vehicle_service.service.IVehiculoExternoService;
import com.rentar.vehicle_service.service.IVehiculoService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
public class VehicleGrpcServiceImpl extends VehicleServiceGrpc.VehicleServiceImplBase {
    private final IVehiculoService vehiculoService;
    private final IVehiculoExternoService vehiculoExternoService;
    private final VehiculoGrpcMapper mapper;
    private final RequestValidator validator;

    public VehicleGrpcServiceImpl(IVehiculoService vehiculoService, IVehiculoExternoService vehiculoExternoService, VehiculoGrpcMapper mapper, RequestValidator validator) {
        this.vehiculoService = vehiculoService;
        this.vehiculoExternoService = vehiculoExternoService;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public void crearVehiculo(CrearVehiculoRequest request, StreamObserver<CrearVehiculoResponse> responseObserver) {
        VehiculoRequestDTO dto = mapper.toRequestDTO(request);
        validator.validate(dto);
        VehiculoResponseDTO creado = vehiculoService.crearVehiculo(dto);
        CrearVehiculoResponse response = CrearVehiculoResponse.newBuilder()
                .setVehicle(mapper.toProto(creado))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void actualizarVehiculo(ActualizarVehiculoRequest request, StreamObserver<ActualizarVehiculoResponse> responseObserver) {
        VehiculoUpdateRequestDTO dto = mapper.toUpdateDTO(request);
        validator.validate(dto);
        VehiculoResponseDTO actualizado = vehiculoService.actualizarVehiculo(request.getIdVehiculo(), dto);
        ActualizarVehiculoResponse response = ActualizarVehiculoResponse.newBuilder()
                .setVehicle(mapper.toProto(actualizado))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void actualizarEstadoVehiculo(ActualizarEstadoVehiculoRequest request, StreamObserver<ActualizarEstadoVehiculoResponse> responseObserver) {
        VehiculoResponseDTO actualizado = vehiculoService.actualizarEstado(
                request.getIdVehiculo(),
                mapper.toEstado(request.getEstado()));
        ActualizarEstadoVehiculoResponse response = ActualizarEstadoVehiculoResponse.newBuilder()
                .setVehicle(mapper.toProto(actualizado))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void eliminarVehiculo(EliminarVehiculoRequest request, StreamObserver<EliminarVehiculoResponse> responseObserver) {
        vehiculoService.eliminarVehiculo(request.getIdVehiculo());
        responseObserver.onNext(EliminarVehiculoResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculos(GetVehiculosRequest request, StreamObserver<GetVehiculosResponse> responseObserver) {
        List<VehiculoResponseDTO> vehiculos = vehiculoService.listarVehiculos();
        GetVehiculosResponse response = GetVehiculosResponse.newBuilder()
                .addAllVehicles(mapper.toProtoList(vehiculos))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculosActivos(GetVehiculosActivosRequest request, StreamObserver<GetVehiculosActivosResponse> responseObserver) {
        List<VehiculoResponseDTO> vehiculos = vehiculoService.listarVehiculosActivos();
        GetVehiculosActivosResponse response = GetVehiculosActivosResponse.newBuilder()
                .addAllVehicles(mapper.toProtoList(vehiculos))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculoById(GetVehiculoByIdRequest request, StreamObserver<GetVehiculoByIdResponse> responseObserver) {
        VehiculoResponseDTO vehiculo = vehiculoService.obtenerVehiculoPorId(request.getIdVehiculo());
        GetVehiculoByIdResponse response = GetVehiculoByIdResponse.newBuilder()
                .setVehicle(mapper.toProto(vehiculo))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculoByPatente(GetVehiculoByPatenteRequest request, StreamObserver<GetVehiculoByPatenteResponse> responseObserver) {
        VehiculoResponseDTO vehiculo = vehiculoService.obtenerVehiculoPorPatente(request.getPatente());
        GetVehiculoByPatenteResponse response = GetVehiculoByPatenteResponse.newBuilder()
                .setVehicle(mapper.toProto(vehiculo))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void consultarDisponibilidad(ConsultarDisponibilidadRequest request, StreamObserver<ConsultarDisponibilidadResponse> responseObserver) {
        FiltroDisponibilidadDTO filtro = mapper.toFiltroDTO(request);
        List<VehiculoResponseDTO> vehiculos = vehiculoService.consultarDisponibilidad(filtro);
        ConsultarDisponibilidadResponse response = ConsultarDisponibilidadResponse.newBuilder()
                .addAllVehicles(mapper.toProtoList(vehiculos))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registrarVehiculoExterno(RegistrarVehiculoExternoRequest request, StreamObserver<RegistrarVehiculoExternoResponse> responseObserver) {
        VehiculoExternoRequestDTO dto = mapper.toRequestDTO(request);
        validator.validate(dto);
        VehiculoExternoResponseDTO creado = vehiculoExternoService.registrarVehiculoExterno(dto);
        RegistrarVehiculoExternoResponse response = RegistrarVehiculoExternoResponse.newBuilder()
                .setVehiculoExterno(mapper.toProto(creado))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculosExternosByCliente(GetVehiculosExternosByClienteRequest request, StreamObserver<GetVehiculosExternosByClienteResponse> responseObserver) {
        List<VehiculoExternoResponseDTO> externos = vehiculoExternoService.listarVehiculosExternosPorCliente(request.getIdCliente());
        GetVehiculosExternosByClienteResponse response = GetVehiculosExternosByClienteResponse.newBuilder()
                .addAllVehiculosExternos(mapper.toProtoListExternos(externos))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
