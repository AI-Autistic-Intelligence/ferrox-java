package dev.ferrox.showcase.presentation;

import dev.ferrox.security.PasetoTokenService;
import dev.paseto.jpaseto.Paseto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "PASETO v4 Authenticated Cryptography Endpoints")
public class AuthController {

    private final PasetoTokenService tokenService;

    public AuthController(PasetoTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public record TokenRequest(String subject, String role) {}

    @PostMapping("/token")
    @Operation(summary = "Generate PASETO v4 Token", description = "Generates an authenticated PASETO v4 Local token using XChaCha20-Poly1305 symmetric encryption.")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody TokenRequest request) {
        String role = (request.role() != null && !request.role().isBlank()) ? request.role() : "USER";
        String token = tokenService.generateToken(request.subject(), role);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "token_type", "Bearer",
                "subject", request.subject(),
                "role", role
        ));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify PASETO Token", description = "Parses and verifies PASETO token claims in constant time.")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String rawToken = authHeader.substring(7).trim();
        try {
            Paseto paseto = tokenService.verifyAndExtractClaims(rawToken);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "subject", paseto.getClaims().getSubject(),
                    "role", paseto.getClaims().get("role", String.class),
                    "expiration", paseto.getClaims().getExpiration().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "valid", false,
                    "error", "Token verification failed: " + e.getMessage()
            ));
        }
    }
}
