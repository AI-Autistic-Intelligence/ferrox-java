package dev.ferrox.eventmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * In-process asynchronous EventBus powered by Virtual Threads.
 */
@Service
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);
    private final ApplicationContext context;
    private final ExecutorService executor;

    public EventBus(ApplicationContext context) {
        this.context = context;
        // Project Loom: Virtual Thread per task for ultra-high concurrency Pub/Sub
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @SuppressWarnings("unchecked")
    public <E extends DomainEvent> void publish(E event) {
        ResolvableType type = ResolvableType.forClassWithGenerics(EventHandler.class, event.getClass());
        String[] beanNames = context.getBeanNamesForType(type);

        for (String beanName : beanNames) {
            EventHandler<E> handler = (EventHandler<E>) context.getBean(beanName);
            executor.submit(() -> {
                try {
                    log.debug("Dispatching event {} to {}", event.getClass().getSimpleName(), handler.getClass().getSimpleName());
                    handler.onEvent(event);
                } catch (Exception ex) {
                    log.error("Failed to handle event", ex);
                }
            });
        }
    }
}
