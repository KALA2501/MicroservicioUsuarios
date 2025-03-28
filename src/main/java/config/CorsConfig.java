package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("*") // Permite todos los orígenes en desarrollo
                    .allowedMethods("*") // Permite todos los métodos
                    .allowedHeaders("*") // Permite todos los headers
                    .exposedHeaders("*") // Expone todos los headers
                    .maxAge(3600); // Cache preflight por 1 hora
            }
        };
    }
} 