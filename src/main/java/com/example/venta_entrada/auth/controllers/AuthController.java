package com.example.venta_entrada.auth.controllers;

import com.example.venta_entrada.auth.dtos.request.LoginRequest;
import com.example.venta_entrada.auth.dtos.response.LoginResponse;
import com.example.venta_entrada.auth.models.RefreshToken;
import com.example.venta_entrada.core.security.JwtService;
import com.example.venta_entrada.auth.services.RefreshTokenService;
import com.example.venta_entrada.core.utils.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para Login, Logout y Refresh Token")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final com.example.venta_entrada.usuarios.repositories.UsuarioRepository usuarioRepository;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Operation(summary = "Iniciar sesión en el sistema")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        Cookie accessCookie = cookieUtil.createAccessTokenCookie(accessToken, jwtExpiration);
        Cookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken.getToken(), refreshExpiration);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        String rol = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_CLIENTE");
        
        boolean isAdmin = "ROLE_ADMIN".equals(rol);
        String mensaje = isAdmin ? "¡Bienvenido/a Admin!" : "¡Bienvenido/a!";

        com.example.venta_entrada.usuarios.models.Usuario dbUser = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after auth"));

        return ResponseEntity.ok(LoginResponse.builder()
                .mensaje(mensaje)
                .rol(rol)
                .id(dbUser.getId())
                .email(dbUser.getEmail())
                .username(dbUser.getNombre() + " " + dbUser.getApellido())
                .build());
    }

    @Operation(summary = "Refrescar el Access Token usando la cookie de Refresh Token")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenString = cookieUtil.getCookieValue(request, "refresh_token");

        if (refreshTokenString == null || refreshTokenString.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        return refreshTokenService.findByToken(refreshTokenString)
            .map(refreshTokenService::verifyExpiration)
            .map(rt -> rt != null ? rt.getUsuario() : null)
            .map(usuario -> {
                UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
                String accessToken = jwtService.generateToken(userDetails);
                Cookie accessCookie = cookieUtil.createAccessTokenCookie(accessToken, jwtExpiration);
                response.addCookie(accessCookie);
                
                String rolName = usuario.getRol().getNombre();
                
                return ResponseEntity.ok(LoginResponse.builder()
                    .mensaje("Token refrescado exitosamente")
                    .rol(rolName)
                    .id(usuario.getId())
                    .email(usuario.getEmail())
                    .username(usuario.getNombre() + " " + usuario.getApellido())
                    .build());
            })
            .orElseThrow(() -> new RuntimeException("Refresh token no está en la base de datos"));
    }

    @Operation(summary = "Cerrar sesión (Logout)")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenString = cookieUtil.getCookieValue(request, "refresh_token");
        
        if (refreshTokenString != null && !refreshTokenString.isEmpty()) {
            try {
                refreshTokenService.deleteByToken(refreshTokenString);
            } catch (Exception e) {
                // Ignore if not found
            }
        }
        
        response.addCookie(cookieUtil.clearCookie("access_token", "/"));
        response.addCookie(cookieUtil.clearCookie("refresh_token", "/api/auth"));

        return ResponseEntity.ok("Logout exitoso. Sesión cerrada y cookies eliminadas.");
    }
}
