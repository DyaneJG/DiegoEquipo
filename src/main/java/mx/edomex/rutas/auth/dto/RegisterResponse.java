package mx.edomex.rutas.auth.dto;

import java.util.UUID;

public record RegisterResponse(UUID id, String email, String rol) {}
