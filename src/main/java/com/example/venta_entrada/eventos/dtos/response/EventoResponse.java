package com.example.venta_entrada.eventos.dtos.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EventoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaEvento;
    private String ubicacion;
    private int capacidadTotal;
    private String estado;
    private int edadMinima;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaActualizacion;

    private String imagenPortada;

    private java.util.List<TipoEntradaDTO> tipos_tickets;

    @Data
    public static class TipoEntradaDTO {
        private Long id;
        private String nombre;
        private java.math.BigDecimal precio;
        private Integer stock_disponible;
        private Boolean activo;
        // Using stock_disponible to match frontend's expectation
    }
}
