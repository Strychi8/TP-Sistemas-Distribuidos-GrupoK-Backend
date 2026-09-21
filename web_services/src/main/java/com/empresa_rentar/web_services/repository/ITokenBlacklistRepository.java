package com.empresa_rentar.web_services.repository;

import com.empresa_rentar.web_services.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ITokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);
    boolean existsByToken(String token);
    void deleteByFechaExpiracionBefore(LocalDateTime fechaExpiracion);
}
