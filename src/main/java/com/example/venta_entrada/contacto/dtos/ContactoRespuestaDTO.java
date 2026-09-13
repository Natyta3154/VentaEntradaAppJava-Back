package com.example.venta_entrada.contacto.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactoRespuestaDTO {

    @NotBlank(message = "La respuesta es obligatoria")
    private String respuesta;
}
