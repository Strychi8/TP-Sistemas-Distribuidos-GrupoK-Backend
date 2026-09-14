package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("usuarioRepository")
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
    public boolean existsByEmail(String email);
}