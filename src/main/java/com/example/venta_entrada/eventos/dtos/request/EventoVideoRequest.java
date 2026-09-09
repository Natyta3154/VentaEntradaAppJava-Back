package com.example.venta_entrada.eventos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventoVideoRequest {

    @NotBlank(message = "El nombre del video es requerido")
    private String nombreVideo;

    @NotBlank(message = "La url del video es requerida")
    private String urlVideo;

    @NotNull(message = "El orden del video es requerido")
    private int orden;

    @NotNull(message = "El id del evento es requerido")
    private Long eventoId;

}
