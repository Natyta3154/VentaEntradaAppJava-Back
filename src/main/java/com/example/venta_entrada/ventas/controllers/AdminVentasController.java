package com.example.venta_entrada.ventas.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.venta_entrada.ventas.dtos.response.AdminCompraResponseDTO;
import com.example.venta_entrada.ventas.dtos.response.AdminEntradaResponseDTO;
import com.example.venta_entrada.ventas.services.AdminVentasService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/ventas")
@RequiredArgsConstructor
@Tag(name = "Admin Ventas", description = "Endpoints de administración para ventas y entradas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVentasController {

    private final AdminVentasService adminVentasService;

    @Operation(summary = "Obtener todas las compras (histórico)")
    @GetMapping
    public ResponseEntity<Page<AdminCompraResponseDTO>> obtenerTodasLasCompras(Pageable pageable) {
        return ResponseEntity.ok(adminVentasService.obtenerTodasLasCompras(pageable));
    }

    @Operation(summary = "Obtener todas las entradas generadas")
    @GetMapping("/entradas")
    public ResponseEntity<Page<AdminEntradaResponseDTO>> obtenerTodasLasEntradas(Pageable pageable) {
        return ResponseEntity.ok(adminVentasService.obtenerTodasLasEntradas(pageable));
    }

    @Operation(summary = "Procesar devolución de una compra mediante Mercado Pago")
    @org.springframework.web.bind.annotation.PostMapping("/{id}/devolucion")
    public ResponseEntity<?> procesarDevolucion(@org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            adminVentasService.procesarDevolucion(id);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Devolución procesada con éxito"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("mensaje", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar una compra por su ID")
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCompra(@org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            adminVentasService.eliminarCompra(id);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Compra eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("mensaje", e.getMessage()));
        }
    }
}
