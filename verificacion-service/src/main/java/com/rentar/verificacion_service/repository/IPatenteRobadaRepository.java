package com.rentar.verificacion_service.repository;

import com.rentar.verificacion_service.model.PatenteRobada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPatenteRobadaRepository extends JpaRepository<PatenteRobada, Long> {
    boolean existsByPatenteIgnoreCase(String patente);
    Optional<PatenteRobada> findByPatenteIgnoreCase(String patente);
}
