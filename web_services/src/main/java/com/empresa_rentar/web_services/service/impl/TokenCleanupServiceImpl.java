package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.repository.ITokenBlacklistRepository;
import com.empresa_rentar.web_services.service.ITokenCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service("tokenCleanupService")
public class TokenCleanupServiceImpl implements ITokenCleanupService {
    private final ITokenBlacklistRepository tokenBlacklistRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    @Override
    public void purgarTokensExpirados() {
        LocalDateTime ahora = LocalDateTime.now();
        log.info("Iniciando tarea programada: Purga de tokens expirados en blacklist anterior a {}", ahora);
        tokenBlacklistRepository.deleteByFechaExpiracionBefore(ahora);
        log.info("Purga de tokens finalizada exitosamente.");
    }
}
