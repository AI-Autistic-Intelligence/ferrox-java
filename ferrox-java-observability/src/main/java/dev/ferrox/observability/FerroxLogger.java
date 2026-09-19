package dev.ferrox.observability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ultra-fast JSON logger inspired by Pino (Node.js).
 * Redacts sensitive fields (like 'password', 'token') automatically.
 */
public class FerroxLogger {

    private final Logger slf4jLogger;
    private static final ObjectMapper mapper = new ObjectMapper();

    public FerroxLogger(Class<?> clazz) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
    }

    public void info(String message, Map<String, Object> additionalContext) {
        if (slf4jLogger.isInfoEnabled()) {
            slf4jLogger.info(buildJson("INFO", message, additionalContext));
        }
    }

    public void error(String message, Throwable ex) {
        if (slf4jLogger.isErrorEnabled()) {
            Map<String, Object> ctx = new LinkedHashMap<>();
            ctx.put("error", ex.getMessage());
            ctx.put("stack", ex.getStackTrace()[0].toString());
            slf4jLogger.error(buildJson("ERROR", message, ctx));
        }
    }

    private String buildJson(String level, String msg, Map<String, Object> ctx) {
        Map<String, Object> logNode = new LinkedHashMap<>();
        logNode.put("time", Instant.now().toString());
        logNode.put("level", level);
        logNode.put("msg", msg);
        logNode.put("correlationId", MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY));

        if (ctx != null) {
            Map<String, Object> redactedCtx = new LinkedHashMap<>();
            ctx.forEach((k, v) -> {
                if (k.toLowerCase().contains("password") || k.toLowerCase().contains("token")) {
                    redactedCtx.put(k, "[REDACTED]");
                } else {
                    redactedCtx.put(k, v);
                }
            });
            logNode.put("ctx", redactedCtx);
        }

        try {
            return mapper.writeValueAsString(logNode);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"failed_to_serialize_log\"}";
        }
    }
}
