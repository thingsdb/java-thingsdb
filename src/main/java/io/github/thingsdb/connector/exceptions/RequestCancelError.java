package io.github.thingsdb.connector.exceptions;

public class RequestCancelError extends TiException {
    public RequestCancelError(String errorMessage) {
        super(errorMessage);
    }
}
