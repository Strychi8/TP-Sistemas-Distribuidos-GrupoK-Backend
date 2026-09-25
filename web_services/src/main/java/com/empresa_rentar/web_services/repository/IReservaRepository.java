package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.enums.EstadoReserva;
import com.empresa_rentar.web_services.model.Reserva;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IReservaRepository extends CrudRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva>{
    /**
     * Verifica si existe al menos una reserva CONFIRMADA para el mismo vehículo
     * que se solape temporalmente con el rango de fechas solicitado.
     *
     * @param idVehiculo
     * @param fechaInicio
     * @param fechaFin
     * @param estado
     * @return boolean
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reserva r
            WHERE r.vehiculo.idVehiculo = :idVehiculo
            AND r.estado = :estado
            AND (:fechaInicio < r.fechaFin AND :fechaFin > r.fechaInicio)
            """)
    boolean existeSolapamiento(
            @Param("idVehiculo") Long idVehiculo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("estado") EstadoReserva estado
    );

    @Query("SELECT r FROM Reserva r WHERE r.cliente.idCliente = :idCliente")
    List<Reserva> findByClienteId(Long idCliente);

    /**
    * Obtiene el historial de reservas de un cliente,
    * incluyendo únicamente las reservas que se encuentran
    * en los estados indicados.
    *
    * @param idCliente identificador del cliente
    * @param estados estados de reserva que se desean incluir
    * @return lista de reservas del historial, ordenadas por fecha de inicio descendente
    */
    @Query("""
            SELECT r
            FROM Reserva r
            WHERE r.cliente.idCliente = :idCliente
            AND r.estado IN :estados
            ORDER BY r.fechaInicio DESC
            """)
    List<Reserva> findHistorialByClienteId(
            @Param("idCliente") Long idCliente,
            @Param("estados") List<EstadoReserva> estados
    );
    
     @Query("""
            SELECT r FROM Reserva r
            WHERE r.estado = :estado
            AND r.fechaFin <= :fechaActual
            """)
    List<Reserva> findReservasVencidas(
            @Param("estado") EstadoReserva estado,
            @Param("fechaActual") LocalDateTime fechaActual
    );

    @Query("""
            SELECT r FROM Reserva r
            WHERE r.estado = :estado
            AND r.fechaInicio <= :fechaActual
            AND r.fechaFin > :fechaActual
            """)
    List<Reserva> findReservasEnCurso(
            @Param("estado") EstadoReserva estado,
            @Param("fechaActual") LocalDateTime fechaActual
    );
}
