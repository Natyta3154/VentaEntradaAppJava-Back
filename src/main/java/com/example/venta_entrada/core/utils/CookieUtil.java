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

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

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
        boolean secure = isProdEnvironment();
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", secure ? "None" : "Lax");
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
        boolean secure = isProdEnvironment();
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", secure ? "None" : "Lax");
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
        boolean secure = isProdEnvironment();
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", secure ? "None" : "Lax");
        return cookie;
    }

    private boolean isProdEnvironment() {
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        String forceSecure = System.getenv("COOKIE_SECURE");
        if ("true".equalsIgnoreCase(forceSecure)) return true;
        if ("prod".equalsIgnoreCase(env) || "production".equalsIgnoreCase(env)) return true;
        return frontendUrl != null && frontendUrl.contains("https://");
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
