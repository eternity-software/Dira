package com.diraapp.exceptions;

public class UnknownAuthorException extends RuntimeException {

    public UnknownAuthorException() {
        super("Author is unknown");
    }
}
