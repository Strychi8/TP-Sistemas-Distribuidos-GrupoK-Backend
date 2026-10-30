package com.rentar.vehicle_service.service.impl;

import com.rentar.vehicle_service.dto.request.FiltroDisponibilidadDTO;
import com.rentar.vehicle_service.dto.request.VehiculoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoUpdateRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoResponseDTO;
import com.rentar.vehicle_service.enums.EstadoVehiculo;
import com.rentar.vehicle_service.exception.custom.BadRequestException;
import com.rentar.vehicle_service.exception.custom.ConflictException;
import com.rentar.vehicle_service.exception.custom.ResourceNotFoundException;
import com.rentar.vehicle_service.mapper.VehiculoMapper;
import com.rentar.vehicle_service.model.Vehiculo;
import com.rentar.vehicle_service.repository.IVehiculoRepository;
import com.rentar.vehicle_service.service.IVehiculoService;
import com.rentar.vehicle_service.validation.PatenteNormalizer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiculoServiceImpl implements IVehiculoService {
    private final IVehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoServiceImpl(IVehiculoRepository vehiculoRepository, VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Override
    @Transactional
    public VehiculoResponseDTO crearVehiculo(VehiculoRequestDTO dto) {
        dto.setPatente(PatenteNormalizer.normalizar(dto.getPatente()));

        // Evitamos validar previamente con existsByPatente() para prevenir condiciones de carrera.
        // Se inserta directamente confiando en la restricción UNIQUE de la base de datos.
        Vehiculo vehiculo = vehiculoMapper.toEntity(dto);

        try {
            Vehiculo guardado = vehiculoRepository.save(vehiculo);
            return vehiculoMapper.toDTO(guardado);
        } catch (DataIntegrityViolationException ex) {
            // Capturamos el error de restricción UNIQUE (patente duplicada) para lanzar un 409 Conflict.
            throw new ConflictException("Ya existe un vehículo con la patente: " + dto.getPatente());
        }
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizarVehiculo(Long id, VehiculoUpdateRequestDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        vehiculoMapper.updateEntityFromDTO(dto, vehiculo);
        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        return vehiculoMapper.toDTO(actualizado);
    }

    @Override
    public VehiculoResponseDTO actualizarEstado(Long id, EstadoVehiculo nuevoEstado) {
        if (nuevoEstado == null) {
            throw new BadRequestException("El estado del vehículo es obligatorio");
        }
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
        vehiculo.setEstado(nuevoEstado);
        return vehiculoMapper.toDTO(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtenerVehiculoPorId(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
        return vehiculoMapper.toDTO(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtenerVehiculoPorPatente(String patente) {
        String patenteNormalizada = PatenteNormalizer.normalizar(patente);
        Vehiculo vehiculo = vehiculoRepository.findByPatente(patenteNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con patente: " + patente));
        return vehiculoMapper.toDTO(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarVehiculos() {
        return vehiculoRepository.findAll()
                .stream()
                .map(vehiculoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarVehiculosActivos() {
        return vehiculoRepository.findByActivoTrueAndInhabilitadoFalse()
                .stream()
                .map(vehiculoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarVehiculo(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
    }

    @Override
    public List<VehiculoResponseDTO> consultarDisponibilidad(FiltroDisponibilidadDTO filtro) {
        return vehiculoRepository.buscarConFiltros(
                filtro.getTipoVehiculo(),
                filtro.getMarca(),
                filtro.getModelo(),
                filtro.getPrecioMin(),
                filtro.getPrecioMax()
        ).stream()
                .map(vehiculoMapper::toDTO)
                .collect(Collectors.toList());
    }
}
