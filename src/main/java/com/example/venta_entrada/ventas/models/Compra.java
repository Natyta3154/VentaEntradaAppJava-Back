package com.example.venta_entrada.ventas.models;

import com.example.venta_entrada.usuarios.models.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_compra", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCompra;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado;

    @OneToOne(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Pago pago;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Entrada> entradas;
}
