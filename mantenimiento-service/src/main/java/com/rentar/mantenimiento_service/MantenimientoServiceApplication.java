package com.rentar.mantenimiento_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // Habilita la ejecución de métodos de forma asíncrona
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MantenimientoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MantenimientoServiceApplication.class, args);
    }

}
