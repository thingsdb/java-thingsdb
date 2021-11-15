package io.github.thingsdb.connector.exceptions;

public class AuthError extends TiException {
    public AuthError(String errorMessage) {
        super(errorMessage);
    }
}
