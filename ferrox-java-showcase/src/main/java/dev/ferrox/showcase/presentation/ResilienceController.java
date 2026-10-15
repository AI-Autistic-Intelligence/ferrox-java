package dev.ferrox.showcase.presentation;

import dev.ferrox.resilience.CircuitBreaker;
import dev.ferrox.resilience.CircuitState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/resilience")
@Tag(name = "Resilience & Fault Tolerance", description = "Virtual-Thread Aware Circuit Breaker and Failure Recovery")
public class ResilienceController {

    private final CircuitBreaker circuitBreaker = new CircuitBreaker(3, 5000); // 3 failures, 5s recovery
    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    @GetMapping("/unstable")
    @Operation(summary = "Call Unstable Downstream Dependency", description = "Protected by CircuitBreaker. Tripping after 3 consecutive failures will fast-fail with HTTP 503.")
    public ResponseEntity<Map<String, Object>> callUnstableEndpoint(@RequestParam(value = "fail", defaultValue = "false") boolean forceFail) {
        int attempt = attemptCounter.incrementAndGet();

        try {
            String result = circuitBreaker.execute(() -> {
                if (forceFail) {
                    throw new IllegalStateException("Simulated upstream dependency timeout/failure!");
                }
                return "Operation successful at attempt #" + attempt;
            });

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "result", result,
                    "circuit_state", circuitBreaker.getState().name()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "failed",
                    "error", e.getMessage(),
                    "circuit_state", circuitBreaker.getState().name()
            ));
        } catch (Exception e) {
            // Circuit Breaker OPEN fast-fail
            return ResponseEntity.status(503).body(Map.of(
                    "status", "circuit_breaker_open",
                    "error", e.getMessage(),
                    "circuit_state", circuitBreaker.getState().name()
            ));
        }
    }

    @GetMapping("/state")
    @Operation(summary = "Get Circuit Breaker State", description = "Returns CLOSED, OPEN, or HALF_OPEN.")
    public ResponseEntity<Map<String, Object>> getCircuitState() {
        CircuitState state = circuitBreaker.getState();
        return ResponseEntity.ok(Map.of(
                "state", state.name(),
                "failures_threshold", 3,
                "recovery_timeout_ms", 5000
        ));
    }
}
