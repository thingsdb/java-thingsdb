package io.github.thingsdb.connector.exceptions;

public class CancelledError extends TiException {
    public CancelledError(String errorMessage) {
        super(errorMessage);
    }
}
