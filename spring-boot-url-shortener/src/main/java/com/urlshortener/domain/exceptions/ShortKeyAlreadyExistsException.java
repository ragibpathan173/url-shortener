package com.urlshortener.domain.exceptions;

public class ShortKeyAlreadyExistsException extends RuntimeException {
    public ShortKeyAlreadyExistsException(String shortKey) {
        super("Short key is already in use: " + shortKey);
    }
}
