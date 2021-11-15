package io.github.thingsdb.connector.exceptions;

public class ZeroDivError extends TiException {
    public ZeroDivError(String errorMessage) {
        super(errorMessage);
    }
}
