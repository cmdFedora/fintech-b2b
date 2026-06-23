package com.fintech.b2b.domain.model;

public record UserIdentity(
        Long id,
        String email,
        String rol
) {}