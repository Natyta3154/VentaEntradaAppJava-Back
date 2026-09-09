package com.example.venta_entrada.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {
    private Double totalRecaudado;
    private Double ventasDelDia;
    private Long entradasVendidas;
    private Long entradasValidadas;
    private Long usuariosActivos;
    private Long eventosActivos;
    private Long devoluciones;
}
