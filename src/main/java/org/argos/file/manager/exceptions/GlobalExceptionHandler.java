package org.argos.file.manager.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    /**
     * Handles all exceptions implementing IApiException.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(STATUS, ex.getStatusCode());
        errorDetails.put(ERROR, ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(errorDetails);
    }

    /**
     * Handles all other exceptions as a fallback.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with generic error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(STATUS, 500);
        errorDetails.put(ERROR, "Internal Server Error");
        errorDetails.put(MESSAGE, ex.getMessage());

        return ResponseEntity.status(500).body(errorDetails);
    }
}
