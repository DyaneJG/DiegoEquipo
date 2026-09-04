package mx.edomex.rutas.auth.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${jwt.expiration-minutes}")
    private long expiracionMinutos;

    public String generarToken(String email, String rol) throws Exception {
        RSAPrivateKey privateKey = cargarLlavePrivada();
        Instant ahora = Instant.now();
        return Jwts.builder()
            .subject(email)
            .claim("rol", rol)
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    private RSAPrivateKey cargarLlavePrivada() throws Exception {
        String contenido = new String(privateKeyResource.getInputStream().readAllBytes())
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(contenido);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
            .generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }
}
