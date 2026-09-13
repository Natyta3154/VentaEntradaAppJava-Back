package com.example.venta_entrada.ventas.services;

import com.example.venta_entrada.eventos.repositories.EventoRepository;
import com.example.venta_entrada.eventos.models.Evento;
import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.ventas.dtos.request.CompraRequestDTO;
import com.example.venta_entrada.ventas.dtos.request.ItemCompraDTO;
import com.example.venta_entrada.ventas.dtos.response.CompraResponseDTO;
import com.example.venta_entrada.ventas.models.*;
import com.example.venta_entrada.ventas.repositories.CompraRepository;
import com.example.venta_entrada.ventas.repositories.EntradaRepository;
import com.example.venta_entrada.ventas.repositories.PagoRepository;
import com.example.venta_entrada.ventas.repositories.TipoEntradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final CompraRepository compraRepository;
    private final EntradaRepository entradaRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final EventoRepository eventoRepository;
    private final PagoRepository pagoRepository;
    private final MercadoPagoService mercadoPagoService;
    private final EmailService emailService;

    @Transactional
    public CompraResponseDTO procesarCompra(CompraRequestDTO request, Usuario usuario) {
        
        List<Entrada> entradasAPreparar = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (ItemCompraDTO item : request.getItems()) {
            TipoEntrada tipo = tipoEntradaRepository.findById(item.getTipoEntradaId())
                    .orElseThrow(() -> new RuntimeException("Tipo de entrada no encontrado"));
            
            // Bloqueo pesimista para concurrencia
            Evento evento = eventoRepository.findByIdWithLock(tipo.getEvento().getId())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            
            // Validar capacidad dinámica (capacidadTotal - entradasVendidas)
            int vendidas = entradaRepository.countByEventoIdAndEstadoNot(evento.getId(), EstadoEntrada.CANCELADA);
            int capacidadDisponible = evento.getCapacidadTotal() - vendidas;
            
            if (item.getCantidad() > capacidadDisponible) {
                throw new RuntimeException("No hay suficiente capacidad para el evento: " + evento.getNombre());
            }

            for (int i = 0; i < item.getCantidad(); i++) {
                Entrada entrada = Entrada.builder()
                        .evento(evento)
                        .tipoEntrada(tipo)
                        .precio(tipo.getPrecio())
                        .estado(EstadoEntrada.RESERVADA) 
                        .build();
                entradasAPreparar.add(entrada);
                total = total.add(tipo.getPrecio());
            }
        }

        Compra compra = Compra.builder()
                .usuario(usuario)
                .total(total)
                .estado(EstadoCompra.PENDIENTE)
                .entradas(new ArrayList<>())
                .build();
                
        for (Entrada e : entradasAPreparar) {
            e.setCompra(compra);
            compra.getEntradas().add(e);
        }
        
        compra = compraRepository.save(compra);
        
        String initPoint = mercadoPagoService.createPreference(compra);
        
        return CompraResponseDTO.builder()
                .id(compra.getId())
                .estado(compra.getEstado().name())
                .total(compra.getTotal())
                .checkoutUrl(initPoint)
                .build();
    }

    @Transactional
    public void procesarNotificacionPago(Long paymentId) {
        com.mercadopago.resources.payment.Payment payment = mercadoPagoService.verificarPago(paymentId);
        
        if (payment != null && payment.getExternalReference() != null) {
            Long compraId = Long.valueOf(payment.getExternalReference());
            
            Compra compra = compraRepository.findById(compraId).orElse(null);
            
            if (compra != null && compra.getEstado() == EstadoCompra.PENDIENTE) {
                if ("approved".equals(payment.getStatus())) {
                    compra.setEstado(EstadoCompra.COMPLETADA);
                    
                    Pago pago = Pago.builder()
                            .compra(compra)
                            .proveedor("MERCADOPAGO")
                            .medioPago(payment.getPaymentMethodId())
                            .referenciaPago(payment.getId().toString())
                            .monto(new BigDecimal(payment.getTransactionAmount().toString()))
                            .estado(EstadoPago.APROBADO)
                            .build();
                    pagoRepository.save(pago);
                    
                    compra.getEntradas().forEach(e -> e.setEstado(EstadoEntrada.VALIDA));
                    compraRepository.save(compra);
                    
                    // Inicializar propiedades lazy para evitar error en el hilo asíncrono
                    org.hibernate.Hibernate.initialize(compra.getUsuario());
                    if (compra.getEntradas() != null) {
                        for (Entrada e : compra.getEntradas()) {
                            org.hibernate.Hibernate.initialize(e.getEvento());
                            org.hibernate.Hibernate.initialize(e.getTipoEntrada());
                        }
                    }
                    
                    // Enviar email con las entradas
                    emailService.enviarEntradasPorEmail(compra.getUsuario(), compra.getEntradas());
                } else if ("rejected".equals(payment.getStatus()) || "cancelled".equals(payment.getStatus())) {
                    compra.setEstado(EstadoCompra.CANCELADA);
                    compra.getEntradas().forEach(e -> e.setEstado(EstadoEntrada.CANCELADA));
                    compraRepository.save(compra);
                }
            }
        }
    }
}
