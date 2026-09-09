package com.example.venta_entrada.ventas.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidacionQrRequestDTO {
    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}
