package dev.ferrox.showcase.presentation;

import dev.ferrox.cqrs.CommandBus;
import dev.ferrox.security.RequireRole;
import dev.ferrox.showcase.application.RegisterUserCommand;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CommandBus commandBus;

    public UserController(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterUserCommand cmd) {
        // Dispatches to the CommandHandler, decoupled from the controller layer
        return commandBus.dispatch(cmd);
    }

    @GetMapping("/admin-area")
    @RequireRole("ADMIN")
    public String adminOnly() {
        return "Welcome to the secure area";
    }
}
