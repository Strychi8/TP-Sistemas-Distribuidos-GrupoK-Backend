package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("rolRepository")
public interface IRolRepository extends JpaRepository<Rol, Long> {
    boolean existsByNombreRol(String nombreRol);
    Optional<Rol> findByNombreRol(String nombreRol);
}
