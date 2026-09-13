package com.example.venta_entrada.admin.services;

import com.example.venta_entrada.admin.dtos.AlertaStockDTO;
import com.example.venta_entrada.admin.dtos.DashboardMetricsDTO;
import com.example.venta_entrada.admin.dtos.EventoOcupacionDTO;
import com.example.venta_entrada.eventos.models.Evento;
import com.example.venta_entrada.eventos.repositories.EventoRepository;
import com.example.venta_entrada.usuarios.repositories.UsuarioRepository;
import com.example.venta_entrada.ventas.models.Compra;
import com.example.venta_entrada.ventas.models.Entrada;
import com.example.venta_entrada.ventas.models.EstadoCompra;
import com.example.venta_entrada.ventas.models.EstadoEntrada;
import com.example.venta_entrada.ventas.models.TipoEntrada;
import com.example.venta_entrada.ventas.repositories.CompraRepository;
import com.example.venta_entrada.ventas.repositories.EntradaRepository;
import com.example.venta_entrada.ventas.repositories.TipoEntradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para el Panel de Administración (Dashboard).
 * 
 * Contiene la lógica necesaria para recopilar, calcular y consolidar
 * métricas financieras, estadísticas de asistencia, ocupación de aforos
 * y alertas de stock bajo en tiempo real para los administradores del sistema.
 * 
 * Anotaciones:
 * - {@link Service}: Marca la clase como un componente de servicio en el contenedor de Spring.
 * - {@link RequiredArgsConstructor}: Genera automáticamente el constructor para la inyección de dependencias de los repositorios.
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    // Repositorios para acceder a las tablas de la base de datos
    private final UsuarioRepository usuarioRepository;
    private final CompraRepository compraRepository;
    private final EntradaRepository entradaRepository;
    private final EventoRepository eventoRepository;
    private final TipoEntradaRepository tipoEntradaRepository;

    /**
     * Calcula y consolida todas las métricas clave globales del sistema.
     * 
     * Procesa información sobre:
     * 1. Total recaudado por ventas completadas.
     * 2. Recaudación obtenida en el día actual.
     * 3. Total de entradas vendidas (válidas o ya usadas).
     * 4. Total de entradas validadas (utilizadas en el acceso al evento).
     * 5. Cantidad total de usuarios registrados.
     * 6. Cantidad de eventos que se encuentran activos.
     * 7. Total de compras que fueron reembolsadas/devueltas.
     * 
     * @return {@link DashboardMetricsDTO} DTO con el resumen numérico de todas las métricas.
     */
    public DashboardMetricsDTO getMetrics() {
        // Obtenemos todas las compras y entradas registradas en la base de datos
        List<Compra> compras = compraRepository.findAll();
        List<Entrada> entradas = entradaRepository.findAll();

        // 1. Calcular el dinero total recaudado:
        // Filtra solo compras con estado 'COMPLETADA', extrae el total de cada una y suma los montos.
        Double totalRecaudado = compras.stream()
                .filter(c -> c.getEstado() == EstadoCompra.COMPLETADA)
                .map(c -> c != null && c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (acc, val) -> acc != null && val != null ? acc.add(val) : BigDecimal.ZERO)
                .doubleValue();

        // 2. Calcular las ventas del día actual:
        // Filtra compras completadas cuya fecha coincida exactamente con la fecha de hoy.
        LocalDate hoy = LocalDate.now();
        Double ventasDelDia = compras.stream()
                .filter(c -> c.getEstado() == EstadoCompra.COMPLETADA && c.getFechaCompra().toLocalDate().isEqual(hoy))
                .map(c -> c != null && c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (acc, val) -> acc != null && val != null ? acc.add(val) : BigDecimal.ZERO)
                .doubleValue();

        // 3. Contar entradas vendidas:
        // Se consideran vendidas aquellas que están en estado VALIDA (vigentes) o USADA (ya ingresaron al evento).
        long entradasVendidas = entradas.stream()
                .filter(e -> e.getEstado() == EstadoEntrada.VALIDA || e.getEstado() == EstadoEntrada.USADA)
                .count();

        // 4. Contar entradas ya validadas/usadas en la puerta de acceso:
        long entradasValidadas = entradas.stream()
                .filter(e -> e.getEstado() == EstadoEntrada.USADA)
                .count();

        // 5. Total de usuarios registrados en la plataforma:
        long usuariosActivos = usuarioRepository.count();

        // 6. Contar eventos que actualmente están marcados como activos:
        long eventosActivos = eventoRepository.findAll().stream()
                .filter(e -> e != null && Boolean.TRUE.equals(e.getActivo()))
                .count();

        // 7. Contar compras que fueron reembolsadas:
        long devoluciones = compras.stream()
                .filter(c -> c.getEstado() == EstadoCompra.REEMBOLSADA)
                .count();

        // Construir y retornar el DTO empaquetando todas las métricas calculadas
        return DashboardMetricsDTO.builder()
                .totalRecaudado(totalRecaudado)
                .ventasDelDia(ventasDelDia)
                .entradasVendidas(entradasVendidas)
                .entradasValidadas(entradasValidadas)
                .usuariosActivos(usuariosActivos)
                .eventosActivos(eventosActivos)
                .devoluciones(devoluciones)
                .build();
    }

    /**
     * Calcula el porcentaje y estado de ocupación de aforo para cada evento activo.
     * 
     * Considera únicamente eventos activos cuya fecha de realización sea futura o reciente
     * (hasta 1 día en el pasado) para no saturar el reporte con eventos históricos viejos.
     * 
     * @return Lista de {@link EventoOcupacionDTO} con detalles de capacidad, entradas vendidas
     *         y porcentaje de ocupación redondeado a 2 decimales.
     */
    public List<EventoOcupacionDTO> getOcupacionEventos() {
        // Filtrar eventos: que estén activos y cuya fecha sea posterior a ayer (eventos actuales/futuros)
        List<Evento> eventos = eventoRepository.findAll().stream()
                .filter(e -> e != null && Boolean.TRUE.equals(e.getActivo()))
                .filter(e -> e.getFechaEvento().isAfter(LocalDateTime.now().minusDays(1)))
                .collect(Collectors.toList());

        List<EventoOcupacionDTO> ocupacion = new ArrayList<>();
        
        // Iterar sobre cada evento para calcular sus estadísticas de aforo
        for (Evento evento : eventos) {
            int capacidadTotal = evento.getCapacidadTotal();
            
            // Si la capacidad es 0, evitamos división por cero y pasamos al siguiente evento
            if (capacidadTotal == 0) continue;

            // Contar cuántas entradas vendidas (VALIDA o USADA) pertenecen específicamente a este evento
            long entradasVendidas = entradaRepository.findAll().stream()
                    .filter(e -> e.getEvento().getId().equals(evento.getId()))
                    .filter(e -> e.getEstado() == EstadoEntrada.VALIDA || e.getEstado() == EstadoEntrada.USADA)
                    .count();

            // Calcular el porcentaje de ocupación: (entradasVendidas / capacidadTotal) * 100
            double porcentaje = ((double) entradasVendidas / capacidadTotal) * 100.0;
            
            // Construir el DTO del evento con el porcentaje redondeado a 2 decimales
            ocupacion.add(EventoOcupacionDTO.builder()
                    .eventoId(evento.getId())
                    .titulo(evento.getNombre())
                    .capacidadTotal(capacidadTotal)
                    .entradasVendidas(entradasVendidas)
                    .porcentajeOcupacion(Math.round(porcentaje * 100.0) / 100.0)
                    .build());
        }

        return ocupacion;
    }

    /**
     * Detecta y genera alertas sobre tickets/entradas con inventario crítico.
     * 
     * Criterios para generar una alerta:
     * 1. El stock disponible es menor o igual a 20 unidades.
     * 2. El evento correspondiente se encuentra activo.
     * 3. La fecha del evento es vigente (futura o reciente hasta 1 día atrás).
     * 
     * @return Lista de {@link AlertaStockDTO} con la información del tipo de ticket y stock restante.
     */
    public List<AlertaStockDTO> getAlertasStock() {
        List<TipoEntrada> tipos = tipoEntradaRepository.findAll();
        
        return tipos.stream()
                // Condición 1: Stock crítico (<= 20 unidades disponibles)
                .filter(t -> t.getStockDisponible() != null && t.getStockDisponible() <= 20)
                // Condición 2: Evento activo
                .filter(t -> t.getEvento().getActivo())
                // Condición 3: Evento vigente/futuro
                .filter(t -> t.getEvento().getFechaEvento().isAfter(LocalDateTime.now().minusDays(1)))
                // Mapear cada resultado a su correspondiente DTO
                .map(t -> AlertaStockDTO.builder()
                        .eventoId(t.getEvento().getId())
                        .eventoTitulo(t.getEvento().getNombre())
                        .tipoTicketId(t.getId())
                        .tipoTicketNombre(t.getNombre())
                        .stockDisponible(t.getStockDisponible())
                        .build())
                .collect(Collectors.toList());
    }
}

