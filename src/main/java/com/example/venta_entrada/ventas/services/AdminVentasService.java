package com.example.venta_entrada.ventas.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.venta_entrada.ventas.dtos.response.AdminCompraResponseDTO;
import com.example.venta_entrada.ventas.dtos.response.AdminEntradaResponseDTO;

public interface AdminVentasService {
    Page<AdminCompraResponseDTO> obtenerTodasLasCompras(Pageable pageable);
    Page<AdminEntradaResponseDTO> obtenerTodasLasEntradas(Pageable pageable);
    void procesarDevolucion(Long compraId);
    void eliminarCompra(Long id);
}
