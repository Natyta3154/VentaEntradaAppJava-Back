package com.example.venta_entrada.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio encargado de toda la lógica criptográfica de los JSON Web Tokens (JWT).
 * Utiliza la librería jjwt para firmar tokens nuevos y verificar la firma de tokens entrantes.
 */
@Service
public class JwtService {

    /**
     * Clave secreta utilizada para firmar y verificar los tokens JWT.
     * Se inyecta desde las propiedades de configuración (jwt.secret).
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Tiempo de expiración del token en milisegundos.
     * Se inyecta desde las propiedades de configuración (jwt.expiration).
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extrae el nombre de usuario (subject) de un token JWT.
     *
     * @param token el token JWT del cual extraer el nombre de usuario
     * @return el nombre de usuario almacenado en el token
     */
    public String extractUsername(String token) {
        return extractClaim(token, c -> c != null ? c.getSubject() : null);
    }

    /**
     * Extrae un claim específico de un token JWT utilizando una función de resolución.
     *
     * @param token          el token JWT
     * @param claimsResolver función que determina qué claim extraer
     * @param <T>            tipo de dato del claim a extraer
     * @return el claim extraído
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para un usuario específico sin claims adicionales.
     *
     * @param userDetails los detalles del usuario para el cual generar el token
     * @return el token JWT generado
     */
  public String generateToken(UserDetails userDetails) {

    Map<String, Object> claims = new HashMap<>();

    claims.put(
            "roles",
            userDetails.getAuthorities()
                    .stream()
                    .map(a -> a != null ? a.getAuthority() : null)
                    .toList()
    );

    return generateToken(claims, userDetails);
}

    /**
     * Genera un token JWT para un usuario específico con claims adicionales.
     *
     * @param extraClaims claims adicionales a incluir en el token
     * @param userDetails los detalles del usuario para el cual generar el token
     * @return el token JWT generado
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Construye un token JWT con la información proporcionada.
     *
     * @param extraClaims claims adicionales
     * @param userDetails detalles del usuario (su nombre de usuario será el subject)
     * @param expiration  tiempo de expiración en milisegundos
     * @return el token JWT generado en formato compactado
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
                
    }

    /**
     * Verifica si un token es válido evaluando si pertenece al usuario y si no ha expirado.
     *
     * @param token       el token JWT a validar
     * @param userDetails los detalles del usuario contra los cuales validar el token
     * @return true si el token es válido, false en caso contrario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica si un token JWT ha expirado.
     *
     * @param token el token JWT
     * @return true si el token ha expirado, false si sigue vigente
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración de un token JWT.
     *
     * @param token el token JWT
     * @return la fecha de expiración almacenada en el token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, c -> c != null ? c.getExpiration() : null);
    }

    /**
     * Extrae todos los claims (cuerpo) de un token JWT.
     * Valida la firma del token durante este proceso.
     *
     * @param token el token JWT
     * @return los claims almacenados en el token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene la clave de firma secreta decodificando la clave en formato Base64.
     *
     * @return la clave de firma secreta del algoritmo HMAC-SHA
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
