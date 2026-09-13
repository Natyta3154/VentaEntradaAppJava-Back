package com.example.venta_entrada.core.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de bienvenida y Health Check del API.
 * Permite verificar que el backend está online al abrir la URL base.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ONLINE");
        response.put("aplicacion", "API Venta de Entradas y Control de Accesos");
        response.put("version", "1.0.0");
        response.put("endpoints_publicos", Map.of(
            "eventos", "/api/eventos",
            "artistas", "/api/artistas",
            "contacto", "/api/contactos"
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
