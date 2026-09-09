package com.example.venta_entrada.ventas.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TipoEntradaResponseDTO {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Integer stockTotal;
    private Integer stockDisponible;
    private LocalDateTime fechaInicioVenta;
    private LocalDateTime fechaFinVenta;
    private Boolean activo;
}
