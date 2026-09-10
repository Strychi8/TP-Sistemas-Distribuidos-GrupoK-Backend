package com.empresa_rentar.web_services;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/saludo")
    public String saludo() {
        return "Hola Grupo K!";
    }
}