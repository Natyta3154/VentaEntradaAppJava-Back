package com.example.venta_entrada.ventas.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminCompraResponseDTO {
    private Long id;
    private String usuarioEmail;
    private String usuarioNombre;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaCompra;
    private String mercadoPagoId;
}
