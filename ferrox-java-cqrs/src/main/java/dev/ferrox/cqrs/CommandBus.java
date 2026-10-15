package dev.ferrox.cqrs;

import dev.ferrox.core.result.Result;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Decoupled CommandBus dispatcher with native Java 21 Virtual Thread asynchronous dispatching.
 */
@Service
public class CommandBus {

    private final ApplicationContext context;
    private final Map<Class<?>, CommandHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public CommandBus(ApplicationContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <R, C extends Command<R>> R dispatch(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) resolveHandler(command.getClass());
        return handler.handle(command);
    }

    /**
     * Dispatches command asynchronously on a Java 21 Virtual Thread, wrapping output in Result.
     */
    public <R, C extends Command<R>> CompletableFuture<Result<R, Throwable>> dispatchAsync(C command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                R res = dispatch(command);
                return Result.success(res);
            } catch (Throwable t) {
                return Result.failure(t);
            }
        }, virtualThreadExecutor);
    }

    private CommandHandler<?, ?> resolveHandler(Class<?> cmdClass) {
        return handlerCache.computeIfAbsent(cmdClass, clazz -> {
            String[] names = context.getBeanNamesForType(CommandHandler.class);
            for (String name : names) {
                CommandHandler<?, ?> bean = context.getBean(name, CommandHandler.class);
                ResolvableType resolvableType = ResolvableType.forClass(bean.getClass()).as(CommandHandler.class);
                Class<?> generic = resolvableType.getGeneric(0).resolve();
                if (generic != null && generic.isAssignableFrom(clazz)) {
                    return bean;
                }
            }

            // Fallback: match by class naming convention or directly if single bean
            if (names.length == 1) {
                return context.getBean(names[0], CommandHandler.class);
            }

            for (String name : names) {
                CommandHandler<?, ?> bean = context.getBean(name, CommandHandler.class);
                if (bean.getClass().getSimpleName().toLowerCase().contains(clazz.getSimpleName().toLowerCase().replace("command", ""))) {
                    return bean;
                }
            }

            throw new IllegalArgumentException("No CommandHandler registered for " + clazz.getName());
        });
    }
}
