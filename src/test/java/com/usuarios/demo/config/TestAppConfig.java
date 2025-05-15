package com.usuarios.demo.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.usuarios.demo.controllers" // Solo carga el controlador real
})
public class TestAppConfig {
}
