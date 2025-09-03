package com.portfolio.domain.exception;

public class ServiceException extends RuntimeException {
    private final Error error;

    public ServiceException(Error error) {
        super(error.code());
        this.error = error;
    }

    public ServiceException(Error error, String message) {
        super(message);
        this.error = error;
    }

    public ServiceException(Error error, Throwable cause) {
        super(cause);
        this.error = error;
    }

    public ServiceException(Error error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public String getErrorCode() {
        return error.code();
    }
} 