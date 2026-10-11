package com.rentar.vehicle_service.repository;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import com.rentar.vehicle_service.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatente(String patente);

    Optional<Vehiculo> findByPatente(String patente);

    List<Vehiculo> findByActivoTrue();

    List<Vehiculo> findByActivoTrueAndTipoVehiculo(TipoVehiculo tipoVehiculo);

    @Query("""
            SELECT v FROM Vehiculo v
            WHERE v.activo = true
              AND (:tipoVehiculo IS NULL OR v.tipoVehiculo = :tipoVehiculo)
              AND (:marca IS NULL OR LOWER(v.marca) LIKE LOWER(CONCAT('%', :marca, '%')))
              AND (:modelo IS NULL OR LOWER(v.modelo) LIKE LOWER(CONCAT('%', :modelo, '%')))
              AND (:precioMin IS NULL OR v.precioDiario >= :precioMin)
              AND (:precioMax IS NULL OR v.precioDiario <= :precioMax)
            """)
    List<Vehiculo> buscarConFiltros(@Param("tipoVehiculo") TipoVehiculo tipoVehiculo,
                                    @Param("marca") String marca,
                                    @Param("modelo") String modelo,
                                    @Param("precioMin") Double precioMin,
                                    @Param("precioMax") Double precioMax);

}