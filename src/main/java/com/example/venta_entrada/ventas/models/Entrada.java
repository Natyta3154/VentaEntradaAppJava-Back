package com.example.venta_entrada.ventas.models;

import com.example.venta_entrada.eventos.models.Evento;
import com.example.venta_entrada.usuarios.models.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "entradas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_entrada_id", nullable = false)
    private TipoEntrada tipoEntrada;

    @Column(nullable = false, unique = true, length = 100)
    private String codigo;

    @Column(name = "codigo_qr", nullable = false, unique = true, length = 255)
    private String codigoQr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEntrada estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "fecha_ingreso")
    private LocalDateTime fechaIngreso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validado_por_id")
    private Usuario validadoPor;

    @Column(name = "fecha_creacion", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;
    
    @PrePersist
    public void generateUUIDs() {
        if (codigo == null) {
            codigo = UUID.randomUUID().toString();
        }
        if (codigoQr == null) {
            codigoQr = UUID.randomUUID().toString();
        }
    }
}
