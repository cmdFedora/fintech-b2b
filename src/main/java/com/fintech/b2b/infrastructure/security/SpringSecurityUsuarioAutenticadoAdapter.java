package com.fintech.b2b.infrastructure.security;

import com.fintech.b2b.domain.model.UserIdentity;
import com.fintech.b2b.domain.model.exception.AccesoDenegadoException;
import com.fintech.b2b.domain.model.port.UsuarioAutenticadoPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityUsuarioAutenticadoAdapter implements UsuarioAutenticadoPort {

    @Override
    public Long getIdUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Cambio aplicado: Excepción de dominio para que responda con HTTP 403
            throw new AccesoDenegadoException("No se encontró un token de autenticación activo o válido para realizar esta operación.");
        }
        
        UserIdentity identity = (UserIdentity) authentication.getPrincipal();
        return identity.id(); 
    }
}