package dev.ferrox.showcase.application;

import dev.ferrox.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class GetUserHandler implements QueryHandler<GetUserQuery, UserDto> {

    @Override
    public UserDto handle(GetUserQuery query) {
        // Simulated lookup (in production, fetch from PostgreSQL or cache)
        return new UserDto(
                query.userId(),
                "user." + query.userId() + "@ferrox.dev",
                "Enterprise User " + query.userId(),
                "USER",
                Instant.now().toString()
        );
    }
}
