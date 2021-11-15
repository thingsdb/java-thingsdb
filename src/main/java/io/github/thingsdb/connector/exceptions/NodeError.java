package io.github.thingsdb.connector.exceptions;

public class NodeError extends TiException {
    public NodeError(String errorMessage) {
        super(errorMessage);
    }
}
