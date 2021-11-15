package io.github.thingsdb.connector.exceptions;

public class AssertionError extends TiException {
    public AssertionError(String errorMessage) {
        super(errorMessage);
    }
}
