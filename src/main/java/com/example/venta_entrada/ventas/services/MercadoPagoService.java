package com.example.venta_entrada.ventas.services;

import com.example.venta_entrada.ventas.models.Compra;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la integración directa con la pasarela de pagos Mercado Pago.
 * 
 * Funcionalidades:
 * 1. Creación de Preferencias de Pago (Checkout Pro / Sandbox).
 * 2. Consulta y verificación del estado de un pago específico.
 * 3. Procesamiento de reembolsos / devoluciones automáticas de dinero.
 */
@Service
public class MercadoPagoService {

    /**
     * URL base pública del backend donde Mercado Pago enviará las notificaciones IPN y retornos.
     */
    @Value("${app.backend-url:${APP_URL:https://ventaentradaappjava-back.onrender.com}}")
    private String appUrl;

    /**
     * Genera una preferencia de pago en Mercado Pago para una orden de compra.
     * 
     * Construye los ítems descriptivos agrupando entradas por tipo, configura las
     * URLs de retorno (éxito, pendiente, fallo) y la URL del Webhook IPN de notificación.
     * 
     * @param compra Entidad de la compra con sus entradas y monto total.
     * @return URL (sandbox_init_point o init_point) hacia la cual redirigir al usuario para pagar.
     * @throws RuntimeException si ocurre algún fallo de comunicación o validación con la API de Mercado Pago.
     */
    public String createPreference(Compra compra) {
        try {
            // Agrupar entradas por su TipoEntrada para generar un resumen claro en el checkout de MP
            var entradasByTipo = compra.getEntradas().stream()
                .collect(Collectors.groupingBy(e -> e.getTipoEntrada()));
                
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'DE' MMMM - HH:mm 'hs'", new java.util.Locale("es", "ES"));
            
            StringBuilder titleBuilder = new StringBuilder();
            String lastEventInfo = "";
            
            for (var entry : entradasByTipo.entrySet()) {
                var tipo = entry.getKey();
                int qty = entry.getValue().size();
                String eventName = tipo.getEvento().getNombre() != null ? tipo.getEvento().getNombre() : "";
                String fechaStr = tipo.getEvento().getFechaEvento().format(formatter).toUpperCase();
                
                String eventInfo = eventName + " (" + fechaStr + ")";
                
                if (titleBuilder.length() > 0) {
                    titleBuilder.append(", ");
                }
                titleBuilder.append(qty).append("x ").append(tipo.getNombre());
                lastEventInfo = eventInfo;
            }
            
            String mpTitle = titleBuilder.toString() + " - " + lastEventInfo;
            // Mercado Pago limita el título a 256 caracteres
            if (mpTitle.length() > 250) {
                mpTitle = mpTitle.substring(0, 247) + "...";
            }

            // Crear el ítem que se mostrará en el checkout
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", compra.getId().toString());
            item.put("title", mpTitle);
            item.put("description", "Entradas");
            item.put("quantity", 1);
            item.put("unit_price", compra.getTotal().doubleValue());
            item.put("currency_id", "ARS");
            
            List<java.util.Map<String, Object>> items = new ArrayList<>();
            items.add(item);

            // URLs a las que Mercado Pago redirige al cliente según el resultado
            java.util.Map<String, Object> backUrls = new java.util.HashMap<>();
            backUrls.put("success", appUrl + "/api/ventas/webhook/success");
            backUrls.put("pending", appUrl + "/api/ventas/webhook/pending");
            backUrls.put("failure", appUrl + "/api/ventas/webhook/failure");

            // Configuración general del payload de preferencia
            java.util.Map<String, Object> request = new java.util.HashMap<>();
            request.put("items", items);
            request.put("back_urls", backUrls);
            request.put("auto_return", "approved");
            request.put("external_reference", compra.getId().toString());
            request.put("notification_url", appUrl + "/api/ventas/webhook/ipn");

            // Llamada HTTP directa a Mercado Pago usando RestTemplate
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + com.mercadopago.MercadoPagoConfig.getAccessToken());
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(request, headers);
            
            org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.exchange(
                    "https://api.mercadopago.com/checkout/preferences",
                    org.springframework.http.HttpMethod.POST,
                    entity,
                    java.util.Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("sandbox_init_point");
            } else {
                throw new RuntimeException("Error al crear preferencia en Mercado Pago HTTP Status: " + response.getStatusCode());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("Error de API MP: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error al crear preferencia en Mercado Pago: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear preferencia en Mercado Pago", e);
        }
    }

    /**
     * Consulta y obtiene el detalle oficial de un pago en Mercado Pago mediante el SDK.
     * 
     * @param paymentId Identificador del pago en Mercado Pago.
     * @return Objeto {@link com.mercadopago.resources.payment.Payment} con el estado y datos del pago.
     * @throws RuntimeException si no se puede verificar con los servidores de Mercado Pago.
     */
    public com.mercadopago.resources.payment.Payment verificarPago(Long paymentId) {
        try {
            com.mercadopago.client.payment.PaymentClient client = new com.mercadopago.client.payment.PaymentClient();
            return client.get(paymentId);
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error al verificar el pago en Mercado Pago", e);
        }
    }

    /**
     * Ejecuta el reembolso o devolución total de un pago realizado en Mercado Pago.
     * 
     * @param paymentId Identificador único del pago a reembolsar.
     * @return Objeto {@link com.mercadopago.resources.payment.PaymentRefund} con la respuesta de la devolución.
     * @throws RuntimeException si la operación de devolución falla o es denegada.
     */
    public com.mercadopago.resources.payment.PaymentRefund procesarDevolucion(Long paymentId) {
        try {
            com.mercadopago.client.payment.PaymentRefundClient client = new com.mercadopago.client.payment.PaymentRefundClient();
            return client.refund(paymentId);
        } catch (MPApiException e) {
            String mpError = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            System.err.println("Error de API MP (Devolución): " + mpError);
            throw new RuntimeException("Error al procesar la devolución en Mercado Pago: " + mpError, e);
        } catch (MPException e) {
            throw new RuntimeException("Error al procesar la devolución en Mercado Pago: " + e.getMessage(), e);
        }
    }
}

