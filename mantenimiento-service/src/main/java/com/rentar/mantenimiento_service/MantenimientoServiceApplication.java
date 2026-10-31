package com.rentar.mantenimiento_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada del microservicio responsable de gestionar el mantenimiento
 * preventivo asociado a las reservas confirmadas de Rentar.
 *
 * <p>Este servicio combina persistencia de solicitudes, procesamiento
 * asincrónico y comunicación basada en eventos mediante Kafka.</p>
 */
@EnableAsync // Habilita la ejecución de métodos de forma asíncrona
@SpringBootApplication
public class MantenimientoServiceApplication {

    public static void main(String[] args) {
      SpringApplication.run(MantenimientoServiceApplication.class, args);
    }

}
