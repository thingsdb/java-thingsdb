package io.github.thingsdb.connector.exceptions;

public class ForbiddenError extends TiException {
    public ForbiddenError(String errorMessage) {
        super(errorMessage);
    }
}
