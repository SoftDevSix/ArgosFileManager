package org.argos.file.manager.exceptions;

/**
 * Base class for API exceptions.
 * Implements IApiException and extends RuntimeException.
 */
public abstract class ApiException extends RuntimeException implements IApiException {
    private final int statusCode;

    protected ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }
}
