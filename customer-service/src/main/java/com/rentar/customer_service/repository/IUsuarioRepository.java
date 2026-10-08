package com.rentar.customer_service.repository;

import com.rentar.customer_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("usuarioRepository")
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
    public boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);
}