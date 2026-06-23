package com.fintech.b2b.infrastructure.security;

import com.fintech.b2b.domain.model.UserIdentity;
import com.fintech.b2b.domain.model.port.TokenProviderPort;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Inyección de la dependencia mediante la interfaz (Puerto)
    private final TokenProviderPort tokenProvider;

    public JwtAuthenticationFilter(TokenProviderPort tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Verificamos si la petición tiene el formato de token correcto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 2. Un único parseo: validamos la criptografía y extraemos la identidad simultáneamente
            UserIdentity identity = tokenProvider.validar(jwt);

            // 3. La firma, expiración, issuer y audience fueron validadas correctamente
            if (identity.email() != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Utilizamos el rol de la identidad (nuestro dominio ya exige el formato ROLE_TESORERO)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(identity.rol());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        identity,
                        null,
                        Collections.singletonList(authority)
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. Registramos al usuario en el contexto de Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (ExpiredJwtException e) {
            // 5. Los bloques catch ya no son código muerto.
            logger.warn("Petición rechazada: El token JWT ha expirado para la IP o el usuario.");
            SecurityContextHolder.clearContext();
        } catch (SignatureException e) {
            logger.error("Alerta de Seguridad Crítica: Intento de uso de un token con firma inválida.");
            SecurityContextHolder.clearContext();
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            logger.error("Petición rechazada: Token JWT malformado o corrupto.");
            SecurityContextHolder.clearContext();
        }
        
        catch (JwtException e) {
            logger.error("Error JWT", e);
            SecurityContextHolder.clearContext();
        }

        // 6. Pasamos la petición al siguiente filtro o controlador
        filterChain.doFilter(request, response);
    }
}