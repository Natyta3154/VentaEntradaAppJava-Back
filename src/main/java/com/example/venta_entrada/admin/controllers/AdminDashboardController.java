package com.example.venta_entrada.admin.controllers;

import com.example.venta_entrada.admin.dtos.AlertaStockDTO;
import com.example.venta_entrada.admin.dtos.DashboardMetricsDTO;
import com.example.venta_entrada.admin.dtos.EventoOcupacionDTO;
import com.example.venta_entrada.admin.services.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el Panel de Administración (Dashboard).
 * 
 * Expone los endpoints necesarios para que los usuarios con rol de administrador
 * puedan visualizar estadísticas clave del negocio, niveles de ocupación de eventos
 * y alertas tempranas sobre stock crítico de entradas.
 * 
 * Anotaciones principales:
 * - {@link RestController}: Define la clase como controlador web donde cada método retorna directamente datos serializados (JSON).
 * - {@link RequestMapping}: Establece la ruta base para todos los endpoints de este controlador ("/api/admin/dashboard").
 * - {@link RequiredArgsConstructor}: Genera automáticamente mediante Lombok el constructor con los atributos finales (inyección de dependencias).
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    // Servicio que contiene la lógica de negocio para el cálculo y consulta de métricas
    private final AdminDashboardService dashboardService;

    /**
     * Obtiene las métricas generales y globales del panel de administración.
     * 
     * Endpoint: GET /api/admin/dashboard/metrics
     * Seguridad: Requiere permiso de rol 'ROLE_ADMIN'.
     * 
     * @return {@link ResponseEntity} que contiene un objeto {@link DashboardMetricsDTO}
     *         con los indicadores principales (ej. total de ventas, ingresos recaudados,
     *         eventos activos, usuarios registrados, etc.).
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DashboardMetricsDTO> getMetrics() {
        return ResponseEntity.ok(dashboardService.getMetrics());
    }

    /**
     * Obtiene el listado de eventos junto con sus porcentajes y datos de ocupación/capacidad.
     * 
     * Endpoint: GET /api/admin/dashboard/ocupacion
     * Seguridad: Requiere permiso de rol 'ROLE_ADMIN'.
     * 
     * @return {@link ResponseEntity} que contiene una lista de {@link EventoOcupacionDTO},
     *         detallando para cada evento la capacidad total, entradas vendidas y porcentaje de ocupación.
     */
    @GetMapping("/ocupacion")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<EventoOcupacionDTO>> getOcupacionEventos() {
        return ResponseEntity.ok(dashboardService.getOcupacionEventos());
    }

    /**
     * Obtiene las alertas de inventario o stock crítico de entradas.
     * 
     * Endpoint: GET /api/admin/dashboard/alertas-stock
     * Seguridad: Requiere permiso de rol 'ROLE_ADMIN'.
     * 
     * @return {@link ResponseEntity} que contiene una lista de {@link AlertaStockDTO},
     *         identificando aquellos eventos o tipos de entradas que están por agotarse
     *         o por debajo del umbral mínimo de disponibilidad.
     */
    @GetMapping("/alertas-stock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AlertaStockDTO>> getAlertasStock() {
        return ResponseEntity.ok(dashboardService.getAlertasStock());
    }
}

