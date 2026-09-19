package dev.ferrox.core;

import lombok.Getter;

@Getter
public class AppError extends RuntimeException {
    private final ErrorCode code;
    private final int statusCode;
    private final Object details;

    public AppError(ErrorCode code, String message, int statusCode) {
        this(code, message, statusCode, null);
    }

    public AppError(ErrorCode code, String message, int statusCode, Object details) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
        this.details = details;
    }
}
