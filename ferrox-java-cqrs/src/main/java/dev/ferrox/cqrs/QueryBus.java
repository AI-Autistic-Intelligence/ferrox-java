package dev.ferrox.cqrs;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decoupled QueryBus dispatcher.
 */
@Service
public class QueryBus {

    private final ApplicationContext context;
    private final Map<Class<?>, QueryHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();

    public QueryBus(ApplicationContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <R, Q extends Query<R>> R dispatch(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) handlerCache.computeIfAbsent(query.getClass(), qClass -> {
            String[] names = context.getBeanNamesForType(QueryHandler.class);
            for (String name : names) {
                QueryHandler<?, ?> bean = context.getBean(name, QueryHandler.class);
                // Simple generic resolution placeholder
            }
            throw new RuntimeException("No handler found for " + qClass.getName());
        });
        return handler.handle(query);
    }
}
