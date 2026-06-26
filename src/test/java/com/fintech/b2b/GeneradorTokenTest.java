package com.fintech.b2b;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;

public class GeneradorTokenTest {

    @Test
    public void fabricarTokenVIP() {
        // 1. clave
        String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        
        // 2. Misma matemática que JwtTokenAdapter (Hexadecimal)
        byte[] keyBytes = HexFormat.of().parseHex(secretKey);
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        // 3. Generar token con Emisor y Audiencia requeridos
        long jwtExpiration = 86400000L; // 1 día
        
        String token = Jwts.builder()
                .issuer("fintech-b2b") // Sello obligatorio
                .audience().add("fintech-api").and() // Audiencia
                .subject("hacker@test.com")
                .claim("id", 998L)
                .claim("rol", "ROLE_EMPLEADO")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey) // Firma Hexadecimal
                .compact();

        System.out.println("\n=========================================================");
        System.out.println("🔑 COPIA ESTE TOKEN VIP (1):");
        System.out.println(token);
        System.out.println("=========================================================\n");
    }
}