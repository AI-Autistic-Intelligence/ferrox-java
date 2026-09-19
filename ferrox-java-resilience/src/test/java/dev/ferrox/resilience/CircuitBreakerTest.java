package dev.ferrox.resilience;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {

    @Test
    void testStateMachine() throws Exception {
        CircuitBreaker cb = new CircuitBreaker(2, 1); // threshold 2, timeout 1s

        assertEquals(CircuitState.CLOSED, cb.getState());

        Callable<String> success = () -> "OK";
        Callable<String> fail = () -> { throw new RuntimeException("Crash"); };

        // 1st Fail
        assertThrows(RuntimeException.class, () -> cb.execute(fail));
        assertEquals(CircuitState.CLOSED, cb.getState()); // Not open yet

        // 2nd Fail (Threshold reached)
        assertThrows(RuntimeException.class, () -> cb.execute(fail));
        assertEquals(CircuitState.OPEN, cb.getState()); // Now open

        // Fast-Fail (Immediately rejected)
        assertThrows(IllegalStateException.class, () -> cb.execute(success));

        // Wait for timeout
        Thread.sleep(1100);

        // Half-Open Success
        assertEquals("OK", cb.execute(success));
        assertEquals(CircuitState.CLOSED, cb.getState());
    }
}
