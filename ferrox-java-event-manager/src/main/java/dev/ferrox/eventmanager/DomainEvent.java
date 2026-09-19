package dev.ferrox.eventmanager;

import java.time.Instant;

/**
 * Base interface for Domain Events in the system.
 */
public interface DomainEvent {
    String getEventId();
    Instant getOccurredOn();
}
