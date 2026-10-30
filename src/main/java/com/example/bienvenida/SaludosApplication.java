package com.example.bienvenida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/* Clase principal: al ejecutarla arranca el servidor en http://localhost:8091/ */
@SpringBootApplication
public class SaludosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaludosApplication.class, args);
    }

}
