package com.example.venta_entrada.auth.repositories;

import com.example.venta_entrada.auth.models.RefreshToken;
import com.example.venta_entrada.usuarios.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUsuario(Usuario usuario);
}
