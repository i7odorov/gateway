package com.ivantodorov.gateway.domain.exception;

public class InvalidXmlRequestException extends RuntimeException {

    public InvalidXmlRequestException(String message) {
        super(message);
    }
}
