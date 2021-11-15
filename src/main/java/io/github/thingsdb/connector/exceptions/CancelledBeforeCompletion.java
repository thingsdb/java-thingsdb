package io.github.thingsdb.connector.exceptions;

public class CancelledBeforeCompletion extends TiException {
    public CancelledBeforeCompletion(String errorMessage) {
        super(errorMessage);
    }
}
