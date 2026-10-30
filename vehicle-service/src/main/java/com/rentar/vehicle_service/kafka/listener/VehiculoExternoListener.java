package com.rentar.vehicle_service.kafka.listener;

import com.rentar.vehicle_service.kafka.event.VehiculoExternoCompletadaEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoRegistradoEvent;
import com.rentar.vehicle_service.kafka.producer.VehiculoExternoProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class VehiculoExternoListener {

    private final VehiculoExternoProducer vehiculoExternoProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVehiculoExternoRegistrado(VehiculoExternoRegistradoEvent event) {
        vehiculoExternoProducer.enviarRegistrado(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVehiculoExternoCompletada(VehiculoExternoCompletadaEvent event) {
        vehiculoExternoProducer.enviarCompletada(event);
    }
}
