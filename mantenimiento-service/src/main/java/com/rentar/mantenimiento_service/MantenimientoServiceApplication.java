package com.rentar.mantenimiento_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MantenimientoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MantenimientoServiceApplication.class, args);
    }

}
