package dev.ferrox.security;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.lang.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * PASETO v4 Auth - Translates PASETO tokens into internal claims.
 */
@Service
public class PasetoTokenService {

    // For demo purposes. In production, load from a secure vault.
    private final SecretKey sharedKey = Keys.secretKey("12345678901234567890123456789012".getBytes());

    public String generateToken(String subject, String role) {
        return Pasetos.V2.LOCAL.builder()
                .setSharedSecret(sharedKey)
                .setSubject(subject)
                .claim("role", role)
                .setExpiration(Instant.now().plus(1, ChronoUnit.HOURS))
                .compact();
    }

    public Paseto verifyAndExtractClaims(String token) {
        return Pasetos.parserBuilder()
                .setSharedSecret(sharedKey)
                .build()
                .parse(token);
    }
}
