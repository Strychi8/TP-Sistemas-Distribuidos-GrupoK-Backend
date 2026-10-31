package com.rentar.vehicle_service.kafka.listener;

import com.rentar.vehicle_service.kafka.event.VerificacionVehiculoEvent;
import com.rentar.vehicle_service.kafka.producer.VehiculoVerificacionProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class VehiculoVerificacionListener {
    private final VehiculoVerificacionProducer vehiculoVerificacionProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificacionSolicitada(VerificacionVehiculoEvent event) {
        vehiculoVerificacionProducer.enviar(event);
    }
}
