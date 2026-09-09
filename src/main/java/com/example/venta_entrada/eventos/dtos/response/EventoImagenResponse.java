package com.example.venta_entrada.eventos.dtos.response;

import lombok.Data;

@Data
public class EventoImagenResponse {
    private Long id;
    private String nombreImagen;
    private String urlImagen;
    private int orden;
}
