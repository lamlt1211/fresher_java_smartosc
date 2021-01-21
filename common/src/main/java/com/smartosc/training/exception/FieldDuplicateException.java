package com.smartosc.training.exception;

public class FieldDuplicateException extends RuntimeException {

    public FieldDuplicateException() { super(); }

    public FieldDuplicateException(String message) {
        super(message);
    }

    public FieldDuplicateException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
