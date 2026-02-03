package com.knowledgebase.wrapper.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;
import com.knowledgebase.wrapper.exception.*;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client wrapper using OkHttp3 for making API requests.
 * Handles request/response processing, error handling, and connection management.
 */
public class HttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private final OkHttpClient okHttpClient;
    private final KnowledgeBaseConfig config;
    private final Gson gson;
    
    public HttpClient(KnowledgeBaseConfig config) {
        this.config = config;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.okHttpClient = buildOkHttpClient(config);
    }
    
    private OkHttpClient buildOkHttpClient(KnowledgeBaseConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(
                        config.getMaxIdleConnections(),
                        config.getKeepAliveDuration(),
                        TimeUnit.MINUTES
                ))
                .addInterceptor(new AuthInterceptor(config.getAuthToken()))
                .retryOnConnectionFailure(true);
        
        if (config.isEnableLogging()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::info);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        
        return builder.build();
    }
    
    /**
     * Executes a GET request.
     */
    public <T> T get(String endpoint, Class<T> responseType) throws KnowledgeBaseException {
        String url = config.getBaseUrl() + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        return execute(request, responseType);
    }
    
    /**
     * Executes a POST request with JSON body.
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws KnowledgeBaseException {
        String url = config.getBaseUrl() + endpoint;
        String json = gson.toJson(requestBody);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        return execute(request, responseType);
    }
    
    /**
     * Executes a POST request with multipart form data.
     */
    public <T> T postMultipart(String endpoint, MultipartBody multipartBody, Class<T> responseType) 
            throws KnowledgeBaseException {
        String url = config.getBaseUrl() + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();
        
        return execute(request, responseType);
    }
    
    /**
     * Executes a DELETE request.
     */
    public <T> T delete(String endpoint, Class<T> responseType) throws KnowledgeBaseException {
        String url = config.getBaseUrl() + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        
        return execute(request, responseType);
    }
    
    /**
     * Executes an HTTP request and handles the response.
     */
    private <T> T execute(Request request, Class<T> responseType) throws KnowledgeBaseException {
        int retries = 0;
        KnowledgeBaseException lastException = null;
        
        while (retries <= config.getMaxRetries()) {
            try (Response response = okHttpClient.newCall(request).execute()) {
                return handleResponse(response, responseType);
            } catch (IOException e) {
                lastException = new KnowledgeBaseException("Network error: " + e.getMessage(), e);
                retries++;
                if (retries <= config.getMaxRetries()) {
                    logger.warn("Request failed, retrying ({}/{}): {}", 
                            retries, config.getMaxRetries(), e.getMessage());
                    try {
                        // Exponential backoff with configurable multiplier
                        long backoffTime = config.getRetryBackoffMultiplier() * retries;
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new KnowledgeBaseException("Request interrupted", ie);
                    }
                }
            } catch (KnowledgeBaseException e) {
                // Don't retry on application-level errors
                throw e;
            }
        }
        
        throw lastException;
    }
    
    /**
     * Handles HTTP response and converts to appropriate type or throws exception.
     */
    private <T> T handleResponse(Response response, Class<T> responseType) throws KnowledgeBaseException {
        int statusCode = response.code();
        
        try {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                handleErrorResponse(statusCode, responseBody);
            }
            
            if (responseType == Void.class || responseType == void.class) {
                return null;
            }
            
            return gson.fromJson(responseBody, responseType);
        } catch (IOException e) {
            throw new KnowledgeBaseException("Failed to read response body", e);
        }
    }
    
    /**
     * Handles error responses by throwing appropriate exceptions.
     */
    private void handleErrorResponse(int statusCode, String responseBody) throws KnowledgeBaseException {
        String errorMessage = "API request failed with status " + statusCode;
        
        if (responseBody != null && !responseBody.isEmpty()) {
            errorMessage += ": " + responseBody;
        }
        
        switch (statusCode) {
            case 400:
                throw new ValidationException(errorMessage);
            case 401:
            case 403:
                throw new AuthenticationException(errorMessage);
            case 404:
                throw new ResourceNotFoundException(errorMessage);
            default:
                throw new KnowledgeBaseException(errorMessage, statusCode);
        }
    }
    
    /**
     * Interceptor for adding authorization header to all requests.
     */
    private static class AuthInterceptor implements Interceptor {
        private final String authToken;
        
        public AuthInterceptor(String authToken) {
            this.authToken = authToken;
        }
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json");
            
            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }
    
    /**
     * Shuts down the HTTP client and releases resources.
     */
    public void shutdown() {
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
    }
}
