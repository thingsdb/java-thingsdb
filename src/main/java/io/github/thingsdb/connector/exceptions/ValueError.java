package io.github.thingsdb.connector.exceptions;

public class ValueError extends TiException {
    public ValueError(String errorMessage) {
        super(errorMessage);
    }
}
