package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.enums.TipoVehiculo;
import com.empresa_rentar.web_services.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
           "AND r.estado = com.empresa_rentar.web_services.enums.EstadoReserva.CONFIRMADA " +
           "AND r.fechaInicio < :fin " +
           "AND r.fechaFin > :inicio")
    boolean existsReservaSolapada(@Param("vehiculoId") Long vehiculoId,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);

    /**
     * Vehículos activos sin reservas CONFIRMADAS que se solapen con [inicio, fin],
     * aplicando los filtros opcionales (los parámetros null se ignoran).
     * Se soluciona el olapamiento: inicio_reserva < :fin AND fin_reserva > :inicio.
     */
    @Query("""
            SELECT v FROM Vehiculo v
            WHERE v.activo = TRUE
              AND NOT EXISTS (
                  SELECT r FROM Reserva r
                  WHERE r.vehiculo = v
                    AND r.estado = com.empresa_rentar.web_services.enums.EstadoReserva.CONFIRMADA
                    AND r.fechaInicio < :fin
                    AND r.fechaFin > :inicio
              )
              AND (:tipo IS NULL OR v.tipoVehiculo = :tipo)
              AND (:marca IS NULL OR v.marca LIKE CONCAT('%', :marca, '%'))
              AND (:modelo IS NULL OR v.modelo LIKE CONCAT('%', :modelo, '%'))
              AND (:precioMin IS NULL OR v.precioDiario >= :precioMin)
              AND (:precioMax IS NULL OR v.precioDiario <= :precioMax)
            """)
    List<Vehiculo> findDisponiblesEnRango(@Param("inicio") LocalDateTime inicio,
                                          @Param("fin") LocalDateTime fin,
                                          @Param("tipo") TipoVehiculo tipo,
                                          @Param("marca") String marca,
                                          @Param("modelo") String modelo,
                                          @Param("precioMin") BigDecimal precioMin,
                                          @Param("precioMax") BigDecimal precioMax);

}
