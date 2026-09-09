package com.example.venta_entrada.ventas.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EntradaResponseDTO {
    private Long id;
    private String eventoNombre;
    private String tipoEntradaNombre;
    private String estado;
    private String qrCodeBase64;
}
