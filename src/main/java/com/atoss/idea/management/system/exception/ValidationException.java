package com.atoss.idea.management.system.exception;

public class ValidationException extends Exception {

    public ValidationException() {

    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
