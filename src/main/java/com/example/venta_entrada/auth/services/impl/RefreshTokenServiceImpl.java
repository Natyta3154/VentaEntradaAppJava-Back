package com.example.venta_entrada.auth.services.impl;

import com.example.venta_entrada.auth.models.RefreshToken;
import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.auth.repositories.RefreshTokenRepository;
import com.example.venta_entrada.usuarios.repositories.UsuarioRepository;
import com.example.venta_entrada.auth.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio para los Refresh Tokens.
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UsuarioRepository usuarioRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea un nuevo Refresh Token y lo guarda en la base de datos.
     * Si el usuario ya tenía uno anterior, se borra (para mantener una única sesión activa).
     */
    @Override
    @Transactional
    public RefreshToken createRefreshToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Eliminar tokens previos para este usuario (para que solo tenga una sesión activa)
        refreshTokenRepository.deleteByUsuario(usuario);

        // Generar un token aleatorio seguro (UUID) y asignar su fecha de expiración
        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenDurationMs * 1_000_000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifica si el token recibido aún es válido (no ha pasado su fecha de expiración).
     * Si ya expiró, lo borra de la base de datos.
     */
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("El Refresh Token ha expirado. Por favor inicia sesión de nuevo.");
        }
        return token;
    }

    /**
     * Elimina el token de la BD cuando el usuario hace logout.
     */
    @Override
    @Transactional
    public void deleteByUser(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    @Override
    public java.util.Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
