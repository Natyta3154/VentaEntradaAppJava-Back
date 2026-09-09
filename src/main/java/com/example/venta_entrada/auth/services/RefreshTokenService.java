package com.example.venta_entrada.auth.services;

import com.example.venta_entrada.auth.models.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String email);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUser(String email);
    java.util.Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
}
