package com.example.venta_entrada.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoOcupacionDTO {
    private Long eventoId;
    private String titulo;
    private Integer capacidadTotal;
    private Long entradasVendidas;
    private Double porcentajeOcupacion;
}
