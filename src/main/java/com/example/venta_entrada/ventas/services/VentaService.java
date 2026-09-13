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

/**
 * Servicio principal de gestión de ventas y reservas de entradas.
 * 
 * Responsabilidades clave:
 * 1. Procesar la intención de compra y calcular totales.
 * 2. Bloqueo pesimista de eventos para prevenir sobreventa por concurrencia simultánea.
 * 3. Crear las entradas en estado RESERVADA y la orden en estado PENDIENTE.
 * 4. Integrar con Mercado Pago para generar la URL de pago (Preference).
 * 5. Procesar notificaciones IPN / Webhooks para confirmar pagos aprobados, emitir entradas y enviar emails con QR.
 */
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

    /**
     * Procesa la solicitud inicial de compra de entradas por parte de un usuario.
     * 
     * Flujo de ejecución:
     * 1. Recorre cada ítem solicitado (tipo de entrada y cantidad).
     * 2. Aplica bloqueo pesimista en el Evento (SELECT ... FOR UPDATE) para evitar condiciones de carrera.
     * 3. Calcula la capacidad disponible (capacidad total - entradas no canceladas).
     * 4. Si hay cupo, genera las entidades Entrada con estado 'RESERVADA'.
     * 5. Persiste la orden de Compra con estado 'PENDIENTE'.
     * 6. Solicita a Mercado Pago la creación de la preferencia de pago.
     * 7. Devuelve el DTO con el ID de compra y la URL de checkout de Mercado Pago.
     * 
     * @param request DTO con la lista de tipos de entradas y cantidades solicitadas.
     * @param usuario Usuario autenticado que realiza la compra.
     * @return {@link CompraResponseDTO} con los detalles de la compra y la URL de pago de Mercado Pago.
     */
    @Transactional
    public CompraResponseDTO procesarCompra(CompraRequestDTO request, Usuario usuario) {
        
        List<Entrada> entradasAPreparar = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (ItemCompraDTO item : request.getItems()) {
            // Obtener el tipo de entrada (ej: VIP, General, Campo)
            TipoEntrada tipo = tipoEntradaRepository.findById(item.getTipoEntradaId())
                    .orElseThrow(() -> new RuntimeException("Tipo de entrada no encontrado"));
            
            // Bloqueo pesimista sobre el evento para asegurar consistencia ante compras concurrentes
            Evento evento = eventoRepository.findByIdWithLock(tipo.getEvento().getId())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            
            // Validar capacidad dinámica (capacidadTotal - entradasVendidas o reservadas)
            int vendidas = entradaRepository.countByEventoIdAndEstadoNot(evento.getId(), EstadoEntrada.CANCELADA);
            int capacidadDisponible = evento.getCapacidadTotal() - vendidas;
            
            if (item.getCantidad() > capacidadDisponible) {
                throw new RuntimeException("No hay suficiente capacidad para el evento: " + evento.getNombre());
            }

            // Preparar las entradas individuales
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

        // Crear la orden de compra pendiente
        Compra compra = Compra.builder()
                .usuario(usuario)
                .total(total)
                .estado(EstadoCompra.PENDIENTE)
                .entradas(new ArrayList<>())
                .build();
                
        // Asociar bidireccionalmente las entradas con la compra
        for (Entrada e : entradasAPreparar) {
            e.setCompra(compra);
            compra.getEntradas().add(e);
        }
        
        // Guardar la compra (cascada guarda las entradas)
        compra = compraRepository.save(compra);
        
        // Generar la preferencia de pago en la pasarela de Mercado Pago
        String initPoint = mercadoPagoService.createPreference(compra);
        
        return CompraResponseDTO.builder()
                .id(compra.getId())
                .estado(compra.getEstado().name())
                .total(compra.getTotal())
                .checkoutUrl(initPoint)
                .build();
    }

    /**
     * Procesa la notificación automática (Webhook/IPN) enviada por Mercado Pago tras un evento de pago.
     * 
     * Flujo de ejecución:
     * 1. Consulta el estado del pago en la API de Mercado Pago usando el paymentId.
     * 2. Obtiene la compra vinculada mediante 'external_reference'.
     * 3. Si el estado es 'approved':
     *    - Actualiza la compra a COMPLETADA.
     *    - Registra el registro de Pago con referencia y monto.
     *    - Activa las entradas a estado VALIDA (habilitadas para generar QR y entrar).
     *    - Dispara el envío asíncrono de correo con las entradas y sus códigos QR en formato PNG.
     * 4. Si el estado es 'rejected' o 'cancelled':
     *    - Marca la compra y las entradas como CANCELADA (liberando el cupo).
     * 
     * @param paymentId Identificador único del pago en Mercado Pago.
     */
    @Transactional
    public void procesarNotificacionPago(Long paymentId) {
        com.mercadopago.resources.payment.Payment payment = mercadoPagoService.verificarPago(paymentId);
        
        if (payment != null && payment.getExternalReference() != null) {
            Long compraId = Long.valueOf(payment.getExternalReference());
            
            Compra compra = compraRepository.findById(compraId).orElse(null);
            
            if (compra != null && compra.getEstado() == EstadoCompra.PENDIENTE) {
                if ("approved".equals(payment.getStatus())) {
                    // Pago aprobado exitosamente
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
                    
                    // Habilitar entradas a estado VALIDA
                    compra.getEntradas().forEach(e -> e.setEstado(EstadoEntrada.VALIDA));
                    compraRepository.save(compra);
                    
                    // Inicializar propiedades lazy para evitar LazyInitializationException en hilos @Async
                    org.hibernate.Hibernate.initialize(compra.getUsuario());
                    if (compra.getEntradas() != null) {
                        for (Entrada e : compra.getEntradas()) {
                            org.hibernate.Hibernate.initialize(e.getEvento());
                            org.hibernate.Hibernate.initialize(e.getTipoEntrada());
                        }
                    }
                    
                    // Enviar email con las entradas y códigos QR adjuntos de forma asíncrona
                    emailService.enviarEntradasPorEmail(compra.getUsuario(), compra.getEntradas());
                } else if ("rejected".equals(payment.getStatus()) || "cancelled".equals(payment.getStatus())) {
                    // Pago rechazado o cancelado: se liberan los cupos
                    compra.setEstado(EstadoCompra.CANCELADA);
                    compra.getEntradas().forEach(e -> e.setEstado(EstadoEntrada.CANCELADA));
                    compraRepository.save(compra);
                }
            }
        }
    }
}

