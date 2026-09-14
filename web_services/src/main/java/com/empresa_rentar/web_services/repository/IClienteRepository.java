package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("clienteRepository")
public interface IClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByDniAndIdClienteNot(String dni, Long idCliente);
}
