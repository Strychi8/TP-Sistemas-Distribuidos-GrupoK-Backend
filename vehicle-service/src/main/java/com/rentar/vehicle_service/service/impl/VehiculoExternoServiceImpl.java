package com.rentar.vehicle_service.service.impl;

import com.rentar.vehicle_service.dto.request.VehiculoExternoRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoExternoResponseDTO;
import com.rentar.vehicle_service.enums.EstadoVehiculo;
import com.rentar.vehicle_service.exception.custom.BadRequestException;
import com.rentar.vehicle_service.exception.custom.ConflictException;
import com.rentar.vehicle_service.exception.custom.ResourceNotFoundException;
import com.rentar.vehicle_service.kafka.event.ValidacionPatenteEvent;
import com.rentar.vehicle_service.kafka.event.ValidacionTitularidadEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoCompletadaEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoRegistradoEvent;
import com.rentar.vehicle_service.mapper.VehiculoExternoMapper;
import com.rentar.vehicle_service.model.Vehiculo;
import com.rentar.vehicle_service.model.VehiculoExterno;
import com.rentar.vehicle_service.repository.IVehiculoExternoRepository;
import com.rentar.vehicle_service.repository.IVehiculoRepository;
import com.rentar.vehicle_service.service.IVehiculoExternoService;
import com.rentar.vehicle_service.validation.PatenteNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculoExternoServiceImpl implements IVehiculoExternoService {

    private final IVehiculoExternoRepository vehiculoExternoRepository;
    private final IVehiculoRepository vehiculoRepository;
    private final VehiculoExternoMapper vehiculoExternoMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public VehiculoExternoResponseDTO registrarVehiculoExterno(VehiculoExternoRequestDTO dto) {
        if (dto.getPrecioDiario() == null || dto.getPrecioDiario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio diario debe ser mayor a 0");
        }

        String patenteNormalizada = PatenteNormalizer.normalizar(dto.getPatente());
        dto.setPatente(patenteNormalizada);

        // Validar duplicados en flota interna (Vehiculo)
        if (vehiculoRepository.existsByPatente(patenteNormalizada)) {
            throw new ConflictException("Ya existe un vehículo oficial con la patente: " + patenteNormalizada);
        }

        // Validar duplicados en VehiculoExterno (PENDIENTE o APROBADO)
        if (vehiculoExternoRepository.existsByPatenteAndEstadoIn(patenteNormalizada, List.of("PENDIENTE", "APROBADO"))) {
            throw new ConflictException("Ya existe una solicitud pendiente o aprobada para la patente: " + patenteNormalizada);
        }

        VehiculoExterno externo = vehiculoExternoMapper.toEntity(dto);
        VehiculoExterno guardado = vehiculoExternoRepository.save(externo);

        // Publicar evento después del commit
        eventPublisher.publishEvent(VehiculoExternoRegistradoEvent.builder()
                .vehiculoExternoId(guardado.getIdVehiculoExterno())
                .clienteId(guardado.getClienteId())
                .documento(dto.getDocumento())
                .patente(guardado.getPatente())
                .build());

        log.info("Vehículo externo registrado con éxito en estado PENDIENTE: id={}, patente={}",
                guardado.getIdVehiculoExterno(), guardado.getPatente());

        return vehiculoExternoMapper.toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoExternoResponseDTO> listarVehiculosExternosPorCliente(Long clienteId) {
        return vehiculoExternoRepository.findByClienteId(clienteId).stream()
                .map(vehiculoExternoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void procesarValidacionPatente(ValidacionPatenteEvent event) {
        log.info("Procesando validación de patente para vehiculoExternoId={}, resultado={}",
                event.getVehiculoExternoId(), event.getResultado());

        Optional<VehiculoExterno> opt = vehiculoExternoRepository.findByIdForUpdate(event.getVehiculoExternoId());
        if (opt.isEmpty()) {
            log.warn("Vehículo externo no encontrado para validación de patente: id={}", event.getVehiculoExternoId());
            return;
        }

        VehiculoExterno externo = opt.get();
        if (event.getResultado().equals(externo.getValidacionPatente())) {
            return; // Sin cambios
        }

        externo.setValidacionPatente(event.getResultado());
        if ("RECHAZADO".equals(event.getResultado())) {
            externo.setEstado("RECHAZADO");
        }
        vehiculoExternoRepository.save(externo);

        intentarCorrelacionarYAprobar(externo.getIdVehiculoExterno());
    }

    @Override
    @Transactional
    public void procesarValidacionTitularidad(ValidacionTitularidadEvent event) {
        log.info("Procesando validación de titularidad para vehiculoExternoId={}, resultado={}",
                event.getVehiculoExternoId(), event.getResultado());

        Optional<VehiculoExterno> opt = vehiculoExternoRepository.findByIdForUpdate(event.getVehiculoExternoId());
        if (opt.isEmpty()) {
            log.warn("Vehículo externo no encontrado para validación de titularidad: id={}", event.getVehiculoExternoId());
            return;
        }

        VehiculoExterno externo = opt.get();
        if (event.getResultado().equals(externo.getValidacionTitularidad())) {
            return; // Sin cambios
        }

        externo.setValidacionTitularidad(event.getResultado());
        if ("RECHAZADO".equals(event.getResultado())) {
            externo.setEstado("RECHAZADO");
        }
        vehiculoExternoRepository.save(externo);

        intentarCorrelacionarYAprobar(externo.getIdVehiculoExterno());
    }

    private void intentarCorrelacionarYAprobar(Long vehiculoExternoId) {
        int afectadas = vehiculoExternoRepository.intentarAprobar(vehiculoExternoId);
        if (afectadas > 0) {
            log.info("Vehículo externo aprobado por correlación exitosa: id={}", vehiculoExternoId);
            eventPublisher.publishEvent(VehiculoExternoCompletadaEvent.builder()
                    .vehiculoExternoId(vehiculoExternoId)
                    .build());
        }
    }

    @Override
    @Transactional
    public void procesarCompletada(VehiculoExternoCompletadaEvent event) {
        log.info("Procesando evento completada para vehiculoExternoId={}", event.getVehiculoExternoId());

        Optional<VehiculoExterno> opt = vehiculoExternoRepository.findByIdForUpdate(event.getVehiculoExternoId());
        if (opt.isEmpty()) {
            log.warn("Vehículo externo no encontrado para completada: id={}", event.getVehiculoExternoId());
            return;
        }

        VehiculoExterno externo = opt.get();
        if (externo.getVehiculoId() != null) {
            log.info("Vehículo externo ya fue incorporado previamente: id={}, vehiculoId={}",
                    externo.getIdVehiculoExterno(), externo.getVehiculoId());
            return;
        }

        if (!"APROBADO".equals(externo.getEstado())) {
            log.warn("Vehículo externo no está APROBADO, estado actual: {}", externo.getEstado());
            return;
        }

        // Verificar si ya existe un Vehiculo oficial con la misma patente
        if (vehiculoRepository.findByPatente(externo.getPatente()).isPresent()) {
            log.warn("Ya existe un vehículo con patente {} al intentar completar alta externa. Marcando RECHAZADO.",
                    externo.getPatente());
            externo.setEstado("RECHAZADO");
            vehiculoExternoRepository.save(externo);
            return;
        }

        // Crear vehículo en la flota oficial sin disparar verificación interna
        Vehiculo vehiculoCreado = crearVehiculoDesdeExterno(externo);
        externo.setVehiculoId(vehiculoCreado.getIdVehiculo());
        vehiculoExternoRepository.save(externo);

        log.info("Vehículo externo incorporado exitosamente a la flota: idExterno={}, idVehiculo={}",
                externo.getIdVehiculoExterno(), vehiculoCreado.getIdVehiculo());
    }

    private Vehiculo crearVehiculoDesdeExterno(VehiculoExterno externo) {
        Vehiculo vehiculo = Vehiculo.builder()
                .patente(externo.getPatente())
                .marca(externo.getMarca())
                .modelo(externo.getModelo())
                .anio(externo.getAnio())
                .color(externo.getColor())
                .tipoVehiculo(externo.getTipoVehiculo())
                .precioDiario(externo.getPrecioDiario())
                .estado(EstadoVehiculo.DISPONIBLE)
                .activo(true)
                .inhabilitado(false)
                .build();

        return vehiculoRepository.save(vehiculo);
    }
}
