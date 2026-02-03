package com.knowledgebase.wrapper.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends KnowledgeBaseException {
    
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, 404, cause);
    }
}
