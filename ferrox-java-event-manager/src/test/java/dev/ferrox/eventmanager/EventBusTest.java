package dev.ferrox.eventmanager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EventBusTest {

    static class DummyEvent implements DomainEvent {
        @Override public String getEventId() { return UUID.randomUUID().toString(); }
        @Override public Instant getOccurredOn() { return Instant.now(); }
    }

    @Test
    void testAsyncEventDispatchToVirtualThread() throws InterruptedException {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        EventBus eventBus = new EventBus(context);

        CountDownLatch latch = new CountDownLatch(1);

        EventHandler<DummyEvent> dummyHandler = event -> latch.countDown();

        when(context.getBeanNamesForType((org.springframework.core.ResolvableType) any()))
                .thenReturn(new String[]{"dummyHandler"});
        when(context.getBean("dummyHandler")).thenReturn(dummyHandler);

        eventBus.publish(new DummyEvent());

        // Wait up to 2 seconds for the Virtual Thread to execute the handler
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed, "Event handler was not invoked asynchronously in time.");
    }
}
