package com.rentar.mantenimiento_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // Habilita la ejecución de métodos de forma asíncrona
@SpringBootApplication
public class MantenimientoServiceApplication {

    public static void main(String[] args) {
      SpringApplication.run(MantenimientoServiceApplication.class, args);
    }

}
