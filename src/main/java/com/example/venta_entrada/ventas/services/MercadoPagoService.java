package com.example.venta_entrada.ventas.services;

import com.example.venta_entrada.ventas.models.Compra;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MercadoPagoService {

    @Value("${APP_URL:http://localhost:8080}")
    private String appUrl;

    public String createPreference(Compra compra) {
        try {
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
            if (mpTitle.length() > 250) {
                mpTitle = mpTitle.substring(0, 247) + "...";
            }

            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", compra.getId().toString());
            item.put("title", mpTitle);
            item.put("description", "Entradas");
            item.put("quantity", 1);
            item.put("unit_price", compra.getTotal().doubleValue());
            item.put("currency_id", "ARS");
            
            List<java.util.Map<String, Object>> items = new ArrayList<>();
            items.add(item);

            java.util.Map<String, Object> backUrls = new java.util.HashMap<>();
            backUrls.put("success", appUrl + "/api/ventas/webhook/success");
            backUrls.put("pending", appUrl + "/api/ventas/webhook/pending");
            backUrls.put("failure", appUrl + "/api/ventas/webhook/failure");

            java.util.Map<String, Object> request = new java.util.HashMap<>();
            request.put("items", items);
            request.put("back_urls", backUrls);
            request.put("auto_return", "approved");
            request.put("external_reference", compra.getId().toString());
            request.put("notification_url", appUrl + "/api/ventas/webhook/ipn");

            System.out.println("Enviando a MP con APP_URL (Bypass SDK): " + appUrl);

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

    public com.mercadopago.resources.payment.Payment verificarPago(Long paymentId) {
        try {
            com.mercadopago.client.payment.PaymentClient client = new com.mercadopago.client.payment.PaymentClient();
            return client.get(paymentId);
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error al verificar el pago en Mercado Pago", e);
        }
    }

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
