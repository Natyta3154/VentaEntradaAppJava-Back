package com.example.venta_entrada.eventos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventoImagenRequest {

    @NotBlank(message = "El nombre de la imagen es requerido")
    private String nombreImagen;

    @NotBlank(message = "La url de la imagen es requerida")
    private String urlImagen;

    @NotNull(message = "El orden de la imagen es requerido")
    private int orden;

    @NotNull(message = "El id del evento es requerido")
    private Long eventoId;

}
