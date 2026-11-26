package com.rentar.rental_service.grpc;

import com.rentar.rental.grpc.*;
import com.rentar.rental_service.grpc.mapper.ReservaGrpcMapper;
import com.rentar.rental_service.service.IReservaService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class RentalGrpcServiceImpl extends RentalServiceGrpc.RentalServiceImplBase {

    private final IReservaService reservaService;
    private final ReservaGrpcMapper mapper;

    public RentalGrpcServiceImpl(IReservaService reservaService, ReservaGrpcMapper mapper) {
        this.reservaService = reservaService;
        this.mapper = mapper;
    }

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<CrearReservaResponse> observer) {
        var reservaCreada = reservaService.crearReserva(mapper.toCrearReservaDTO(request));
        CrearReservaResponse response = CrearReservaResponse.newBuilder()
                .setReservation(mapper.toProto(reservaCreada))
                .build();
        observer.onNext(response);
        observer.onCompleted();
    }

    @Override
    public void consultarReservas(ConsultarReservasRequest request, StreamObserver<ConsultarReservasResponse> observer) {
        ConsultarReservasResponse.Builder lista = ConsultarReservasResponse.newBuilder();
        reservaService.consultarReservas(mapper.toFiltroDTO(request))
                .forEach(r -> lista.addReservations(mapper.toProto(r)));
        observer.onNext(lista.build());
        observer.onCompleted();
    }

    @Override
    public void getReservaById(GetReservaByIdRequest request, StreamObserver<GetReservaByIdResponse> observer) {
        GetReservaByIdResponse response = GetReservaByIdResponse.newBuilder()
                .setReservation(mapper.toProto(reservaService.obtenerPorId(request.getIdReserva())))
                .build();
        observer.onNext(response);
        observer.onCompleted();
    }

    @Override
    public void cancelarReserva(CancelarReservaRequest request, StreamObserver<CancelarReservaResponse> observer) {
        CancelarReservaResponse response = CancelarReservaResponse.newBuilder()
                .setReservation(mapper.toProto(reservaService.cancelarReserva(request.getIdReserva())))
                .build();
        observer.onNext(response);
        observer.onCompleted();
    }

    @Override
    public void consultarHistorialAlquileres(ConsultarHistorialAlquileresRequest request, StreamObserver<ConsultarHistorialAlquileresResponse> observer) {
        ConsultarHistorialAlquileresResponse.Builder lista = ConsultarHistorialAlquileresResponse.newBuilder();
        reservaService.consultarHistorial(request.getIdCliente())
                .forEach(r -> lista.addHistory(mapper.toHistory(r)));
        observer.onNext(lista.build());
        observer.onCompleted();
    }

    @Override
    public void verificarSolapamientoVehiculo(VerificarSolapamientoVehiculoRequest request, StreamObserver<VerificarSolapamientoVehiculoResponse> observer) {
        boolean solapado = reservaService.verificarSolapamiento(
                request.getIdVehiculo(),
                mapper.toLocalDateTime(request.getFechaInicio()),
                mapper.toLocalDateTime(request.getFechaFin()));
        observer.onNext(VerificarSolapamientoVehiculoResponse.newBuilder().setSolapado(solapado).build());
        observer.onCompleted();
    }
}