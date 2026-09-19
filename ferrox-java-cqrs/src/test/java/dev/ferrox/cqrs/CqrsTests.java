package dev.ferrox.cqrs;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CqrsTests {

    record TestCommand(String data) implements Command<String> {}
    
    class TestCommandHandler implements CommandHandler<TestCommand, String> {
        @Override
        public String handle(TestCommand command) {
            return command.data().toUpperCase();
        }
    }

    @Test
    void testCommandBusDispatch() {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeanNamesForType(CommandHandler.class)).thenReturn(new String[]{"testHandler"});
        when(context.getBean("testHandler", CommandHandler.class)).thenReturn(new TestCommandHandler());

        CommandBus bus = new CommandBus(context);
        String result = bus.dispatch(new TestCommand("hello"));
        assertEquals("HELLO", result);
    }
}
