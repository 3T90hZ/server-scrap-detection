package com.scrapDetection.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BaseException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public ResourceAlreadyExistsException(String resourceName, String field, String value) {
        super(String.format("%s already exists with %s: %s", resourceName, field, value), HttpStatus.CONFLICT);
    }
}
