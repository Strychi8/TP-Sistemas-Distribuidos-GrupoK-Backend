package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.model.UsuarioRol;
import com.empresa_rentar.web_services.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("usuarioRolRepository")
public interface IUsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
    List<UsuarioRol> findByUsuario_IdUsuario(Long idUsuario);
}
