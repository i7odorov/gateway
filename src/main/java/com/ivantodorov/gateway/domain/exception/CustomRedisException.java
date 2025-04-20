package com.ivantodorov.gateway.domain.exception;

public class CustomRedisException extends RuntimeException {

    public CustomRedisException(String message) {
        super(message);
    }
}
