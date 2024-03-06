package com.diraapp.exceptions;

public class NoSuchValueException extends Exception {

    private final String key;

    public NoSuchValueException(String key) {
        super(key);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
