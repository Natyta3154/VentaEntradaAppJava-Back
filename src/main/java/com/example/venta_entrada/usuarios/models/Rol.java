package com.example.venta_entrada.usuarios.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa los roles o permisos de los usuarios dentro del sistema.
 * Por ejemplo: "ADMIN", "USER", etc.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    /**
     * Identificador único del rol. Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del rol.
     * Este campo es obligatorio y no puede repetirse entre roles.
     */
    @Column(nullable = false, unique = true, length = 55)
    private String nombre;
}
