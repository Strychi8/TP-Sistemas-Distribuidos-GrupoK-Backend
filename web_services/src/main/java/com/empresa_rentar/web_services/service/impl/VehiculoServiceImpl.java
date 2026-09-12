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
        // Validar que la patente sea única
        if (vehiculoRepository.existsByPatente(dto.getPatente())) {
            throw new ConflictException("Ya existe un vehículo con la patente: " + dto.getPatente());
        }

        Vehiculo vehiculo = vehiculoMapper.toEntity(dto);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        return vehiculoMapper.toDTO(guardado);
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
