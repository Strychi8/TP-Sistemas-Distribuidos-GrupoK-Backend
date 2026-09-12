package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.enums.TipoVehiculo;
import com.empresa_rentar.web_services.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatente(String patente);

    Optional<Vehiculo> findByPatente(String patente);

    List<Vehiculo> findByActivoTrue();

    List<Vehiculo> findByActivoTrueAndTipoVehiculo(TipoVehiculo tipoVehiculo);

    /**
     * Verifica si un vehículo tiene reservas CONFIRMADAS que se solapan con el rango dado.
     * Un vehículo está ocupado si existe una reserva tal que: inicio_reserva < :fin AND fin_reserva > :inicio
     */
    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
           "WHERE r.vehiculo.idVehiculo = :vehiculoId " +
           "AND r.estado = 'CONFIRMADA' " +
           "AND r.fechaInicio < :fin " +
           "AND r.fechaFin > :inicio")
    boolean existsReservaSolapada(@Param("vehiculoId") Long vehiculoId,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);
}
