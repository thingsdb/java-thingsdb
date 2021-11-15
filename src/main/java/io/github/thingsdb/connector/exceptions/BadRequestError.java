package io.github.thingsdb.connector.exceptions;

public class BadRequestError extends TiException {
    public BadRequestError(String errorMessage) {
        super(errorMessage);
    }
}
