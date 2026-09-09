package com.example.venta_entrada.ventas.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidacionQrResponseDTO {
    private String status; // 'verde' | 'rojo'
    private String mensaje;
    private ValidationDetails detalles;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationDetails {
        private String usuario;
        private String email;
        private String tipo;
        private String evento;
    }
}
