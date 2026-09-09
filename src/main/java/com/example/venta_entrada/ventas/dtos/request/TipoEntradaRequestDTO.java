package com.example.venta_entrada.ventas.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TipoEntradaRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @NotNull(message = "La capacidad (stock) es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer stockTotal;

    @NotNull(message = "La fecha de inicio de venta es obligatoria")
    private LocalDateTime fechaInicioVenta;

    @NotNull(message = "La fecha de fin de venta es obligatoria")
    private LocalDateTime fechaFinVenta;
}
