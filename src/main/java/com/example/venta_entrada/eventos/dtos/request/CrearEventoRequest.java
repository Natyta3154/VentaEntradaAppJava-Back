package com.example.venta_entrada.eventos.dtos.request;


import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearEventoRequest {

    @NotBlank(message = "El nombre del evento es requerido")
    private String nombre;

    @NotBlank(message = "La descripcion del evento es requerida")
    private String descripcion;

    @NotBlank(message = "La lugar del evento es requerido")
    private String ubicacion;
    
    @NotNull(message = "La capacidad del evento es requerida")
    private Integer capacidadTotal;

    @NotNull(message = "La edad minima es requerida")
    private Integer edadMinima;

    @NotNull(message = "La fecha del evento es requerida")
    private LocalDateTime fechaEvento;

    @NotNull(message = "La fecha de fin del evento es requerida")
    private LocalDateTime fechaFin;
    
    @NotBlank(message = "El estado es requerido")
    private String estado;

    private String imagenPortada;

}
