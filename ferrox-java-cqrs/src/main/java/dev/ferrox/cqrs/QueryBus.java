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
 * Decoupled QueryBus dispatcher with native Java 21 Virtual Thread asynchronous dispatching.
 */
@Service
public class QueryBus {

    private final ApplicationContext context;
    private final Map<Class<?>, QueryHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public QueryBus(ApplicationContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <R, Q extends Query<R>> R dispatch(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) resolveHandler(query.getClass());
        return handler.handle(query);
    }

    /**
     * Dispatches query asynchronously on a Java 21 Virtual Thread, wrapping output in Result.
     */
    public <R, Q extends Query<R>> CompletableFuture<Result<R, Throwable>> dispatchAsync(Q query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                R res = dispatch(query);
                return Result.success(res);
            } catch (Throwable t) {
                return Result.failure(t);
            }
        }, virtualThreadExecutor);
    }

    private QueryHandler<?, ?> resolveHandler(Class<?> qClass) {
        return handlerCache.computeIfAbsent(qClass, clazz -> {
            String[] names = context.getBeanNamesForType(QueryHandler.class);
            for (String name : names) {
                QueryHandler<?, ?> bean = context.getBean(name, QueryHandler.class);
                ResolvableType resolvableType = ResolvableType.forClass(bean.getClass()).as(QueryHandler.class);
                Class<?> generic = resolvableType.getGeneric(0).resolve();
                if (generic != null && generic.isAssignableFrom(clazz)) {
                    return bean;
                }
            }

            if (names.length == 1) {
                return context.getBean(names[0], QueryHandler.class);
            }

            for (String name : names) {
                QueryHandler<?, ?> bean = context.getBean(name, QueryHandler.class);
                if (bean.getClass().getSimpleName().toLowerCase().contains(clazz.getSimpleName().toLowerCase().replace("query", ""))) {
                    return bean;
                }
            }

            throw new IllegalArgumentException("No QueryHandler registered for " + clazz.getName());
        });
    }
}
