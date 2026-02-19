package com.lucaprevioo.jdbi.exception;

public class FailedValidationException extends IllegalStateException {

    public FailedValidationException(String message) { super(message); }

    public FailedValidationException(String message, Throwable cause) { super(message, cause); }
}

