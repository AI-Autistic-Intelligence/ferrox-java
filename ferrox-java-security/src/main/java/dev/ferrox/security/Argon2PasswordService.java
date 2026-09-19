package dev.ferrox.security;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Replicates ferrox-security Argon2id hashing.
 */
@Service
public class Argon2PasswordService {

    private final Argon2PasswordEncoder encoder;

    public Argon2PasswordService() {
        // Defaults: saltLength=16, hashLength=32, parallelism=1, memory=1<<14, iterations=3
        this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
