package dev.ferrox.showcase.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/secure")
@Tag(name = "Sentinel Threat Protection", description = "Endpoints Protected by Real-Time Shannon Entropy and Injection Filters")
public class SecurityTestController {

    public record EchoRequest(String message) {}

    @PostMapping("/echo")
    @Operation(summary = "Echo Secure Payload", description = "Inspects request query strings, URI and payload. Blocked by SentinelThreatEngine if threat pattern or high entropy is detected.")
    public ResponseEntity<Map<String, Object>> echoPayload(
            @RequestParam(value = "queryParam", required = false) String queryParam,
            @RequestBody EchoRequest request) {

        return ResponseEntity.ok(Map.of(
                "status", "clean",
                "message", request.message() != null ? request.message() : "",
                "queryParam", queryParam != null ? queryParam : "",
                "sentinel_verified", true
        ));
    }
}
