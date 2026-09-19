package dev.ferrox.cqrs;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decoupled CommandBus dispatcher.
 */
@Service
public class CommandBus {

    private final ApplicationContext context;
    private final Map<Class<?>, CommandHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();

    public CommandBus(ApplicationContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <R, C extends Command<R>> R dispatch(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) handlerCache.computeIfAbsent(command.getClass(), cmdClass -> {
            String[] names = context.getBeanNamesForType(CommandHandler.class);
            for (String name : names) {
                CommandHandler<?, ?> bean = context.getBean(name, CommandHandler.class);
                // In a real implementation, we'd use reflection (ResolvableType) to check generic bounds
                // to match the exact handler for this command type.
                // For simplicity in this demo, we assume the user implements it correctly.
            }
            throw new RuntimeException("No handler found for " + cmdClass.getName());
        });
        return handler.handle(command);
    }
}
