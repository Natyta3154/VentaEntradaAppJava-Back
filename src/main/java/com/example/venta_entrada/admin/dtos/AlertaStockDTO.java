package com.example.venta_entrada.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStockDTO {
    private Long eventoId;
    private String eventoTitulo;
    private Long tipoTicketId;
    private String tipoTicketNombre;
    private Integer stockDisponible;
}
