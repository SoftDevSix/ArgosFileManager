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

    /**
     * Handles all exceptions extending ApiException.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", ex.getStatusCode());
        errorDetails.put("error", ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(errorDetails);
    }

    /**
     * Handles InternalServerError exceptions.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<Map<String, Object>> handleInternalServerError(InternalServerError ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", 500);
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", ex.getMessage());

        return ResponseEntity.status(500).body(errorDetails);
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
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", 500);
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", ex.getMessage());

        return ResponseEntity.status(500).body(errorDetails);
    }
}
