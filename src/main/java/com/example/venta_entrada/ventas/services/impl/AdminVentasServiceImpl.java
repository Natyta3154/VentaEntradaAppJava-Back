package com.example.venta_entrada.ventas.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.venta_entrada.ventas.dtos.response.AdminCompraResponseDTO;
import com.example.venta_entrada.ventas.dtos.response.AdminEntradaResponseDTO;
import com.example.venta_entrada.ventas.repositories.CompraRepository;
import com.example.venta_entrada.ventas.repositories.EntradaRepository;
import com.example.venta_entrada.ventas.services.AdminVentasService;

import com.example.venta_entrada.ventas.models.Compra;
import com.example.venta_entrada.ventas.models.EstadoCompra;
import com.example.venta_entrada.ventas.models.EstadoEntrada;
import com.example.venta_entrada.ventas.models.EstadoPago;
import com.example.venta_entrada.ventas.services.MercadoPagoService;
import com.mercadopago.resources.payment.PaymentRefund;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminVentasServiceImpl implements AdminVentasService {

    private final CompraRepository compraRepository;
    private final EntradaRepository entradaRepository;
    private final MercadoPagoService mercadoPagoService;

    @Override
    public Page<AdminCompraResponseDTO> obtenerTodasLasCompras(Pageable pageable) {
        return compraRepository.findAll(pageable).map(compra -> 
            AdminCompraResponseDTO.builder()
                .id(compra.getId())
                .usuarioEmail(compra.getUsuario().getEmail())
                .usuarioNombre(compra.getUsuario().getNombre() + " " + compra.getUsuario().getApellido())
                .total(compra.getTotal())
                .estado(compra.getEstado().name())
                .fechaCompra(compra.getFechaCompra())
                .mercadoPagoId(compra.getPago() != null ? compra.getPago().getReferenciaPago() : null)
                .build()
        );
    }

    @Override
    public Page<AdminEntradaResponseDTO> obtenerTodasLasEntradas(Pageable pageable) {
        return entradaRepository.findAll(pageable).map(entrada -> 
            AdminEntradaResponseDTO.builder()
                .id(entrada.getId())
                .eventoNombre(entrada.getEvento().getNombre())
                .tipoEntradaNombre(entrada.getTipoEntrada().getNombre())
                .estado(entrada.getEstado().name())
                .usuarioEmail(entrada.getCompra().getUsuario().getEmail())
                .usuarioNombre(entrada.getCompra().getUsuario().getNombre() + " " + entrada.getCompra().getUsuario().getApellido())
                .compraId(entrada.getCompra().getId())
                .build()
        );
    }

    @Override
    @Transactional
    public void procesarDevolucion(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        
        if (compra.getEstado() != EstadoCompra.COMPLETADA) {
            throw new RuntimeException("Sólo se pueden devolver compras COMPLETADAS.");
        }
        
        if (compra.getPago() == null || compra.getPago().getReferenciaPago() == null) {
            throw new RuntimeException("No se encontró el ID de MercadoPago para esta compra.");
        }
        
        Long paymentId = Long.valueOf(compra.getPago().getReferenciaPago());
        
        // Llamada a Mercado Pago
        PaymentRefund refund = mercadoPagoService.procesarDevolucion(paymentId);
        
        if (refund != null && "approved".equals(refund.getStatus())) {
            compra.setEstado(EstadoCompra.REEMBOLSADA);
            compra.getPago().setEstado(EstadoPago.REEMBOLSADO);
            
            compra.getEntradas().forEach(entrada -> {
                entrada.setEstado(EstadoEntrada.CANCELADA);
            });
            
            compraRepository.save(compra);
        } else {
            throw new RuntimeException("La devolución fue rechazada por Mercado Pago.");
        }
    }

    @Override
    @Transactional
    public void eliminarCompra(Long id) {
        if (!compraRepository.existsById(id)) {
            throw new RuntimeException("Compra no encontrada");
        }
        compraRepository.deleteById(id);
    }
}
