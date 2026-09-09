package com.example.venta_entrada.usuarios.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) para manejar la solicitud de registro de un nuevo usuario.
 * <p>
 * Este record encapsula los datos necesarios que un cliente debe enviar para crear
 * una cuenta en el sistema, aplicando validaciones básicas de formato y obligatoriedad.
 * </p>
 *
 * @param nombre   El nombre del usuario. No puede estar en blanco.
 * @param apellido El apellido del usuario. No puede estar en blanco.
 * @param email    El correo electrónico del usuario. Debe tener un formato válido y no puede estar en blanco.
 * @param password La contraseña elegida por el usuario. Debe tener al menos 6 caracteres y no puede estar en blanco.
 */
public record RegistroUsuarioRequest(
    @NotBlank
    String nombre,

    @NotBlank
    String apellido,
    
    @Email
    @NotBlank
    String email,

    @NotBlank
    @Size(
        min = 6,
        message = "La contraseña debe tener al menos 6 caracteres"
    )
    String password

) {}