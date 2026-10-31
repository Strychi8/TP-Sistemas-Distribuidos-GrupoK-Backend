package com.rentar.rental_service.service.impl;

import com.rentar.rental_service.dto.request.FiltroReservaDTO;
import com.rentar.rental_service.dto.request.ReservaRequestDTO;
import com.rentar.rental_service.dto.response.ReservaGraphQLDTO;
import com.rentar.rental_service.dto.response.ReservaResponseDTO;
import com.rentar.rental_service.enums.EstadoReserva;
import com.rentar.rental_service.exception.custom.BadRequestException;
import com.rentar.rental_service.exception.custom.BusinessException;
import com.rentar.rental_service.exception.custom.ConflictException;
import com.rentar.rental_service.exception.custom.ResourceNotFoundException;
import com.rentar.rental_service.mapper.ReservaMapper;
import com.rentar.rental_service.model.Reserva;
import com.rentar.rental_service.repository.IReservaRepository;
import com.rentar.rental_service.service.IReservaService;
import com.rentar.rental_service.kafka.event.ReservaConfirmadaEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ReservaServiceImpl implements IReservaService {

    private final IReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final ApplicationEventPublisher eventPublisher;

    private long calcularDias(LocalDateTime inicio, LocalDateTime fin) {
        long horas = Duration.between(inicio, fin).toHours();
        return Math.max(1, (long) Math.ceil(horas / 24.0));
    }

    private BigDecimal calcularImporte(
            LocalDateTime inicio,
            LocalDateTime fin,
            BigDecimal precioDiario) {
        return precioDiario.multiply(BigDecimal.valueOf(calcularDias(inicio, fin)));
    }

    @Override
    @Transactional
    public ReservaResponseDTO crearReserva(ReservaRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("El cuerpo de la reserva es obligatorio");
        }
        if (dto.idCliente() == null || dto.idCliente() <= 0) {
            throw new BadRequestException("El ID de cliente debe ser un número positivo");
        }
        if (dto.idVehiculo() == null || dto.idVehiculo() <= 0) {
            throw new BadRequestException("El ID de vehículo debe ser un número positivo");
        }
        validarFechas(dto.fechaInicio(), dto.fechaFin());
        if (dto.precioDiario() == null || dto.precioDiario().signum() <= 0) {
            throw new BadRequestException("El precio diario debe ser mayor que cero");
        }

        if (reservaRepository.existeSolapamiento(
                dto.idVehiculo(), dto.fechaInicio(), dto.fechaFin(), EstadoReserva.CONFIRMADA)) {
            throw new ConflictException(
                    "El vehículo ya tiene una reserva confirmada en ese período");
        }

        Reserva reserva = Reserva.builder()
                .idCliente(dto.idCliente())
                .idVehiculo(dto.idVehiculo())
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .precioDiario(dto.precioDiario())
                .importeTotal(calcularImporte(dto.fechaInicio(), dto.fechaFin(), dto.precioDiario()))
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        Reserva reservaGuardada = reservaRepository.save(reserva);

        eventPublisher.publishEvent(new ReservaConfirmadaEvent(
                reservaGuardada.getIdReserva(),
                reservaGuardada.getIdVehiculo(),
                reservaGuardada.getFechaInicio(),
                reservaGuardada.getFechaFin()
        ));

        return reservaMapper.mapToResponseDTO(reservaGuardada);
    }

    private void validarFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de inicio debe ser futura");
        }
        if (!fin.isAfter(inicio)) {
            throw new BadRequestException("La fecha de fin debe ser posterior al inicio");
        }
    }

    @Override
    @Transactional
    public ReservaResponseDTO cancelarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con ID: " + idReserva));

        if (reserva.getEstado() == EstadoReserva.CANCELADA
                || reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new BusinessException(
                    "No se puede cancelar una reserva cancelada o finalizada");
        }

        if (!LocalDateTime.now().isBefore(reserva.getFechaInicio())) {
            throw new BusinessException(
                    "No se puede cancelar una reserva cuyo período ya comenzó");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaMapper.mapToResponseDTO(reservaRepository.save(reserva));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponseDTO obtenerPorId(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con ID: " + idReserva));

        return reservaMapper.mapToResponseDTO(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaGraphQLDTO> consultarHistorial(Long idCliente) {
        return reservaRepository.findHistorialByClienteId(
                        idCliente,
                        List.of(EstadoReserva.FINALIZADA, EstadoReserva.CANCELADA))
                .stream()
                .map(this::aGraphQLDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaGraphQLDTO> consultarReservas(FiltroReservaDTO filtro) {
        Specification<Reserva> spec = (root, query, cb) -> cb.conjunction();

        if (filtro != null) {
            if (filtro.getIdCliente() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("idCliente"), filtro.getIdCliente()));
            }
            if (filtro.getIdVehiculo() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("idVehiculo"), filtro.getIdVehiculo()));
            }
            if (filtro.getEstado() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("estado"), filtro.getEstado()));
            }
            if (filtro.getFechaDesde() != null && !filtro.getFechaDesde().isBlank()) {
                LocalDateTime desde = LocalDateTime.parse(filtro.getFechaDesde());
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("fechaInicio"), desde));
            }
            if (filtro.getFechaHasta() != null && !filtro.getFechaHasta().isBlank()) {
                LocalDateTime hasta = LocalDateTime.parse(filtro.getFechaHasta());
                spec = spec.and((root, query, cb) ->
                        cb.lessThanOrEqualTo(root.get("fechaFin"), hasta));
            }
        }

        return reservaRepository.findAll(spec).stream()
                .map(this::aGraphQLDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarSolapamiento(
            Long idVehiculo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (idVehiculo == null || idVehiculo <= 0) {
            throw new BadRequestException("El ID de vehículo debe ser un número positivo");
        }
        if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio)) {
            throw new BadRequestException("El período de fechas no es válido");
        }
        return reservaRepository.existeSolapamiento(
                idVehiculo, fechaInicio, fechaFin, EstadoReserva.CONFIRMADA);
    }

    private ReservaGraphQLDTO aGraphQLDTO(Reserva r) {
        return ReservaGraphQLDTO.builder()
                .idReserva(r.getIdReserva())
                .idCliente(r.getIdCliente())
                .idVehiculo(r.getIdVehiculo())
                .fechaInicio(r.getFechaInicio().toString())
                .fechaFin(r.getFechaFin().toString())
                .importeTotal(r.getImporteTotal())
                .precioDiario(r.getPrecioDiario())
                .estado(r.getEstado())
                .resultadoMantenimiento(r.getResultadoMantenimiento())
                .build();
    }
}
