package io.github.thingsdb.connector.exceptions;

public class MaxQuotaError extends TiException {
    public MaxQuotaError(String errorMessage) {
        super(errorMessage);
    }
}
