package com.example.venta_entrada.ventas.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @Column(length = 50)
    private String proveedor; // "MERCADOPAGO"

    @Column(name = "referencia_pago", length = 255)
    private String referenciaPago;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "medio_pago", length = 50)
    private String medioPago;

    @Column(name = "fecha_pago")
    @CreationTimestamp
    private LocalDateTime fechaPago;
}
