package com.knowledgebase.wrapper.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends KnowledgeBaseException {
    
    public AuthenticationException(String message) {
        super(message, 401);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, 401, cause);
    }
}
