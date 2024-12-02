package org.argos.file.manager.exceptions;

/**
 * Exception for Bad Request errors.
 */
public class BadRequestError extends ApiException {
    public BadRequestError(String message) {
        super(message, 400);
    }
}
