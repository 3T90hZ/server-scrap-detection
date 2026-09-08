package com.scrapDetection.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    // Convenience constructors
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s không tồn tại với id: %d", resourceName, id), HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(String.format("%s Không tồn tại với %s: %s", resourceName, field, value), HttpStatus.NOT_FOUND);
    }
}
