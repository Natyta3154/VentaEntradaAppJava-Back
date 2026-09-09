package com.example.venta_entrada.ventas.controllers;

import com.example.venta_entrada.ventas.services.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas/webhook")
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookController {

    private final VentaService ventaService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/ipn")
    public ResponseEntity<String> handleIpn(@RequestParam Map<String, String> allParams, @RequestBody(required = false) String body) {
        log.debug("IPN Received params: {}", allParams);
        // Evitamos loguear todo el body en producción para no exponer PII o secrets
        log.trace("IPN Received body: {}", body);
        
        String topic = allParams.get("topic");
        String idParam = allParams.get("id");
        String typeParam = allParams.get("type");
        String dataIdParam = allParams.get("data.id");
        
        Long paymentId = null;
        
        if ("payment".equals(topic) && idParam != null) {
            paymentId = Long.valueOf(idParam);
        } else if ("payment".equals(typeParam) && dataIdParam != null) {
            paymentId = Long.valueOf(dataIdParam);
        }
        
        if (paymentId != null) {
            try {
                log.info("Procesando notificacion de pago ID: {}", paymentId);
                ventaService.procesarNotificacionPago(paymentId);
                log.info("Pago ID {} procesado correctamente", paymentId);
            } catch (Exception e) {
                log.error("Error procesando pago {}", paymentId, e);
            }
        }
        
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/success")
    public ResponseEntity<Void> success() {
        return ResponseEntity.status(302).header("Location", frontendUrl).build();
    }

    @GetMapping("/pending")
    public ResponseEntity<Void> pending() {
        return ResponseEntity.status(302).header("Location", frontendUrl).build();
    }

    @GetMapping("/failure")
    public ResponseEntity<Void> failure() {
        return ResponseEntity.status(302).header("Location", frontendUrl).build();
    }
}
