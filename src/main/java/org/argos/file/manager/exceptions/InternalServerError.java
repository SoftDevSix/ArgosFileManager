package org.argos.file.manager.exceptions;

/**
 * Exception for Internal Server errors.
 */
public class InternalServerError extends ApiException {
    public InternalServerError(String message) {
        super(message, 500);
    }
}
