package com.rentar.mantenimiento_service.repository;

import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitudMantenimientoRepository extends JpaRepository<SolicitudMantenimiento, Long> {

    // Método utilitario para consultar por el ID de la reserva
    Optional<SolicitudMantenimiento> findByReservaId(Long reservaId);
}
