package com.urlshortener.domain.exceptions;

public class InvalidOriginalUrlException extends RuntimeException {
    public InvalidOriginalUrlException(String message) {
        super(message);
    }
}
