package com.rentar.rental_service.service;

import com.rentar.rental_service.enums.EstadoReserva;
import com.rentar.rental_service.repository.IReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaExpirationScheduler {

    private final IReservaRepository reservaRepository;

    // Este metodo se ejecuta cada minuto (60000 ms) para finalizar reservas vencidas.

    @Scheduled(fixedDelayString = "${rental.reservations.expiration-check-ms:60000}")
    @Transactional
    public void finalizarReservasVencidas() {
        LocalDateTime fechaHoraActualUtc = LocalDateTime.now(ZoneOffset.UTC);
        var vencidas = reservaRepository.findReservasVencidas(
                EstadoReserva.CONFIRMADA,
                fechaHoraActualUtc);

        if (vencidas.isEmpty()) {
            return;
        }

        vencidas.forEach(reserva -> reserva.setEstado(EstadoReserva.FINALIZADA));
        reservaRepository.saveAll(vencidas);
        log.info("Se finalizaron {} reservas vencidas", vencidas.size());
    }
}
