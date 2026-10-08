package com.rentar.customer_service.repository;

import com.rentar.customer_service.model.UsuarioRol;
import com.rentar.customer_service.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("usuarioRolRepository")
public interface IUsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
}
