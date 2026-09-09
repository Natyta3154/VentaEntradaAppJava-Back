package com.example.venta_entrada.ventas.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCompraDTO {
    @NotNull(message = "El ID del tipo de entrada es obligatorio")
    private Long tipoEntradaId;
    
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @jakarta.validation.constraints.Max(value = 10, message = "No puedes comprar más de 10 entradas por tipo")
    private int cantidad;
}
