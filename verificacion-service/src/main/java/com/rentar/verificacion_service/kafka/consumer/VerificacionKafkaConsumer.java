package com.rentar.verificacion_service.kafka.consumer;

import com.rentar.verificacion_service.config.KafkaConfig;
import com.rentar.verificacion_service.kafka.event.ValidacionPatenteEvent;
import com.rentar.verificacion_service.kafka.event.ValidacionTitularidadEvent;
import com.rentar.verificacion_service.kafka.event.VehiculoExternoRegistradoEvent;
import com.rentar.verificacion_service.kafka.event.VerificacionResultadoEvent;
import com.rentar.verificacion_service.kafka.event.VerificacionVehiculoEvent;
import com.rentar.verificacion_service.kafka.producer.VerificacionKafkaProducer;
import com.rentar.verificacion_service.service.IRegistroPolicialService;
import com.rentar.verificacion_service.service.IRegistroTitularidadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificacionKafkaConsumer {

    private final IRegistroPolicialService registroPolicialService;
    private final IRegistroTitularidadService registroTitularidadService;
    private final VerificacionKafkaProducer producer;

    /**
     * Consulta el registro policial para una solicitud de verificación.
     * Publica VALIDADO o ROBADO en el topic de resultados.
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_VERIFICACION_SOLICITADA, groupId = "${spring.kafka.consumer.group-id}")
    public void consumirVerificacionSolicitada(VerificacionVehiculoEvent event) {
        log.info("[KAFKA CONSUMER] Recibida solicitud de verificacion policial: vehiculoId={}, patente={}",
                event.getVehiculoId(), event.getPatente());

        try {
            boolean robado = registroPolicialService.esRobado(event.getPatente());
            String resultado = robado ? "ROBADO" : "VALIDADO";

            VerificacionResultadoEvent resultadoEvent = VerificacionResultadoEvent.builder()
                    .vehiculoId(event.getVehiculoId())
                    .patente(event.getPatente())
                    .resultado(resultado)
                    .build();

            log.info("[KAFKA CONSUMER] Emitiendo resultado de verificacion policial: {}", resultadoEvent);
            producer.publicarResultadoVerificacion(resultadoEvent);
        } catch (Exception e) {
            log.error("[KAFKA CONSUMER] Error técnico verificando vehiculoId={}: {}. Publicando ROBADO preventivamente.",
                    event.getVehiculoId(), e.getMessage());
            VerificacionResultadoEvent resultadoEvent = VerificacionResultadoEvent.builder()
                    .vehiculoId(event.getVehiculoId())
                    .patente(event.getPatente())
                    .resultado("ROBADO")
                    .build();
            producer.publicarResultadoVerificacion(resultadoEvent);
        }
    }

    /**
     * Valida la patente y la titularidad de un vehículo externo.
     * Publica un evento de resultado para cada validación.
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_EXTERNOS_REGISTRADOS, groupId = "${spring.kafka.consumer.group-id}")
    public void consumirVehiculoExternoRegistrado(VehiculoExternoRegistradoEvent event) {
        log.info("[KAFKA CONSUMER] Recibido registro de vehiculo externo: vehiculoExternoId={}, clienteId={}, patente={}",
                event.getVehiculoExternoId(), event.getClienteId(), event.getPatente());

        // Rechaza la patente si aparece en el registro policial.
        try {
            boolean robado = registroPolicialService.esRobado(event.getPatente());
            String resultadoPatente = robado ? "RECHAZADO" : "VALIDADO";

            ValidacionPatenteEvent patenteEvent = ValidacionPatenteEvent.builder()
                    .vehiculoExternoId(event.getVehiculoExternoId())
                    .patente(event.getPatente())
                    .resultado(resultadoPatente)
                    .build();

            producer.publicarValidacionPatente(patenteEvent);
        } catch (Exception e) {
            log.error("[KAFKA CONSUMER] Error técnico validando patente para vehiculoExternoId={}: {}. Publicando RECHAZADO.",
                    event.getVehiculoExternoId(), e.getMessage());
            ValidacionPatenteEvent patenteEvent = ValidacionPatenteEvent.builder()
                    .vehiculoExternoId(event.getVehiculoExternoId())
                    .patente(event.getPatente())
                    .resultado("RECHAZADO")
                    .build();
            producer.publicarValidacionPatente(patenteEvent);
        }

        // Comprueba que los datos del cliente coincidan con el registro de titularidad.
        try {
            IRegistroTitularidadService.ResultadoTitularidad resultadoTit =
                    registroTitularidadService.verificarTitularidad(
                            event.getClienteId(),
                            event.getDocumento(),
                            event.getPatente()
                    );

            String resultadoTitularidad = resultadoTit.valido() ? "VALIDADO" : "RECHAZADO";
            String documentoResuelto = resultadoTit.documento() != null && !resultadoTit.documento().isBlank()
                    ? resultadoTit.documento()
                    : (event.getDocumento() != null ? event.getDocumento() : "N/A");

            ValidacionTitularidadEvent titularidadEvent = ValidacionTitularidadEvent.builder()
                    .vehiculoExternoId(event.getVehiculoExternoId())
                    .clienteId(event.getClienteId())
                    .documento(documentoResuelto)
                    .patente(event.getPatente())
                    .resultado(resultadoTitularidad)
                    .build();

            producer.publicarValidacionTitularidad(titularidadEvent);
        } catch (Exception e) {
            log.error("[KAFKA CONSUMER] Error técnico validando titularidad para vehiculoExternoId={}: {}. Publicando RECHAZADO.",
                    event.getVehiculoExternoId(), e.getMessage());
            ValidacionTitularidadEvent titularidadEvent = ValidacionTitularidadEvent.builder()
                    .vehiculoExternoId(event.getVehiculoExternoId())
                    .clienteId(event.getClienteId())
                    .documento(event.getDocumento() != null ? event.getDocumento() : "N/A")
                    .patente(event.getPatente())
                    .resultado("RECHAZADO")
                    .build();
            producer.publicarValidacionTitularidad(titularidadEvent);
        }
    }
}
