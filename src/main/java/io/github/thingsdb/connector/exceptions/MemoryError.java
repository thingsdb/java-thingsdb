package io.github.thingsdb.connector.exceptions;

public class MemoryError extends TiException {
    public MemoryError(String errorMessage) {
        super(errorMessage);
    }
}
