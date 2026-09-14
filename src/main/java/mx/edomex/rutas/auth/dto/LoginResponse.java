package mx.edomex.rutas.auth.dto;

public record LoginResponse(String accessToken, String refreshToken, long expiraEnSegundos) {}
