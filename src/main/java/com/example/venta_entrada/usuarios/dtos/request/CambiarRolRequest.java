package com.example.venta_entrada.usuarios.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarRolRequest {
    @NotBlank(message = "El nuevo rol es requerido")
    private String nuevoRol;
}
