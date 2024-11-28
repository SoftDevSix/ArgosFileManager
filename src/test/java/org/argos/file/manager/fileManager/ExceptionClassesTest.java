package org.argos.file.manager.fileManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.argos.file.manager.exceptions.ApiException;
import org.argos.file.manager.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class ExceptionClassesTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
    }

    /**
     * Test handling of ApiException.
     */
    @Test
    void handleApiException_ShouldReturnCorrectResponse() {
        ApiException apiException = new CustomApiException("Custom API error", 400);

        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleApiException(apiException);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Custom API error", body.get("error"));
        assertEquals(400, body.get("status"));
        assertNotNull(body.get("timestamp"));
    }

    /**
     * Test handling of general exceptions.
     */
    @Test
    void handleGeneralException_ShouldReturnInternalServerErrorResponse() {
        Exception generalException = new Exception("General error occurred");

        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleGeneralException(generalException);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals(500, body.get("status"));
        assertEquals("General error occurred", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    /**
     * Custom implementation of ApiException for testing purposes.
     */
    static class CustomApiException extends ApiException {
        protected CustomApiException(String message, int statusCode) {
            super(message, statusCode);
        }
    }
}
