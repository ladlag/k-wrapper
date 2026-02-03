package com.knowledgebase.wrapper.spring;

import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;
import com.knowledgebase.wrapper.config.PerformancePresets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for Knowledge Base API client.
 * 
 * <p>This configuration will be automatically activated when:</p>
 * <ul>
 *   <li>KnowledgeBaseClient is on the classpath</li>
 *   <li>No custom KnowledgeBaseClient bean is defined</li>
 *   <li>knowledgebase.url property is configured</li>
 * </ul>
 * 
 * <p>Configuration example in application.yaml:</p>
 * <pre>
 * knowledgebase:
 *   url: https://your-kb-service.com
 *   auth-token: ${KB_AUTH_TOKEN}
 *   preset: highConcurrency  # or development, production, batchUpload, lowLatency
 *   # Or specify individual settings:
 *   connect-timeout: 30
 *   read-timeout: 120
 *   max-idle-connections: 50
 *   enable-logging: false
 * </pre>
 */
@Configuration
@ConditionalOnClass(KnowledgeBaseClient.class)
@EnableConfigurationProperties(KnowledgeBaseProperties.class)
@ConditionalOnProperty(prefix = "knowledgebase", name = "url")
public class KnowledgeBaseAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseAutoConfiguration.class);
    
    private KnowledgeBaseClient client;
    
    /**
     * Creates a KnowledgeBaseClient bean based on configuration properties.
     * Spring will automatically call close() when the context is destroyed
     * because KnowledgeBaseClient implements AutoCloseable.
     * 
     * @param properties the configuration properties from application.yaml
     * @return configured KnowledgeBaseClient instance
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public KnowledgeBaseClient knowledgeBaseClient(KnowledgeBaseProperties properties) {
        logger.info("Auto-configuring Knowledge Base Client");
        logger.info("Base URL: {}", properties.getUrl());
        
        KnowledgeBaseConfig config = buildConfig(properties);
        
        this.client = new KnowledgeBaseClient(config);
        
        logger.info("Knowledge Base Client auto-configured successfully");
        return this.client;
    }
    
    /**
     * Builds KnowledgeBaseConfig from properties.
     * Supports both preset-based and custom configuration.
     */
    private KnowledgeBaseConfig buildConfig(KnowledgeBaseProperties properties) {
        String preset = properties.getPreset();
        
        // If preset is specified, use it as base configuration
        if (preset != null && !preset.isEmpty()) {
            logger.info("Using performance preset: {}", preset);
            return buildFromPreset(preset, properties);
        }
        
        // Otherwise, build from individual properties
        logger.info("Using custom configuration");
        return new KnowledgeBaseConfig.Builder()
                .baseUrl(properties.getUrl())
                .authToken(properties.getAuthToken())
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout())
                .maxIdleConnections(properties.getMaxIdleConnections())
                .keepAliveDuration(properties.getKeepAliveDuration())
                .maxRetries(properties.getMaxRetries())
                .retryBackoffMultiplier(properties.getRetryBackoffMultiplier())
                .enableLogging(properties.isEnableLogging())
                .build();
    }
    
    /**
     * Builds configuration from preset.
     */
    private KnowledgeBaseConfig buildFromPreset(String preset, KnowledgeBaseProperties properties) {
        KnowledgeBaseConfig.Builder builder;
        
        switch (preset.toLowerCase()) {
            case "development":
                builder = PerformancePresets.development(properties.getUrl(), properties.getAuthToken());
                break;
            case "production":
                builder = PerformancePresets.production(properties.getUrl(), properties.getAuthToken());
                break;
            case "highconcurrency":
            case "high-concurrency":
                builder = PerformancePresets.highConcurrency(properties.getUrl(), properties.getAuthToken());
                break;
            case "batchupload":
            case "batch-upload":
                builder = PerformancePresets.batchUpload(properties.getUrl(), properties.getAuthToken());
                break;
            case "lowlatency":
            case "low-latency":
                builder = PerformancePresets.lowLatency(properties.getUrl(), properties.getAuthToken());
                break;
            default:
                logger.warn("Unknown preset '{}', using production preset", preset);
                builder = PerformancePresets.production(properties.getUrl(), properties.getAuthToken());
        }
        
        return builder.build();
    }
}
