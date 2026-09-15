package com.example.venta_entrada.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS para Spring MVC.
 * Permite que el frontend (local o en producción) se comunique con la API incluyendo cookies.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String urlConfig = (frontendUrl != null) ? frontendUrl : "http://localhost:5173";
        String[] rawOrigins = urlConfig.split(",");
        java.util.List<String> validOrigins = new java.util.ArrayList<>();
        for (String origin : rawOrigins) {
            if (origin != null) {
                String clean = origin.trim().replaceAll("/+$", "");
                if (!clean.isEmpty()) {
                    validOrigins.add(clean);
                }
            }
        }
        String[] origins = validOrigins.toArray(new String[0]);

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

