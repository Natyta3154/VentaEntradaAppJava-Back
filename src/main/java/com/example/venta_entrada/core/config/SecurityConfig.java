package com.example.venta_entrada.core.config;

import com.example.venta_entrada.core.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.http.HttpMethod;
import java.util.Arrays;
import java.util.List;

/**
 * Configuración central de seguridad web con Spring Security 6 / Spring Boot 3+.
 * 
 * Aspectos configurados:
 * 1. Autenticación Stateless (sin sesiones en memoria) mediante tokens JWT guardados en cookies HTTP-only.
 * 2. Protección CSRF con token en cookie accesible desde el frontend (header X-XSRF-TOKEN).
 * 3. Configuración CORS dinámica para permitir peticiones desde el frontend con credenciales/cookies.
 * 4. Control de acceso granular por endpoints y por roles (ROLE_ADMIN, ROLE_CLIENTE, ROLE_PORTERO).
 * 5. Filtro personalizado de autenticación JWT insertado antes del filtro de usuario/contraseña estándar.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Define la cadena de filtros de seguridad HTTP y las reglas de autorización de rutas.
     * 
     * @param http Objeto para configurar la seguridad web.
     * @return {@link SecurityFilterChain} construida.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler requestHandler = new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler();
        // Permite que Spring resuelva el token tanto de headers como de parámetros sin enmascaramiento adicional
        requestHandler.setCsrfRequestAttributeName(null); 

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Habilitamos CSRF utilizando una cookie accesible desde JS para que el frontend pueda enviar el header X-XSRF-TOKEN
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/api/auth/**", "/api/usuarios/registro", "/api/ventas/webhook/**", "/api/contactos/**")
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos que no requieren login
                .requestMatchers(
                    "/",
                    "/api/health",
                    "/api/usuarios/registro", 
                    "/api/auth/**", 
                    "/api/ventas/webhook/**",
                    "/api/eventos/**",
                    "/api/artistas/**",
                    "/api/imagenes/**",
                    "/api/videos/**",
                    "/error"
                ).permitAll() 
                .requestMatchers(HttpMethod.POST, "/api/contactos").permitAll()
                // Endpoints restringidos exclusivamente para Administradores
                .requestMatchers("/api/ventas/test-mp-direct", "/swagger-ui/**", "/v3/api-docs/**").hasAuthority("ROLE_ADMIN")
                // Cualquier otra petición requiere token válido
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new CsrfCookieFilter(), org.springframework.security.web.authentication.www.BasicAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Configura los orígenes, métodos y encabezados permitidos para peticiones CORS.
     * 
     * @return Fuente de configuración CORS basada en URLs.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String urlConfig = (frontendUrl != null) ? frontendUrl : "http://localhost:5173";
        String[] rawOrigins = urlConfig.split(",");
        List<String> origins = new java.util.ArrayList<>();
        for (String origin : rawOrigins) {
            if (origin != null) {
                String clean = origin.trim().replaceAll("/+$", "");
                if (!clean.isEmpty()) {
                    origins.add(clean);
                }
            }
        }
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Collections.singletonList("*"));
        configuration.setAllowCredentials(true); // Permitir cookies (necesario para JWT en cookies)
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Proveedor de autenticación que consulta usuarios en base de datos y valida contraseñas con BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Administrador de autenticación global de Spring Security.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean del codificador seguro de contraseñas mediante algoritmo BCrypt (fuerza por defecto 10 rounds).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

