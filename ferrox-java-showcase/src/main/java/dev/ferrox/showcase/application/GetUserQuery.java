package dev.ferrox.showcase.application;

import dev.ferrox.cqrs.Query;

public record GetUserQuery(String userId) implements Query<UserDto> {
}
