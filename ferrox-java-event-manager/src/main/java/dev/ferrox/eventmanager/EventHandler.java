package dev.ferrox.eventmanager;

/**
 * Listener for specific domain events.
 */
public interface EventHandler<E extends DomainEvent> {
    void onEvent(E event);
}
