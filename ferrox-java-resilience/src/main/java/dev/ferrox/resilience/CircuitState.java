package dev.ferrox.resilience;

public enum CircuitState {
    CLOSED,    // Everything is fine, allow calls
    OPEN,      // Remote service is down, fail fast
    HALF_OPEN  // Testing if remote service is back up
}
