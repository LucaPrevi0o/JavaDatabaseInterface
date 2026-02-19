package com.lucaprevioo.jdbi.exception;

public class FailedTransactionException extends RuntimeException {

    public FailedTransactionException(String message) {
        super(message);
    }

    public FailedTransactionException(String message, Throwable cause) { super(message, cause); }
}
