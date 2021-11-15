package io.github.thingsdb.connector.exceptions;

public class ResultTooLargeError extends TiException {
    public ResultTooLargeError(String errorMessage) {
        super(errorMessage);
    }
}
