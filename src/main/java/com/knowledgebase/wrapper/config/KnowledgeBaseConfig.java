package com.knowledgebase.wrapper.config;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for Knowledge Base API client.
 * This class holds all configuration parameters needed to connect to the Knowledge Base service.
 */
public class KnowledgeBaseConfig {
    
    private final String baseUrl;
    private final String authToken;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    private final boolean enableLogging;
    private final int maxRetries;
    private final int maxIdleConnections;
    private final long keepAliveDuration;

    private KnowledgeBaseConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.authToken = builder.authToken;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.enableLogging = builder.enableLogging;
        this.maxRetries = builder.maxRetries;
        this.maxIdleConnections = builder.maxIdleConnections;
        this.keepAliveDuration = builder.keepAliveDuration;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * Builder class for KnowledgeBaseConfig using Builder pattern.
     */
    public static class Builder {
        private String baseUrl;
        private String authToken;
        private int connectTimeout = 30;
        private int readTimeout = 60;
        private int writeTimeout = 60;
        private boolean enableLogging = false;
        private int maxRetries = 3;
        private int maxIdleConnections = 5;
        private long keepAliveDuration = 5;

        public Builder() {
        }

        /**
         * Sets the base URL of the Knowledge Base API.
         * @param baseUrl the base URL (required)
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the authentication token for API requests.
         * @param authToken the Bearer token (required)
         * @return this builder
         */
        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        /**
         * Sets the connection timeout in seconds.
         * @param connectTimeout timeout in seconds (default: 30)
         * @return this builder
         */
        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the read timeout in seconds.
         * @param readTimeout timeout in seconds (default: 60)
         * @return this builder
         */
        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Sets the write timeout in seconds.
         * @param writeTimeout timeout in seconds (default: 60)
         * @return this builder
         */
        public Builder writeTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        /**
         * Enables or disables HTTP request/response logging.
         * @param enableLogging true to enable logging (default: false)
         * @return this builder
         */
        public Builder enableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        /**
         * Sets the maximum number of retry attempts for failed requests.
         * @param maxRetries number of retries (default: 3)
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets the maximum number of idle connections in the pool.
         * @param maxIdleConnections number of connections (default: 5)
         * @return this builder
         */
        public Builder maxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        /**
         * Sets the keep-alive duration for connections in minutes.
         * @param keepAliveDuration duration in minutes (default: 5)
         * @return this builder
         */
        public Builder keepAliveDuration(long keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        /**
         * Builds the KnowledgeBaseConfig instance.
         * @return configured KnowledgeBaseConfig
         * @throws IllegalArgumentException if required fields are missing
         */
        public KnowledgeBaseConfig build() {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Base URL is required");
            }
            if (authToken == null || authToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Auth token is required");
            }
            return new KnowledgeBaseConfig(this);
        }
    }
}
