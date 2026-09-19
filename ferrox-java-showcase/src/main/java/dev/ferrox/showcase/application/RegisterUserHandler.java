package dev.ferrox.showcase.application;

import dev.ferrox.cqrs.CommandHandler;
import dev.ferrox.data.SingleflightGroup;
import dev.ferrox.security.PasetoTokenService;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserHandler implements CommandHandler<RegisterUserCommand, String> {

    private final SingleflightGroup singleflight;
    private final PasetoTokenService tokenService;

    public RegisterUserHandler(SingleflightGroup singleflight, PasetoTokenService tokenService) {
        this.singleflight = singleflight;
        this.tokenService = tokenService;
    }

    @Override
    public String handle(RegisterUserCommand command) {
        // Use singleflight to prevent DB dogpiling if the user double-clicks register
        return singleflight.work("register_user_" + command.email(), () -> {
            
            // Simulate slow db insert
            Thread.sleep(500); 
            
            // Generate a secure PASETO token for the new user
            return tokenService.generateToken(command.email(), "USER");
        });
    }
}
