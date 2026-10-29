package com.rentar.rental_service.grpc.mapper;

import com.google.protobuf.Timestamp;
import com.rentar.rental.grpc.CrearReservaRequest;
import com.rentar.rental.grpc.FiltroReserva;
import com.rentar.rental.grpc.RentalHistory;
import com.rentar.rental.grpc.Reservation;
import com.rentar.rental_service.dto.request.FiltroReservaDTO;
import com.rentar.rental_service.dto.request.ReservaRequestDTO;
import com.rentar.rental_service.dto.response.ReservaGraphQLDTO;
import com.rentar.rental_service.dto.response.ReservaResponseDTO;
import com.rentar.rental_service.enums.EstadoReserva;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class ReservaGrpcMapper {
    // ---------- proto -> dominio ----------

    public ReservaRequestDTO toCrearReservaDTO(CrearReservaRequest request) {
        // importeTotal del proto se ignora: el service lo recalcula (precioDiario * dias).
        return new ReservaRequestDTO(
                request.getIdCliente(),
                request.getIdVehiculo(),
                toLocalDateTime(request.getFechaInicio()),
                toLocalDateTime(request.getFechaFin()),
                BigDecimal.valueOf(request.getPrecioDiario()));
    }

    public FiltroReservaDTO toFiltroDTO(FiltroReserva proto) {
        FiltroReservaDTO dto = new FiltroReservaDTO();
        if (proto.hasIdCliente()) {
            dto.setIdCliente(proto.getIdCliente());
        }
        if (proto.hasIdVehiculo()) {
            dto.setIdVehiculo(proto.getIdVehiculo());
        }
        if (proto.hasEstado()
                && proto.getEstado() != com.rentar.rental.grpc.EstadoReserva.ESTADO_RESERVA_UNSPECIFIED) {
            dto.setEstado(toEstado(proto.getEstado()));
        }
        if (proto.hasFechaDesde()) {
            dto.setFechaDesde(toLocalDateTime(proto.getFechaDesde()).toString());
        }
        if (proto.hasFechaHasta()) {
            dto.setFechaHasta(toLocalDateTime(proto.getFechaHasta()).toString());
        }
        return dto;
    }

    public LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return LocalDateTime.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos(),
                ZoneOffset.UTC);
    }

    public EstadoReserva toEstado(com.rentar.rental.grpc.EstadoReserva proto) {
        return switch (proto) {
            case CONFIRMADA -> EstadoReserva.CONFIRMADA;
            case CANCELADA -> EstadoReserva.CANCELADA;
            case FINALIZADA -> EstadoReserva.FINALIZADA;
            default -> null; // UNSPECIFIED / UNRECOGNIZED: sin contraparte de dominio
        };
    }

    // ---------- dominio -> proto ----------

    public Timestamp toTimestamp(LocalDateTime fecha) {
        return Timestamp.newBuilder()
                .setSeconds(fecha.toEpochSecond(ZoneOffset.UTC))
                .setNanos(fecha.getNano())
                .build();
    }

    public Reservation toProto(ReservaResponseDTO dto) {
        return Reservation.newBuilder()
                .setIdReserva(dto.idReserva())
                .setIdCliente(dto.idCliente())
                .setIdVehiculo(dto.idVehiculo())
                .setFechaInicio(toTimestamp(dto.fechaInicio()))
                .setFechaFin(toTimestamp(dto.fechaFin()))
                .setImporteTotal(dto.importeTotal().doubleValue())
                .setPrecioDiario(dto.precioDiario().doubleValue())
                .setEstado(toProtoEstado(dto.estadoReserva()))
                .build();
    }

    public Reservation toProto(ReservaGraphQLDTO dto) {
        return Reservation.newBuilder()
                .setIdReserva(dto.getIdReserva())
                .setIdCliente(dto.getIdCliente())
                .setIdVehiculo(dto.getIdVehiculo())
                .setFechaInicio(toTimestamp(LocalDateTime.parse(dto.getFechaInicio())))
                .setFechaFin(toTimestamp(LocalDateTime.parse(dto.getFechaFin())))
                .setImporteTotal(dto.getImporteTotal().doubleValue())
                .setPrecioDiario(dto.getPrecioDiario().doubleValue())
                .setEstado(toProtoEstado(dto.getEstado()))
                .setResultadoMantenimiento(
                    dto.getResultadoMantenimiento() != null
                            ? dto.getResultadoMantenimiento()
                            : "")
                .build();
    }

    public RentalHistory toHistory(ReservaGraphQLDTO dto) {
        LocalDateTime inicio = LocalDateTime.parse(dto.getFechaInicio());
        LocalDateTime fin = LocalDateTime.parse(dto.getFechaFin());
        long dias = Math.max(1, (long) Math.ceil(Duration.between(inicio, fin).toHours() / 24.0));
        return RentalHistory.newBuilder()
                .setIdVehiculo(dto.getIdVehiculo())
                .setFechaInicio(toTimestamp(inicio))
                .setFechaFinalizacion(toTimestamp(fin))
                .setCantidadDias((int) Math.min(dias, Integer.MAX_VALUE))
                .setImporteTotal(dto.getImporteTotal().doubleValue())
                .setEstado(toProtoEstado(dto.getEstado()))
                .build();
    }

    public com.rentar.rental.grpc.EstadoReserva toProtoEstado(EstadoReserva estado) {
        return switch (estado) {
            case CONFIRMADA -> com.rentar.rental.grpc.EstadoReserva.CONFIRMADA;
            case CANCELADA -> com.rentar.rental.grpc.EstadoReserva.CANCELADA;
            case FINALIZADA -> com.rentar.rental.grpc.EstadoReserva.FINALIZADA;
        };
    }
}
