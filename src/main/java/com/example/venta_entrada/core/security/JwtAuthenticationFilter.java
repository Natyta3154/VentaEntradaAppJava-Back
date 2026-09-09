package com.example.venta_entrada.core.security;

import com.example.venta_entrada.core.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Este filtro intercepta todas las peticiones HTTP que llegan al servidor (una vez por petición).
 * Su objetivo es buscar el token JWT dentro de las cookies de la petición, validarlo y,
 * si es correcto, establecer el contexto de seguridad de Spring para autorizar al usuario.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer el token específicamente de la cookie llamada "access_token"
        // En una arquitectura tradicional esto se haría leyendo el header: request.getHeader("Authorization")
        String jwt = cookieUtil.getCookieValue(request, "access_token");
        
        // 2. Si no hay token, simplemente pasamos la petición al siguiente filtro
        // Puede que sea una ruta pública (como /login) que no necesita token.
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extraer el email (subject) cifrado dentro del token
            String userEmail = jwtService.extractUsername(jwt);

            // 4. Si el token tiene un email y el usuario aún no está autenticado en el contexto actual
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Cargamos los datos completos del usuario desde la BD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Verificamos matemáticamente que el token no haya sido alterado y que le pertenezca a este usuario
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // Creamos el objeto de autenticación que Spring Security necesita
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities() // Aquí van los roles (ROLE_ADMIN, etc.)
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Establecemos al usuario como "autenticado" para esta petición
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si el token es inválido, manipulado o expirado, saltará una excepción.
            // La capturamos silenciosamente; el usuario quedará sin autenticar (Anónimo)
            // y Spring Security le denegará el acceso si la ruta es privada devolviendo un 403 o 401.
        }

        // 5. Continuar con el resto de filtros y finalmente llegar al controlador
        filterChain.doFilter(request, response);
    }
}
