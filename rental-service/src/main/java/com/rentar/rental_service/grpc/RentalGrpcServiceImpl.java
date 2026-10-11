package com.rentar.rental_service.grpc;

import com.rentar.rental.grpc.CancelarReservaRequest;
import com.rentar.rental.grpc.ConsultarHistorialRequest;
import com.rentar.rental.grpc.CrearReservaRequest;
import com.rentar.rental.grpc.FiltroReserva;
import com.rentar.rental.grpc.GetReservaByIdRequest;
import com.rentar.rental.grpc.RentalHistoryList;
import com.rentar.rental.grpc.RentalServiceGrpc;
import com.rentar.rental.grpc.Reservation;
import com.rentar.rental.grpc.ReservationList;
import com.rentar.rental.grpc.SolapamientoRequest;
import com.rentar.rental.grpc.SolapamientoResponse;
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
    public void crearReserva(CrearReservaRequest request, StreamObserver<Reservation> observer) {
        observer.onNext(mapper.toProto(reservaService.crearReserva(mapper.toCrearReservaDTO(request))));
        observer.onCompleted();
    }

    @Override
    public void consultarReservas(FiltroReserva request, StreamObserver<ReservationList> observer) {
        ReservationList.Builder lista = ReservationList.newBuilder();
        reservaService.consultarReservas(mapper.toFiltroDTO(request))
                .forEach(r -> lista.addReservations(mapper.toProto(r)));
        observer.onNext(lista.build());
        observer.onCompleted();
    }

    @Override
    public void getReservaById(GetReservaByIdRequest request, StreamObserver<Reservation> observer) {
        observer.onNext(mapper.toProto(reservaService.obtenerPorId(request.getIdReserva())));
        observer.onCompleted();
    }

    @Override
    public void cancelarReserva(CancelarReservaRequest request, StreamObserver<Reservation> observer) {
        observer.onNext(mapper.toProto(reservaService.cancelarReserva(request.getIdReserva())));
        observer.onCompleted();
    }

    @Override
    public void consultarHistorialAlquileres(ConsultarHistorialRequest request, StreamObserver<RentalHistoryList> observer) {
        RentalHistoryList.Builder lista = RentalHistoryList.newBuilder();
        reservaService.consultarHistorial(request.getIdCliente())
                .forEach(r -> lista.addHistory(mapper.toHistory(r)));
        observer.onNext(lista.build());
        observer.onCompleted();
    }

    @Override
    public void verificarSolapamientoVehiculo(SolapamientoRequest request, StreamObserver<SolapamientoResponse> observer) {
        boolean solapado = reservaService.verificarSolapamiento(
                request.getIdVehiculo(),
                mapper.toLocalDateTime(request.getFechaInicio()),
                mapper.toLocalDateTime(request.getFechaFin()));
        observer.onNext(SolapamientoResponse.newBuilder().setSolapado(solapado).build());
        observer.onCompleted();
    }
}