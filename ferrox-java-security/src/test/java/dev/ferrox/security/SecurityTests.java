package dev.ferrox.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecurityTests {

    @Test
    void testArgon2() {
        Argon2PasswordService service = new Argon2PasswordService();
        String raw = "SuperSecureP@ssw0rd!";
        String hash = service.hashPassword(raw);
        
        assertTrue(hash.startsWith("$argon2"));
        assertTrue(service.verifyPassword(raw, hash));
        assertFalse(service.verifyPassword("WrongPassword!", hash));
    }

    @Test
    void testSentinelEntropy() {
        SentinelThreatEngine engine = new SentinelThreatEngine();
        
        // Low entropy string
        assertFalse(engine.evaluateThreat("127.0.0.1", "hello world"));
        
        // High entropy string (pseudo-random / encrypted payload simulation)
        StringBuilder highEntropy = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            highEntropy.append((char) i);
        }
        assertTrue(engine.evaluateThreat("127.0.0.1", highEntropy.toString()));
    }
}
