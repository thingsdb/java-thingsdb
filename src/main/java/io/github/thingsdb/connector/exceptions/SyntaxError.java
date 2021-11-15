package io.github.thingsdb.connector.exceptions;

public class SyntaxError extends TiException {
    public SyntaxError(String errorMessage) {
        super(errorMessage);
    }
}
