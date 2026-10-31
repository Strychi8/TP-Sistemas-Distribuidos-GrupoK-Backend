package com.rentar.rental_service.kafka.event;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MantenimientoResultadoEventTest {

    @Test
    void debeSerializarYDeserializarResultadoNulo() {
        MantenimientoResultadoEvent original =
                new MantenimientoResultadoEvent(1L, 10L, null);

        byte[] bytes;

        try (JacksonJsonSerializer<MantenimientoResultadoEvent> serializer =
                     new JacksonJsonSerializer<>()) {
            bytes = serializer.serialize("mantenimiento.resultado", original);
        }

        String json = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(json.contains("\"resultado\":null"), json);

        try (JacksonJsonDeserializer<MantenimientoResultadoEvent> deserializer =
                     new JacksonJsonDeserializer<>(
                             MantenimientoResultadoEvent.class, false)) {

            MantenimientoResultadoEvent recuperado =
                    deserializer.deserialize("mantenimiento.resultado", bytes);

            assertNotNull(recuperado);
            assertEquals(1L, recuperado.reservaId());
            assertEquals(10L, recuperado.vehiculoId());
            assertNull(recuperado.resultado());
        }
    }
}
