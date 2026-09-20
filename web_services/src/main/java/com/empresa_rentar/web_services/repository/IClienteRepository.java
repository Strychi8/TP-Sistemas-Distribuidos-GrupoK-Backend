package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("clienteRepository")
public interface IClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByDniAndIdClienteNot(String dni, Long idCliente);
    boolean existsByEmailAndIdClienteNot(String email, Long idCliente);
    List<Cliente> findAllByActivoTrue();
    Optional<Cliente> findByUsuario_IdUsuario(Long idUsuario);
}
