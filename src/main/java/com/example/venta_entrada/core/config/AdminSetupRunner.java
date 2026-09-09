package com.example.venta_entrada.core.config;

import com.example.venta_entrada.usuarios.models.Rol;
import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.usuarios.repositories.RolRepository;
import com.example.venta_entrada.usuarios.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSetupRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("No se proporcionaron credenciales de ADMIN iniciales en las variables de entorno. Omitiendo creación de Admin.");
            return;
        }

        Optional<Usuario> existingAdmin = usuarioRepository.findByEmail(adminEmail);
        if (existingAdmin.isPresent()) {
            log.info("El usuario admin {} ya existe en la base de datos.", adminEmail);
            return;
        }

        Optional<Rol> adminRole = rolRepository.findByNombre("ROLE_ADMIN");
        if (adminRole.isEmpty()) {
            log.warn("El rol ROLE_ADMIN no existe en la base de datos. Asegúrese de que las migraciones iniciales se hayan ejecutado.");
            return;
        }

        Usuario newAdmin = Usuario.builder()
                .nombre("Admin")
                .apellido("Sistema")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .rol(adminRole.get())
                .build();

        usuarioRepository.save(newAdmin);
        log.info("Usuario administrador {} creado exitosamente desde variables de entorno.", adminEmail);
    }
}
