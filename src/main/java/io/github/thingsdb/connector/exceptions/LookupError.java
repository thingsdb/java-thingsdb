package io.github.thingsdb.connector.exceptions;

public class LookupError extends TiException {
    public LookupError(String errorMessage) {
        super(errorMessage);
    }
}
