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

/**
 * Controlador de Autenticación y Gestión de Sesiones (AuthController).
 * 
 * Este controlador expone los endpoints necesarios para que los usuarios puedan:
 * 1. Iniciar sesión (/api/auth/login) con email y contraseña.
 * 2. Renovar su token de acceso (/api/auth/refresh) cuando este expire, usando una cookie segura.
 * 3. Cerrar sesión (/api/auth/logout) eliminando las credenciales y cookies de forma segura.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para Login, Logout y Refresh Token")
public class AuthController {

    // Componentes inyectados para gestionar autenticación, tokens y cookies
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final com.example.venta_entrada.usuarios.repositories.UsuarioRepository usuarioRepository;

    // Tiempo de expiración del Access Token (ej: 15 minutos en milisegundos)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Tiempo de expiración del Refresh Token (ej: 7 días en milisegundos)
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Endpoint para INICIAR SESIÓN (Login).
     * 
     * ¿Para qué sirve?
     * Valida el correo y la contraseña que envía el usuario desde el formulario de inicio de sesión.
     * Si las credenciales son correctas:
     * 1. Autentica al usuario mediante Spring Security (AuthenticationManager).
     * 2. Genera un 'Access Token' (JWT) con roles y datos de corta duración (15 min).
     * 3. Genera un 'Refresh Token' único (UUID) y lo guarda en la base de datos (dura 7 días).
     * 4. Guarda ambos tokens dentro de Cookies HTTP-Only en la respuesta del navegador (para máxima seguridad contra ataques XSS).
     * 5. Retorna en el cuerpo JSON los datos del usuario (id, email, nombre completo y rol).
     * 
     * @param request  DTO que contiene el email y la contraseña enviada por el usuario.
     * @param response Objeto HTTP donde se adjuntan las cookies de sesión (access_token y refresh_token).
     * @return {@link ResponseEntity<LoginResponse>} Datos del usuario logueado.
     */
    @Operation(summary = "Iniciar sesión en el sistema")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. Validar email y contraseña en la base de datos
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Extraer los datos del usuario autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generar el Access Token (JWT firmado) y el Refresh Token (UUID en BD)
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        // 4. Crear cookies seguras (HttpOnly, SameSite) para ambos tokens
        Cookie accessCookie = cookieUtil.createAccessTokenCookie(accessToken, jwtExpiration);
        Cookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken.getToken(), refreshExpiration);

        // 5. Agregar las cookies a la respuesta HTTP
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // 6. Obtener el rol del usuario para personalizar la respuesta
        String rol = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_CLIENTE");
        
        boolean isAdmin = "ROLE_ADMIN".equals(rol);
        String mensaje = isAdmin ? "¡Bienvenido/a Admin!" : "¡Bienvenido/a!";

        // 7. Buscar datos completos del usuario en la base de datos
        com.example.venta_entrada.usuarios.models.Usuario dbUser = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras autenticación exitosa"));

        // 8. Responder con el DTO que contiene nombre, email, id y rol
        return ResponseEntity.ok(LoginResponse.builder()
                .mensaje(mensaje)
                .rol(rol)
                .id(dbUser.getId())
                .email(dbUser.getEmail())
                .username(dbUser.getNombre() + " " + dbUser.getApellido())
                .build());
    }

    /**
     * Endpoint para REFRESCAR EL TOKEN DE ACCESO (Refresh Token).
     * 
     * ¿Para qué sirve?
     * Cuando el 'Access Token' de 15 minutos caduca, el frontend llama automáticamente a este endpoint
     * para obtener un nuevo Access Token SIN pedirle la contraseña al usuario nuevamente.
     * 
     * ¿Cómo funciona?
     * 1. Lee la cookie segura 'refresh_token' que viene en la petición HTTP.
     * 2. Si la cookie no existe, retorna 401 Unauthorized.
     * 3. Busca el token en la base de datos y verifica que NO haya superado su fecha de expiración.
     * 4. Genera un nuevo 'Access Token' (JWT) válido por otros 15 minutos y lo devuelve en una nueva cookie.
     * 5. Retorna los datos del usuario actualizados.
     * 
     * @param request  Petición HTTP entrante de donde se extrae la cookie de refresh token.
     * @param response Respuesta HTTP donde se coloca la nueva cookie 'access_token'.
     * @return {@link ResponseEntity<LoginResponse>} Datos del usuario con el token renovado.
     */
    @Operation(summary = "Refrescar el Access Token usando la cookie de Refresh Token")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. Extraer el valor de la cookie 'refresh_token'
        String refreshTokenString = cookieUtil.getCookieValue(request, "refresh_token");

        // 2. Si no hay cookie, rechazar con código 401
        if (refreshTokenString == null || refreshTokenString.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        // 3. Buscar en la base de datos, validar expiración y generar nuevo Access Token
        return refreshTokenService.findByToken(refreshTokenString)
            .map(refreshTokenService::verifyExpiration) // Lanza excepción si expiró
            .map(rt -> rt != null ? rt.getUsuario() : null)
            .map(usuario -> {
                // Cargar detalles del usuario y crear nuevo JWT
                UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
                String accessToken = jwtService.generateToken(userDetails);
                
                // Sobrescribir la cookie con el nuevo token vigente
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
            .orElseThrow(() -> new RuntimeException("Refresh token no encontrado en la base de datos"));
    }

    /**
     * Endpoint para CERRAR SESIÓN (Logout).
     * 
     * ¿Para qué sirve?
     * Cierra la sesión activa del usuario de forma segura e invalida los tokens.
     * 
     * ¿Cómo funciona?
     * 1. Extrae el 'refresh_token' de la cookie del usuario.
     * 2. Borra el registro del 'RefreshToken' de la base de datos para que no pueda volver a usarse.
     * 3. Sobrescribe las cookies 'access_token' y 'refresh_token' con tiempo de vida cero (Max-Age=0),
     *    lo que obliga al navegador del usuario a eliminarlas inmediatamente.
     * 
     * @param request  Petición HTTP entrante con las cookies del usuario.
     * @param response Respuesta HTTP donde se agregan las instrucciones para eliminar las cookies.
     * @return Mensaje confirmando el cierre de sesión exitoso.
     */
    @Operation(summary = "Cerrar sesión (Logout)")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Obtener el token de la cookie
        String refreshTokenString = cookieUtil.getCookieValue(request, "refresh_token");
        
        // 2. Si existe, eliminarlo de la base de datos
        if (refreshTokenString != null && !refreshTokenString.isEmpty()) {
            try {
                refreshTokenService.deleteByToken(refreshTokenString);
            } catch (Exception e) {
                // Si ya fue borrado, se ignora la excepción
            }
        }
        
        // 3. Limpiar las cookies del navegador asignándoles tiempo de expiración cero
        response.addCookie(cookieUtil.clearCookie("access_token", "/"));
        response.addCookie(cookieUtil.clearCookie("refresh_token", "/api/auth"));

        return ResponseEntity.ok("Logout exitoso. Sesión cerrada y cookies eliminadas.");
    }
}

