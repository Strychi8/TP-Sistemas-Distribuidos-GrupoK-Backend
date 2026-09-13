package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.dto.request.VehiculoRequestDTO;
import com.empresa_rentar.web_services.dto.request.VehiculoUpdateRequestDTO;
import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;
import com.empresa_rentar.web_services.exception.custom.ConflictException;
import com.empresa_rentar.web_services.exception.custom.ResourceNotFoundException;
import com.empresa_rentar.web_services.mapper.VehiculoMapper;
import com.empresa_rentar.web_services.model.Vehiculo;
import com.empresa_rentar.web_services.repository.IVehiculoRepository;
import com.empresa_rentar.web_services.service.VehiculoService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final IVehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoServiceImpl(IVehiculoRepository vehiculoRepository, VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Override
    @Transactional
    public VehiculoResponseDTO crearVehiculo(VehiculoRequestDTO dto) {
        // Se elimina la validación previa existsByPatente() para evitar condiciones de carrera:
        // si dos peticiones concurrentes verifican al mismo tiempo que la patente no existe,
        // ambas pasarían la validación e intentarían insertar, generando un error genérico.
        // En su lugar, se intenta el insert directamente y se captura la excepción de
        // restricción unique de la base de datos para mapearla a una respuesta HTTP 409.
        Vehiculo vehiculo = vehiculoMapper.toEntity(dto);

        try {
            Vehiculo guardado = vehiculoRepository.save(vehiculo);
            return vehiculoMapper.toDTO(guardado);
        } catch (DataIntegrityViolationException ex) {
            // La restricción UNIQUE en la columna 'patente' de la tabla 'vehiculos' lanza
            // DataIntegrityViolationException cuando se intenta insertar un duplicado.
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
    @Transactional(readOnly = true)
    public VehiculoResponseDTO buscarPorId(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
        return vehiculoMapper.toDTO(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO buscarPorPatente(String patente) {
        Vehiculo vehiculo = vehiculoRepository.findByPatente(patente)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con patente: " + patente));
        return vehiculoMapper.toDTO(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarTodos() {
        return vehiculoRepository.findAll()
                .stream()
                .map(vehiculoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarActivos() {
        return vehiculoRepository.findByActivoTrue()
                .stream()
                .map(vehiculoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
    }
}
