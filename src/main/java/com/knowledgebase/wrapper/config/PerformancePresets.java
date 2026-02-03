package com.knowledgebase.wrapper.config;

/**
 * Pre-configured settings for different performance scenarios.
 * Provides optimal configurations for various use cases.
 */
public class PerformancePresets {
    
    /**
     * Development configuration - for local testing and debugging.
     * - Lower connection limits
     * - Shorter timeouts
     * - Logging enabled
     */
    public static KnowledgeBaseConfig.Builder development(String baseUrl, String authToken) {
        return new KnowledgeBaseConfig.Builder()
                .baseUrl(baseUrl)
                .authToken(authToken)
                .connectTimeout(15)
                .readTimeout(30)
                .writeTimeout(30)
                .maxIdleConnections(5)
                .keepAliveDuration(5)
                .maxRetries(2)
                .retryBackoffMultiplier(1000)
                .enableLogging(true);
    }
    
    /**
     * Production configuration - balanced for typical production workloads.
     * - Moderate connection limits
     * - Standard timeouts
     * - Logging disabled for performance
     */
    public static KnowledgeBaseConfig.Builder production(String baseUrl, String authToken) {
        return new KnowledgeBaseConfig.Builder()
                .baseUrl(baseUrl)
                .authToken(authToken)
                .connectTimeout(30)
                .readTimeout(120)
                .writeTimeout(120)
                .maxIdleConnections(20)
                .keepAliveDuration(10)
                .maxRetries(3)
                .retryBackoffMultiplier(2000)
                .enableLogging(false);
    }
    
    /**
     * High concurrency configuration - optimized for 50+ concurrent requests.
     * - High connection limits (50 connections)
     * - Extended timeouts for large files
     * - Aggressive retry strategy
     * - Recommended for batch operations and high-traffic scenarios
     */
    public static KnowledgeBaseConfig.Builder highConcurrency(String baseUrl, String authToken) {
        return new KnowledgeBaseConfig.Builder()
                .baseUrl(baseUrl)
                .authToken(authToken)
                .connectTimeout(30)
                .readTimeout(180)      // 3 minutes for large files
                .writeTimeout(300)     // 5 minutes for large uploads
                .maxIdleConnections(50) // Support 50 concurrent connections
                .keepAliveDuration(15) // Longer keep-alive for connection reuse
                .maxRetries(5)         // More retries for reliability
                .retryBackoffMultiplier(1500) // Balanced backoff
                .enableLogging(false);
    }
    
    /**
     * Low latency configuration - optimized for fast, small requests.
     * - Moderate connection pool
     * - Short timeouts
     * - Quick retries
     * - Best for query-heavy workloads
     */
    public static KnowledgeBaseConfig.Builder lowLatency(String baseUrl, String authToken) {
        return new KnowledgeBaseConfig.Builder()
                .baseUrl(baseUrl)
                .authToken(authToken)
                .connectTimeout(10)
                .readTimeout(30)
                .writeTimeout(30)
                .maxIdleConnections(30)
                .keepAliveDuration(5)
                .maxRetries(3)
                .retryBackoffMultiplier(500) // Quick retries
                .enableLogging(false);
    }
    
    /**
     * Batch upload configuration - optimized for large file uploads.
     * - Very large connection pool
     * - Extended timeouts
     * - Patient retry strategy
     * - Recommended for uploadFilesBatch operations
     */
    public static KnowledgeBaseConfig.Builder batchUpload(String baseUrl, String authToken) {
        return new KnowledgeBaseConfig.Builder()
                .baseUrl(baseUrl)
                .authToken(authToken)
                .connectTimeout(60)     // 1 minute connect
                .readTimeout(300)       // 5 minutes read
                .writeTimeout(600)      // 10 minutes write for very large files
                .maxIdleConnections(50) // High concurrency for batch uploads
                .keepAliveDuration(20)  // Long keep-alive for batch operations
                .maxRetries(5)
                .retryBackoffMultiplier(3000) // Patient retries for large files
                .enableLogging(false);
    }
    
    /**
     * Custom configuration builder with recommended defaults.
     * Start with sensible defaults and customize as needed.
     */
    public static KnowledgeBaseConfig.Builder custom(String baseUrl, String authToken) {
        return production(baseUrl, authToken);
    }
}
