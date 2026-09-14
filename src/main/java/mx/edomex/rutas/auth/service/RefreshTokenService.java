package mx.edomex.rutas.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration-days}")
    private long expiracionDias;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generar(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, email, Duration.ofDays(expiracionDias));
        return token;
    }

    public Optional<String> obtenerEmail(String refreshToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + refreshToken));
    }

    public void revocar(String refreshToken) {
        redisTemplate.delete(PREFIX + refreshToken);
    }
}
