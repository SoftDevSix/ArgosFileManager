package org.argos.file.manager.exceptions;

/**
 * Interface for custom API exception types.
 * <p>
 * This interface is used to define the structure of exceptions that can be thrown
 * in the API. It ensures that the exception provides a message and an HTTP status code.
 * </p>
 */
public interface IApiException {

    /**
     * Gets the message of the exception.
     * <p>
     * This method provides a detailed message about the exception, typically describing
     * the reason or context of the error.
     * </p>
     *
     * @return the message associated with the exception
     */
    String getMessage();

    /**
     * Gets the HTTP status code associated with the exception.
     * <p>
     * This method returns an HTTP status code that represents the error condition,
     * such as 404 for "Not Found" or 500 for "Internal Server Error".
     * </p>
     *
     * @return the HTTP status code for the exception
     */
    int getStatusCode();
}
