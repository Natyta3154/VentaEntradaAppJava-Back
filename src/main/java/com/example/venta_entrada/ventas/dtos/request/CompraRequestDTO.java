package com.example.venta_entrada.ventas.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CompraRequestDTO {
    @NotEmpty(message = "Debe haber al menos un ítem en la compra")
    private List<@jakarta.validation.Valid ItemCompraDTO> items;
}
