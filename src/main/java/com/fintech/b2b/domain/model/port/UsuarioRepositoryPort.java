package com.fintech.b2b.domain.model.port;

import com.fintech.b2b.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorId(Long id);
    Usuario guardar(Usuario usuario);
}