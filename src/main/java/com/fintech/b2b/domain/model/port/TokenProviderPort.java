package com.fintech.b2b.domain.model.port;

import com.fintech.b2b.domain.model.UserIdentity;

public interface TokenProviderPort {
    String generarToken(UserIdentity identity);
    UserIdentity validar(String token);
}