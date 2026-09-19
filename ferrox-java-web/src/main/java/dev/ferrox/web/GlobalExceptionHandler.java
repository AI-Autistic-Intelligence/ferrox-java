package dev.ferrox.web;

import dev.ferrox.core.AppError;
import dev.ferrox.core.ErrorCode;
import dev.ferrox.core.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates AppError into standard ErrorResponse JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppError.class)
    public ResponseEntity<ErrorResponse> handleAppError(AppError ex) {
        ErrorResponse res = new ErrorResponse(ex.getCode(), ex.getMessage(), ex.getStatusCode(), ex.getDetails());
        return ResponseEntity.status(ex.getStatusCode()).body(res);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse res = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", 500, ex.getMessage());
        return ResponseEntity.status(500).body(res);
    }
}
