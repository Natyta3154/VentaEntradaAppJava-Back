package com.example.venta_entrada.ventas.models;

import com.example.venta_entrada.eventos.models.Evento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "tipos_entrada")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoEntrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock_total", nullable = false)
    private Integer stockTotal;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible;

    @Column(name = "fecha_inicio_venta")
    private LocalDateTime fechaInicioVenta;

    @Column(name = "fecha_fin_venta")
    private LocalDateTime fechaFinVenta;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
}
