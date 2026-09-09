package com.example.venta_entrada.ventas.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CompraResponseDTO {
    private Long id;
    private String estado;
    private BigDecimal total;
    private String checkoutUrl;
}
