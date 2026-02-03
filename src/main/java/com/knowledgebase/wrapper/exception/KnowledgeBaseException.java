package com.knowledgebase.wrapper.exception;

/**
 * Base exception class for all Knowledge Base API related exceptions.
 */
public class KnowledgeBaseException extends Exception {
    
    private final int statusCode;
    
    public KnowledgeBaseException(String message) {
        super(message);
        this.statusCode = -1;
    }
    
    public KnowledgeBaseException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }
    
    public KnowledgeBaseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public KnowledgeBaseException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public boolean hasStatusCode() {
        return statusCode > 0;
    }
}
