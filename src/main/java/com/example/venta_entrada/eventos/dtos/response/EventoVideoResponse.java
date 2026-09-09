package com.example.venta_entrada.eventos.dtos.response;

import lombok.Data;

@Data
public class EventoVideoResponse {
    private Long id;
    private String nombreVideo;
    private String urlVideo;
    private int orden;
}
