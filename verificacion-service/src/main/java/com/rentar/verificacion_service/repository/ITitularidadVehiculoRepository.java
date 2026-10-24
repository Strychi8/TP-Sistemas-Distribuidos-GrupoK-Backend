package com.rentar.verificacion_service.repository;

import com.rentar.verificacion_service.model.TitularidadVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITitularidadVehiculoRepository extends JpaRepository<TitularidadVehiculo, Long> {
    Optional<TitularidadVehiculo> findByPatenteIgnoreCase(String patente);
    Optional<TitularidadVehiculo> findByClienteIdAndPatenteIgnoreCase(Long clienteId, String patente);
    Optional<TitularidadVehiculo> findByDocumentoAndPatenteIgnoreCase(String documento, String patente);
}
