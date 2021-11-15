package io.github.thingsdb.connector.exceptions;

public class OverflowError extends TiException {
    public OverflowError(String errorMessage) {
        super(errorMessage);
    }
}
