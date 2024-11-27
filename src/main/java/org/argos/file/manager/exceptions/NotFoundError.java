package org.argos.file.manager.exceptions;

/**
 * Exception for Not Found errors.
 */
public class NotFoundError extends ApiException {
    public NotFoundError(String message) {
        super(message, 404);
    }
}
