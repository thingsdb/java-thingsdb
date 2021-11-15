package io.github.thingsdb.connector.exceptions;

public class RequestTimeoutError extends TiException {
    public RequestTimeoutError(String errorMessage) {
        super(errorMessage);
    }
}
