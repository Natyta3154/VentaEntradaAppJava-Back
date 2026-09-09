package com.example.venta_entrada.ventas.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminEntradaResponseDTO {
    private Long id;
    private String eventoNombre;
    private String tipoEntradaNombre;
    private String estado;
    private String usuarioEmail;
    private String usuarioNombre;
    private Long compraId;
}
