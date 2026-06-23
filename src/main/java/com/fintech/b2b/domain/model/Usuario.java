package com.fintech.b2b.domain.model;

import java.time.LocalDateTime;

public class Usuario {
    private Long id;
    private String email;
    private String passwordHash;
    private String rol;
    private LocalDateTime fechaCreacion;

    public Usuario(Long id, String email, String passwordHash, String rol, LocalDateTime fechaCreacion) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!esRolValido(rol)) {
            throw new IllegalArgumentException("Rol de usuario inválido: " + rol);
        }
        
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.fechaCreacion = (fechaCreacion != null) ? fechaCreacion : LocalDateTime.now();
    }

    private boolean esRolValido(String rol) {
        return "ROLE_TESORERO".equals(rol) || "ROLE_EMPLEADO".equals(rol);
    }

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRol() { return rol; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}