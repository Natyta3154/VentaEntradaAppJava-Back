package com.example.venta_entrada.core.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Clase utilitaria para crear, eliminar y leer cookies seguras.
 * Fundamental para implementar una autenticación robusta mediante HttpOnly cookies.
 */
@Component
public class CookieUtil {

    /**
     * Crea una cookie segura para el token de acceso (Access Token).
     * @param token El JWT generado
     * @param durationMs Cuánto tiempo durará en milisegundos
     */
    public Cookie createAccessTokenCookie(String token, long durationMs) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setMaxAge((int) (durationMs / 1000));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(isProdEnvironment()); // Solo true en producción/HTTPS
        cookie.setAttribute("SameSite", "Lax"); // Mitiga CSRF, Lax permite navegación normal
        return cookie;
    }

    /**
     * Crea una cookie segura para el token de refresco (Refresh Token).
     * @param token El token generado (suele ser un UUID)
     * @param durationMs Cuánto tiempo durará en milisegundos (suele ser varios días)
     */
    public Cookie createRefreshTokenCookie(String token, long durationMs) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setMaxAge((int) (durationMs / 1000));
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth"); 
        cookie.setSecure(isProdEnvironment());
        cookie.setAttribute("SameSite", "Strict"); // Refresh token debe ser muy estricto
        return cookie;
    }

    /**
     * Genera una cookie "vacía" y expirada para obligar al navegador a borrarla.
     * Utilizado durante el Logout.
     */
    public Cookie clearCookie(String name, String path) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0); // Tiempo 0 indica al navegador que debe destruirla de inmediato
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        cookie.setSecure(isProdEnvironment());
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    private boolean isProdEnvironment() {
        // En una app real, podrías inyectar el environment. 
        // Para simplificar, asumimos HTTPS si se define en ENV (o true por defecto para no romper local si están en puertos distintos)
        // Spring Environment se podría inyectar aquí. Retornaremos false localmente, true si env var indica.
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        return "prod".equals(env);
    }

    /**
     * Extrae el valor de una cookie específica que viene en la petición HTTP.
     */
    public String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null; // Retorna null si la cookie no existe
    }
}
