package com.rentar.rental_service.kafka.consumer;

import com.rentar.rental_service.kafka.event.MantenimientoResultadoEvent;
import com.rentar.rental_service.model.Reserva;
import com.rentar.rental_service.repository.IReservaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
public class MantenimientoResultadoConsumer {

    private final IReservaRepository reservaRepository;

    public MantenimientoResultadoConsumer(
            IReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @KafkaListener(
            topics = "mantenimiento.resultado",
            groupId = "rental-service",
            containerFactory = "mantenimientoResultadoKafkaListenerContainerFactory"
    )
    @Transactional
    public void consumirResultado(MantenimientoResultadoEvent event) {

        if (event == null
                || event.reservaId() == null
                || event.resultado() == null) {
            log.error("Evento de resultado de mantenimiento inválido");
            return;
        }

        if (!event.resultado().equals("REALIZADO")
                && !event.resultado().equals("NO_REALIZADO")) {
            log.error(
                    "Resultado de mantenimiento inválido: {}",
                    event.resultado()
            );
            return;
        }

        Reserva reserva = reservaRepository.findById(event.reservaId())
                .orElse(null);

        if (reserva == null) {
            log.warn(
                    "No existe la reserva {} para registrar el mantenimiento",
                    event.reservaId()
            );
            return;
        }

        if (Objects.equals(
                reserva.getResultadoMantenimiento(),
                event.resultado())) {
            log.info(
                    "El resultado {} ya estaba registrado para la reserva {}",
                    event.resultado(),
                    event.reservaId()
            );
            return;
        }

        reserva.setResultadoMantenimiento(event.resultado());
        reserva.setFechaResultadoMantenimiento(LocalDateTime.now());

        reservaRepository.save(reserva);

        log.info(
                "Resultado {} registrado para la reserva {}",
                event.resultado(),
                event.reservaId()
        );
    }
}