package com.rentar.customer_service.repository;


import com.rentar.customer_service.enums.NombreRol;
import com.rentar.customer_service.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("rolRepository")
public interface IRolRepository extends JpaRepository<Rol, Long> {
    boolean existsByNombreRol(NombreRol nombreRol);
    Optional<Rol> findByNombreRol(NombreRol nombreRol);
}
