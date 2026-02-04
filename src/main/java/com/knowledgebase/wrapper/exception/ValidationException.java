package com.knowledgebase.wrapper.exception;

/**
 * Exception thrown when request validation fails.
 */
public class ValidationException extends KnowledgeBaseException {
    
    public ValidationException(String message) {
        super(message, 400);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, 400, cause);
    }
}
