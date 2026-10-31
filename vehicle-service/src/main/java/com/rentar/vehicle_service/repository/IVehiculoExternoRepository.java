package com.rentar.vehicle_service.repository;

import com.rentar.vehicle_service.model.VehiculoExterno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVehiculoExternoRepository extends JpaRepository<VehiculoExterno, Long> {

    List<VehiculoExterno> findByClienteId(Long clienteId);

    Optional<VehiculoExterno> findByPatente(String patente);

    boolean existsByPatenteAndEstadoIn(String patente, List<String> estados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VehiculoExterno v WHERE v.idVehiculoExterno = :id")
    Optional<VehiculoExterno> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("""
            UPDATE VehiculoExterno v
            SET v.estado = 'APROBADO', v.fechaActualizacion = CURRENT_TIMESTAMP
            WHERE v.idVehiculoExterno = :id
              AND v.estado = 'PENDIENTE'
              AND v.validacionPatente = 'VALIDADO'
              AND v.validacionTitularidad = 'VALIDADO'
            """)
    int intentarAprobar(@Param("id") Long id);
}
