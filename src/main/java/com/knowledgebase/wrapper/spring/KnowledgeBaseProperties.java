package com.knowledgebase.wrapper.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Knowledge Base API client.
 * Supports configuration via application.yaml or application.properties.
 * 
 * <p>Example configuration in application.yaml:</p>
 * <pre>
 * knowledgebase:
 *   url: https://your-kb-service.com
 *   auth-token: your-bearer-token
 *   connect-timeout: 30
 *   read-timeout: 120
 *   write-timeout: 120
 *   max-idle-connections: 50
 *   keep-alive-duration: 10
 *   max-retries: 3
 *   retry-backoff-multiplier: 2000
 *   enable-logging: false
 * </pre>
 */
@ConfigurationProperties(prefix = "knowledgebase")
public class KnowledgeBaseProperties {
    
    /**
     * Base URL of the Knowledge Base API service.
     */
    private String url;
    
    /**
     * Authentication token (Bearer token) for API access.
     */
    private String authToken;
    
    /**
     * Connection timeout in seconds.
     */
    private int connectTimeout = 30;
    
    /**
     * Read timeout in seconds.
     */
    private int readTimeout = 120;
    
    /**
     * Write timeout in seconds.
     */
    private int writeTimeout = 120;
    
    /**
     * Maximum number of idle connections in the pool.
     */
    private int maxIdleConnections = 50;
    
    /**
     * Keep-alive duration for connections in minutes.
     */
    private long keepAliveDuration = 10;
    
    /**
     * Maximum number of retry attempts for failed requests.
     */
    private int maxRetries = 3;
    
    /**
     * Retry backoff multiplier in milliseconds.
     */
    private int retryBackoffMultiplier = 2000;
    
    /**
     * Enable HTTP request/response logging.
     */
    private boolean enableLogging = false;
    
    /**
     * Performance preset to use (development, production, highConcurrency, batchUpload, lowLatency).
     * If specified, it will override individual settings.
     */
    private String preset;
    
    // Getters and Setters
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public int getWriteTimeout() {
        return writeTimeout;
    }
    
    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }
    
    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }
    
    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }
    
    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }
    
    public void setKeepAliveDuration(long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public int getRetryBackoffMultiplier() {
        return retryBackoffMultiplier;
    }
    
    public void setRetryBackoffMultiplier(int retryBackoffMultiplier) {
        this.retryBackoffMultiplier = retryBackoffMultiplier;
    }
    
    public boolean isEnableLogging() {
        return enableLogging;
    }
    
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
    
    public String getPreset() {
        return preset;
    }
    
    public void setPreset(String preset) {
        this.preset = preset;
    }
}
