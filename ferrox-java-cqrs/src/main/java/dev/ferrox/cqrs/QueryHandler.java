package dev.ferrox.cqrs;

/**
 * Interface for Query Handlers.
 */
public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}
