package com.fintech.b2b.infrastructure.security;

import com.fintech.b2b.domain.model.UserIdentity;
import com.fintech.b2b.domain.model.port.TokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;

@Component
public class JwtTokenAdapter implements TokenProviderPort {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 1 día por defecto
    private long jwtExpiration;

    /**
     * Corrección Criptográfica: Decodificamos estrictamente como Hexadecimal.
     * Garantiza que la matemática de la firma use los bytes reales, no los caracteres ASCII.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = HexFormat.of().parseHex(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generarToken(UserIdentity identity) {
        return Jwts.builder()
                .issuer("fintech-b2b") // Quién lo emite
                .audience().add("fintech-api").and() // Para quién es válido
                .subject(identity.email())
                .claim("id", identity.id()) // Inyectamos el ID para evitar consultas a BD
                .claim("rol", identity.rol())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey()) // API fluida 0.12.x
                .compact();
    }

    @Override
    public UserIdentity validar(String token) {
        // Un único parseo: valida firma, expiración, issuer, audience y clock skew.
        // Las excepciones (ExpiredJwtException, etc.) subirán naturalmente al filtro.
        Claims payload = Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer("fintech-b2b")
                .requireAudience("fintech-api") // Cerramos la brecha de seguridad
                .clockSkewSeconds(60) // Tolerancia de sincronización
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Si llega a esta línea, el token es 100% genuino y vigente
        Long id = payload.get("id", Long.class);
        String email = payload.getSubject();
        String rol = payload.get("rol", String.class);

        return new UserIdentity(id, email, rol);
    }
}