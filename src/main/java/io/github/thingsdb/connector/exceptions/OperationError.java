package io.github.thingsdb.connector.exceptions;

public class OperationError extends TiException {
    public OperationError(String errorMessage) {
        super(errorMessage);
    }
}
