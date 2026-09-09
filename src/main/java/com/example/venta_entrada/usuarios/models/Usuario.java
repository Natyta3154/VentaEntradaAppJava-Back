package com.example.venta_entrada.usuarios.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import lombok.*;

/**
 * Entidad que representa a un usuario registrado en el sistema de venta de entradas.
 * Almacena la información personal, credenciales y su rol asociado.
 */
@SQLDelete(sql = "UPDATE usuarios SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    /**
     * Identificador único del usuario. Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del usuario.
     */
    @Column(nullable = false, length = 55)
    private String nombre;

    /**
     * Apellido del usuario.
     */
    @Column(nullable = false, length = 55)
    private String apellido;

    /**
     * Correo electrónico del usuario. 
     * Se utiliza como nombre de usuario para el inicio de sesión y debe ser único.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Contraseña del usuario. Se recomienda almacenar el hash de la misma por seguridad.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Rol asociado al usuario. 
     * Define los permisos y acciones que el usuario puede realizar en el sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    /**
     * Fecha en la que el usuario se registró en el sistema.
     */
    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Fecha en la que la información del usuario fue actualizada por última vez.
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;


    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;
}
