package dev.ferrox.observability;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FerroxLoggerTest {

    @Test
    void testJsonRedaction() {
        // Setup Logback ListAppender to capture stdout
        Logger logger = (Logger) LoggerFactory.getLogger(FerroxLoggerTest.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        FerroxLogger ferroxLogger = new FerroxLogger(FerroxLoggerTest.class);
        MDC.put(CorrelationIdFilter.CORRELATION_ID_KEY, "test-uuid-123");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("userId", 5);
        ctx.put("password", "superSecret123!");
        ctx.put("token", "paseto_v4_local_...");

        ferroxLogger.info("User login attempt", ctx);

        String jsonLog = listAppender.list.get(0).getFormattedMessage();

        // Assert JSON formatting
        assertTrue(jsonLog.contains("\"level\":\"INFO\""));
        assertTrue(jsonLog.contains("\"correlationId\":\"test-uuid-123\""));
        assertTrue(jsonLog.contains("\"userId\":5"));

        // Assert Redaction
        assertTrue(jsonLog.contains("\"password\":\"[REDACTED]\""));
        assertTrue(jsonLog.contains("\"token\":\"[REDACTED]\""));
        assertFalse(jsonLog.contains("superSecret123!"));
    }
}
