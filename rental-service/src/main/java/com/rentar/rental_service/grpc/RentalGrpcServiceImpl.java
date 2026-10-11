package com.rentar.rental_service.grpc;

import com.rentar.rental_service.dto.request.FiltroReservaDTO;
import com.rentar.rental_service.dto.response.ReservaGraphQLDTO;
import com.rentar.rental_service.dto.response.ReservaResponseDTO;
import com.rentar.rental_service.enums.EstadoReserva;
import com.rentar.rental_service.exception.custom.BusinessException;
import com.rentar.rental_service.exception.custom.ResourceNotFoundException;
import com.rentar.rental_service.service.IReservaService;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Duration;
import java.math.BigDecimal;

@GrpcService
public class RentalGrpcServiceImpl
        extends com.rentar.rental.grpc.RentalServiceGrpc.RentalServiceImplBase {

    private final IReservaService reservaService;

    public RentalGrpcServiceImpl(IReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Override
    public void crearReserva(
            com.rentar.rental.grpc.CrearReservaRequest request,
            StreamObserver<com.rentar.rental.grpc.Reservation> observer) {
        try {
            LocalDateTime inicio = toLocalDateTime(request.getFechaInicio());
            LocalDateTime fin = toLocalDateTime(request.getFechaFin());

            if (request.getIdCliente() <= 0 || request.getIdVehiculo() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Los IDs de cliente y vehículo deben ser positivos")
                        .asRuntimeException();
            }

            if (reservaService.verificarSolapamiento(
                    request.getIdVehiculo(), inicio, fin)) {
                throw Status.ALREADY_EXISTS
                        .withDescription("El vehículo ya tiene una reserva confirmada en ese período")
                        .asRuntimeException();
            }

            ReservaResponseDTO reserva = reservaService.crearReserva(
                    request.getIdCliente(),
                    request.getIdVehiculo(),
                    inicio,
                    fin,
                    BigDecimal.valueOf(request.getPrecioDiario()));

            observer.onNext(toReservation(reserva));
            observer.onCompleted();

        } catch (Exception e) {
            observer.onError(toGrpcError(e));
        }
    }

    @Override
    public void consultarReservas(
            com.rentar.rental.grpc.FiltroReserva request,
            StreamObserver<com.rentar.rental.grpc.ReservationList> observer) {
        try {
            if (request.hasTipoVehiculo()
                    && !request.getTipoVehiculo().isBlank()) {
                throw Status.INVALID_ARGUMENT
                        .withDescription(
                                "El filtro por tipo de vehículo no está disponible en rental-service")
                        .asRuntimeException();
            }

            FiltroReservaDTO filtro = new FiltroReservaDTO();

            if (request.hasIdCliente()) {
                filtro.setIdCliente(request.getIdCliente());
            }
            if (request.hasIdVehiculo()) {
                filtro.setIdVehiculo(request.getIdVehiculo());
            }
            if (request.hasEstado()) {
                filtro.setEstado(toEstadoReserva(request.getEstado()));
            }
            if (request.hasFechaDesde()) {
                filtro.setFechaDesde(
                        toLocalDateTime(request.getFechaDesde()).toString());
            }
            if (request.hasFechaHasta()) {
                filtro.setFechaHasta(
                        toLocalDateTime(request.getFechaHasta()).toString());
            }

            com.rentar.rental.grpc.ReservationList.Builder resultado =
                    com.rentar.rental.grpc.ReservationList.newBuilder();

            for (ReservaGraphQLDTO reserva :
                    reservaService.consultarReservas(filtro)) {
                resultado.addReservations(toReservation(reserva));
            }

            observer.onNext(resultado.build());
            observer.onCompleted();

        } catch (Exception e) {
            observer.onError(toGrpcError(e));
        }
    }

    @Override
    public void getReservaById(
            com.rentar.rental.grpc.GetReservaByIdRequest request,
            StreamObserver<com.rentar.rental.grpc.Reservation> observer) {
        try {
            ReservaResponseDTO reserva =
                    reservaService.obtenerPorId(request.getIdReserva());

            observer.onNext(toReservation(reserva));
            observer.onCompleted();

        } catch (Exception e) {
            observer.onError(toGrpcError(e));
        }
    }

    @Override
    public void cancelarReserva(
            com.rentar.rental.grpc.CancelarReservaRequest request,
            StreamObserver<com.rentar.rental.grpc.Reservation> observer) {
        try {
            ReservaResponseDTO reserva =
                    reservaService.cancelarReserva(request.getIdReserva());

            observer.onNext(toReservation(reserva));
            observer.onCompleted();

        } catch (Exception e) {
            observer.onError(toGrpcError(e));
        }
    }

    @Override
    public void consultarHistorialAlquileres(
            com.rentar.rental.grpc.ConsultarHistorialRequest request,
            StreamObserver<com.rentar.rental.grpc.RentalHistoryList> observer) {
        try {
            com.rentar.rental.grpc.RentalHistoryList.Builder resultado =
                    com.rentar.rental.grpc.RentalHistoryList.newBuilder();

            for (ReservaGraphQLDTO reserva :
                    reservaService.consultarHistorial(request.getIdCliente())) {

                LocalDateTime inicio = LocalDateTime.parse(reserva.getFechaInicio());
                LocalDateTime fin = LocalDateTime.parse(reserva.getFechaFin());

                long dias = Math.max(1,
                        (long) Math.ceil(Duration.between(inicio, fin).toHours() / 24.0));

                resultado.addHistory(
                        com.rentar.rental.grpc.RentalHistory.newBuilder()
                                .setVehiculo(reserva.getIdVehiculo())
                                .setPatente("")
                                .setFechaInicio(toTimestamp(inicio))
                                .setFechaFinalizacion(toTimestamp(fin))
                                .setCantidadDias((int) Math.min(dias, Integer.MAX_VALUE))
                                .setImporteTotal(reserva.getImporteTotal().doubleValue())
                                .setEstado(toGrpcEstado(reserva.getEstado()))
                                .build());
            }

            observer.onNext(resultado.build());
            observer.onCompleted();

        } catch (Exception e) {
            observer.onError(toGrpcError(e));
        }
    }

    @Override
    public void verificarSolapamientoVehiculo(
            com.rentar.rental.grpc.SolapamientoRequest request,
            StreamObserver<com.rentar.rental.grpc.SolapamientoResponse> observer) {
        try {
            boolean solapado = reservaService.verificarSolapamiento(
                    request.getIdVehiculo(),
                    toLocalDateTime(request.getFechaInicio()),
                    toLocalDateTime(request.getFechaFin()));

            observer.onNext(
                    com.rentar.rental.grpc.SolapamientoResponse.newBuilder()
                            .setSolapado(solapado)
                            .build());
            observer.onCompleted();

        } catch (Exception e) {
            observer.onError(toGrpcError(e));
        }
    }

    private com.rentar.rental.grpc.Reservation toReservation(
            ReservaResponseDTO r) {
        return com.rentar.rental.grpc.Reservation.newBuilder()
                .setIdReserva(r.idReserva())
                .setIdCliente(r.idCliente())
                .setIdVehiculo(r.idVehiculo())
                .setFechaInicio(toTimestamp(r.fechaInicio()))
                .setFechaFin(toTimestamp(r.fechaFin()))
                .setImporteTotal(r.importeTotal().doubleValue())
                .setPrecioDiario(r.precioDiario().doubleValue())
                .setEstado(toGrpcEstado(r.estadoReserva()))
                .build();
    }

    private com.rentar.rental.grpc.Reservation toReservation(
            ReservaGraphQLDTO r) {
        return com.rentar.rental.grpc.Reservation.newBuilder()
                .setIdReserva(r.getIdReserva())
                .setIdCliente(r.getIdCliente())
                .setIdVehiculo(r.getIdVehiculo())
                .setFechaInicio(toTimestamp(LocalDateTime.parse(r.getFechaInicio())))
                .setFechaFin(toTimestamp(LocalDateTime.parse(r.getFechaFin())))
                .setImporteTotal(r.getImporteTotal().doubleValue())
                .setPrecioDiario(r.getPrecioDiario().doubleValue())
                .setEstado(toGrpcEstado(r.getEstado()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return LocalDateTime.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos(),
                ZoneOffset.UTC);
    }

    private Timestamp toTimestamp(LocalDateTime fecha) {
        return Timestamp.newBuilder()
                .setSeconds(fecha.toEpochSecond(ZoneOffset.UTC))
                .setNanos(fecha.getNano())
                .build();
    }

    private com.rentar.rental.grpc.EstadoReserva toGrpcEstado(
            EstadoReserva estado) {
        return switch (estado) {
            case CONFIRMADA -> com.rentar.rental.grpc.EstadoReserva.CONFIRMADA;
            case CANCELADA -> com.rentar.rental.grpc.EstadoReserva.CANCELADA;
            case FINALIZADA -> com.rentar.rental.grpc.EstadoReserva.FINALIZADA;
        };
    }

    private EstadoReserva toEstadoReserva(
            com.rentar.rental.grpc.EstadoReserva estado) {
        return switch (estado) {
            case CONFIRMADA -> EstadoReserva.CONFIRMADA;
            case CANCELADA -> EstadoReserva.CANCELADA;
            case FINALIZADA -> EstadoReserva.FINALIZADA;
            default -> throw Status.INVALID_ARGUMENT
                    .withDescription("El estado de reserva no es válido")
                    .asRuntimeException();
        };
    }

    private io.grpc.StatusRuntimeException toGrpcError(Exception e) {
        if (e instanceof ResourceNotFoundException) {
            return Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException();
        }
        if (e instanceof BusinessException) {
            return Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException();
        }
        if (e instanceof io.grpc.StatusRuntimeException grpcException) {
            return grpcException;
        }
        return Status.INTERNAL
                .withDescription("Error interno al procesar la reserva")
                .withCause(e)
                .asRuntimeException();
    }
}