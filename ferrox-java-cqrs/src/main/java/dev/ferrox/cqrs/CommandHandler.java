package dev.ferrox.cqrs;

/**
 * Interface for Command Handlers.
 */
public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}
