package com.rentar.vehicle_service.repository;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import com.rentar.vehicle_service.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatente(String patente);

    Optional<Vehiculo> findByPatente(String patente);

    List<Vehiculo> findByActivoTrueAndInhabilitadoFalse();

    List<Vehiculo> findByActivoTrueAndTipoVehiculoAndInhabilitadoFalse(TipoVehiculo tipoVehiculo);

    @Query("""
            SELECT v FROM Vehiculo v
            WHERE v.activo = true
              AND v.inhabilitado = false
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

    @Modifying
    @Query("""
            UPDATE Vehiculo v
            SET v.inhabilitado = :inhabilitado
            WHERE v.idVehiculo = :idVehiculo
                AND v.inhabilitado = :currentInhabilitado
            """)
    int updateInhabilitado(@Param("idVehiculo") Long idVehiculo,
                           @Param("inhabilitado") Boolean inhabilitado,
                           @Param("currentInhabilitado") Boolean currentInhabilitado);
}