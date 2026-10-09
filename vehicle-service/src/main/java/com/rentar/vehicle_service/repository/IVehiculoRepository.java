package com.rentar.vehicle_service.repository;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import com.rentar.vehicle_service.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatente(String patente);

    Optional<Vehiculo> findByPatente(String patente);

    List<Vehiculo> findByActivoTrue();

    List<Vehiculo> findByActivoTrueAndTipoVehiculo(TipoVehiculo tipoVehiculo);

}