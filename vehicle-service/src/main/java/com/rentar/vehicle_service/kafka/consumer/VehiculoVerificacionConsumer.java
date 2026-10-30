package com.rentar.vehicle_service.kafka.consumer;

import com.rentar.vehicle_service.kafka.event.VerificacionResultadoEvent;
import com.rentar.vehicle_service.model.Vehiculo;
import com.rentar.vehicle_service.repository.IVehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class VehiculoVerificacionConsumer {
    private final IVehiculoRepository vehiculoRepository;

    @KafkaListener(topics = "vehiculos.verificacion.resultado")
    public void onVerificacionResultado(VerificacionResultadoEvent event) {
        log.info("Resultado de verificación recibido: vehiculoId={}, patente={}, resultado={}", event.getVehiculoId(), event.getPatente(), event.getResultado());

        if (event.getVehiculoId()==null) {
            log.warn("Resultado de verificación sin vehiculoId, se descarta");
            return;
        }

        if (!"VALIDADO".equals(event.getResultado()) && !"ROBADO".equals(event.getResultado())) {
            log.warn("Resultado de verificación desconocido: {}", event.getResultado());
            return;
        }

        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(event.getVehiculoId());

        if (vehiculoOpt.isEmpty()) {
            log.warn("Resultado de verificación para vehículo inexistente: {}", event.getVehiculoId());
            return;
        }

        var vehiculo = vehiculoOpt.get();

        if ("ROBADO".equals(event.getResultado())) {
            boolean aplicado = vehiculoRepository.updateInhabilitado(event.getVehiculoId(), true, false) > 0;
            if (aplicado) {
                vehiculo.setInhabilitado(true);
                vehiculoRepository.save(vehiculo);
            }
        } else {
            boolean aplicado = vehiculoRepository.updateInhabilitado(event.getVehiculoId(), false, false) > 0;
            if (aplicado) {
                vehiculo.setInhabilitado(false);
                vehiculoRepository.save(vehiculo);
            }
        }
    }
}
