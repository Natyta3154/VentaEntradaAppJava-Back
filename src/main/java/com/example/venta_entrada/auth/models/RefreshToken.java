package com.example.venta_entrada.auth.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.example.venta_entrada.usuarios.models.Usuario;

/**
 * Entidad que representa un Refresh Token en la base de datos.
 * El Refresh Token se emite al iniciar sesión y sirve para obtener nuevos
 * Access Tokens (de corta duración) sin obligar al usuario a volver a loguearse.
 * Si el usuario cierra sesión (logout), este token debe borrarse de la BD.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

}
