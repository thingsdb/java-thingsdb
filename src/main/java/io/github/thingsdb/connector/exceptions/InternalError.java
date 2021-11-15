package io.github.thingsdb.connector.exceptions;

public class InternalError extends TiException {
    public InternalError(String errorMessage) {
        super(errorMessage);
    }
}
