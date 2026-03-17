package com.lucaprevioo.jdbi.exception;

public class FailedRollbackException extends RuntimeException  {

    public FailedRollbackException(String message) { super(message); }

    public FailedRollbackException(String message, Throwable cause) { super(message, cause); }
}
