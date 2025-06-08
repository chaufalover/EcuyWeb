package com.project.ecuy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenAccessException extends RuntimeException {
    
    public ForbiddenAccessException() {
        super();
    }
    
    public ForbiddenAccessException(String message) {
        super(message);
    }
    
    public ForbiddenAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
