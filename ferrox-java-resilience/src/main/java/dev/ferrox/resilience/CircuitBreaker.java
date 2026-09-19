package dev.ferrox.resilience;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lightweight CircuitBreaker tailored for Virtual Thread pipelines.
 */
public class CircuitBreaker {

    private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    
    private final int failureThreshold;
    private final long openTimeoutSeconds;
    
    private Instant openedAt;

    public CircuitBreaker(int failureThreshold, long openTimeoutSeconds) {
        this.failureThreshold = failureThreshold;
        this.openTimeoutSeconds = openTimeoutSeconds;
    }

    public <T> T execute(Callable<T> action) throws Exception {
        if (state.get() == CircuitState.OPEN) {
            if (Instant.now().isAfter(openedAt.plusSeconds(openTimeoutSeconds))) {
                state.set(CircuitState.HALF_OPEN);
            } else {
                throw new IllegalStateException("Circuit is OPEN (Fail-Fast). Remote service is down.");
            }
        }

        try {
            T result = action.call();
            onSuccess();
            return result;
        } catch (Exception ex) {
            onFailure();
            throw ex;
        }
    }

    private void onSuccess() {
        if (state.get() == CircuitState.HALF_OPEN) {
            state.set(CircuitState.CLOSED);
            failureCount.set(0);
        } else if (state.get() == CircuitState.CLOSED) {
            failureCount.set(0);
        }
    }

    private void onFailure() {
        if (state.get() == CircuitState.HALF_OPEN) {
            state.set(CircuitState.OPEN);
            openedAt = Instant.now();
        } else if (state.get() == CircuitState.CLOSED) {
            if (failureCount.incrementAndGet() >= failureThreshold) {
                state.set(CircuitState.OPEN);
                openedAt = Instant.now();
            }
        }
    }
    
    public CircuitState getState() {
        return state.get();
    }
}
