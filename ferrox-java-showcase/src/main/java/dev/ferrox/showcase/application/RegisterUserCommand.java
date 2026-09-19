package dev.ferrox.showcase.application;

import dev.ferrox.cqrs.Command;

public record RegisterUserCommand(String email, String password) implements Command<String> {
}
