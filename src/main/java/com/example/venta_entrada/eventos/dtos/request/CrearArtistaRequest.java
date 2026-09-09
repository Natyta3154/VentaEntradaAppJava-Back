package com.example.venta_entrada.eventos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrearArtistaRequest {

    @NotBlank(message = "El nombre del artista es requerido")
    private String nombre;

    @NotBlank(message = "El estilo musical es requerido")
    private String estiloMusical;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private String foto;

}
