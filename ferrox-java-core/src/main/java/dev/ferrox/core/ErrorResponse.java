package dev.ferrox.core;

import java.time.Instant;
import java.util.UUID;

public record ErrorResponse(
    String id,
    ErrorCode code,
    String message,
    int statusCode,
    Object details,
    Instant timestamp
) {
    public ErrorResponse(ErrorCode code, String message, int statusCode, Object details) {
        this(UUID.randomUUID().toString(), code, message, statusCode, details, Instant.now());
    }
}
