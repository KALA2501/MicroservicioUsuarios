package com.usuarios.demo.config;

import com.usuarios.demo.controllers.AdminController;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestControllerOnlyConfig {

    @Bean
    public AdminController adminController() {
        return new AdminController();
    }

}
