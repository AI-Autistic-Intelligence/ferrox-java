package dev.ferrox.showcase.presentation;

import dev.ferrox.core.result.Result;
import dev.ferrox.cqrs.CommandBus;
import dev.ferrox.cqrs.QueryBus;
import dev.ferrox.showcase.application.GetUserQuery;
import dev.ferrox.showcase.application.RegisterUserCommand;
import dev.ferrox.showcase.application.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users CQRS", description = "Asynchronous Command & Query Bus Dispatched on Virtual Threads")
public class UserController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public UserController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Register User Command", description = "Dispatches RegisterUserCommand asynchronously on a Java 21 Virtual Thread.")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> register(@RequestBody RegisterUserCommand cmd) {
        return commandBus.dispatchAsync(cmd)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return ResponseEntity.ok(Map.of(
                                "status", "success",
                                "token", result.getOrNull(),
                                "virtual_thread", Thread.currentThread().isVirtual()
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "status", "error",
                                "message", result.getErrorOrNull().getMessage()
                        ));
                    }
                });
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get User Query", description = "Dispatches GetUserQuery asynchronously on a Java 21 Virtual Thread.")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getUser(@PathVariable("id") String id) {
        return queryBus.dispatchAsync(new GetUserQuery(id))
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        UserDto user = result.getOrNull();
                        return ResponseEntity.ok(Map.of(
                                "status", "success",
                                "data", user,
                                "virtual_thread", Thread.currentThread().isVirtual()
                        ));
                    } else {
                        return ResponseEntity.status(404).body(Map.of(
                                "status", "not_found",
                                "message", result.getErrorOrNull().getMessage()
                        ));
                    }
                });
    }
}
