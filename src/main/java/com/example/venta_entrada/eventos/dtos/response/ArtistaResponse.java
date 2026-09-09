package com.example.venta_entrada.eventos.dtos.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ArtistaResponse {
    private Long id;
    private String nombre;
    private String estiloMusical;
    private String descripcion;
    private String foto;
    private LocalDateTime fechaCreacion;
}
