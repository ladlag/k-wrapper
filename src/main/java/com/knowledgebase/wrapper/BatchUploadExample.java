package com.knowledgebase.wrapper;

import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;
import com.knowledgebase.wrapper.config.PerformancePresets;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import com.knowledgebase.wrapper.model.common.IndexingTechnique;
import com.knowledgebase.wrapper.model.response.InitDatasetResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch upload examples demonstrating high-concurrency features.
 */
public class BatchUploadExample {
    
    /**
     * Example 1: Simple batch upload with 50 concurrent connections.
     */
    public static void simpleBatchUpload() throws KnowledgeBaseException {
        // Use high concurrency preset for 50 concurrent connections
        KnowledgeBaseConfig config = PerformancePresets
                .highConcurrency("https://your-service.com", "your-token")
                .build();
        
        KnowledgeBaseClient client = new KnowledgeBaseClient(config);
        
        // Prepare files
        List<File> files = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            files.add(new File("documents/doc" + i + ".pdf"));
        }
        
        // Upload in batches of 20 (concurrent)
        System.out.println("Uploading " + files.size() + " files...");
        List<String> fileIds = client.uploadFilesBatch(files, 20);
        System.out.println("Uploaded successfully: " + fileIds.size() + " files");
    }
    
    /**
     * Example 2: Batch upload with progress tracking.
     */
    public static void batchUploadWithProgress() throws KnowledgeBaseException {
        KnowledgeBaseConfig config = PerformancePresets
                .highConcurrency("https://your-service.com", "your-token")
                .build();
        
        KnowledgeBaseClient client = new KnowledgeBaseClient(config);
        
        List<File> files = prepareFiles(100);
        
        long startTime = System.currentTimeMillis();
        
        // Upload with progress callback
        List<String> fileIds = client.uploadFilesWithProgress(
                files,
                20,  // Batch size: 20 concurrent uploads
                (completed, total) -> {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    double rate = completed * 1000.0 / Math.max(1, System.currentTimeMillis() - startTime);
                    
                    System.out.printf("Progress: %d/%d (%.1f%%) - Speed: %.2f files/sec - Elapsed: %d sec\n",
                            completed, total, completed * 100.0 / total, rate, elapsed);
                }
        );
        
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("Completed! %d files in %d seconds (%.2f files/sec)\n",
                fileIds.size(), totalTime, fileIds.size() / (double) totalTime);
    }
    
    /**
     * Example 3: One-step upload and create dataset.
     */
    public static void oneStepUploadAndCreate() throws KnowledgeBaseException {
        // Use batch upload preset optimized for large file uploads
        KnowledgeBaseConfig config = PerformancePresets
                .batchUpload("https://your-service.com", "your-token")
                .build();
        
        KnowledgeBaseClient client = new KnowledgeBaseClient(config);
        
        List<File> files = prepareFiles(50);
        
        // Upload and create dataset in one operation
        System.out.println("Uploading files and creating dataset...");
        InitDatasetResponse response = client.uploadAndCreateDataset(
                files,
                15,  // Batch size
                IndexingTechnique.HIGH_QUALITY
        );
        
        System.out.println("Success! Dataset ID: " + response.getDataset().getId());
        System.out.println("Documents: " + response.getDocuments().size());
    }
    
    /**
     * Example 4: Performance comparison.
     */
    public static void performanceComparison() throws KnowledgeBaseException {
        String baseUrl = "https://your-service.com";
        String token = "your-token";
        
        List<File> files = prepareFiles(50);
        
        // Test 1: Small batch size (5)
        System.out.println("\n=== Test 1: Batch size = 5 ===");
        testUpload(baseUrl, token, files, 5);
        
        // Test 2: Medium batch size (10)
        System.out.println("\n=== Test 2: Batch size = 10 ===");
        testUpload(baseUrl, token, files, 10);
        
        // Test 3: Large batch size (20)
        System.out.println("\n=== Test 3: Batch size = 20 ===");
        testUpload(baseUrl, token, files, 20);
        
        // Test 4: Maximum batch size (50)
        System.out.println("\n=== Test 4: Batch size = 50 ===");
        testUpload(baseUrl, token, files, 50);
    }
    
    /**
     * Helper method to test upload with different batch sizes.
     */
    private static void testUpload(String baseUrl, String token, List<File> files, int batchSize) 
            throws KnowledgeBaseException {
        
        KnowledgeBaseConfig config = PerformancePresets
                .highConcurrency(baseUrl, token)
                .build();
        
        KnowledgeBaseClient client = new KnowledgeBaseClient(config);
        
        long startTime = System.currentTimeMillis();
        
        List<String> fileIds = client.uploadFilesBatch(files, batchSize);
        
        long duration = System.currentTimeMillis() - startTime;
        double rate = fileIds.size() * 1000.0 / duration;
        
        System.out.printf("Files: %d, Batch: %d, Time: %.2f sec, Rate: %.2f files/sec\n",
                fileIds.size(), batchSize, duration / 1000.0, rate);
    }
    
    /**
     * Helper method to prepare test files.
     */
    private static List<File> prepareFiles(int count) {
        List<File> files = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            files.add(new File("documents/doc" + i + ".pdf"));
        }
        return files;
    }
    
    /**
     * Example 5: Error handling in batch upload.
     */
    public static void batchUploadWithErrorHandling() {
        KnowledgeBaseConfig config = PerformancePresets
                .highConcurrency("https://your-service.com", "your-token")
                .build();
        
        KnowledgeBaseClient client = new KnowledgeBaseClient(config);
        
        List<File> files = prepareFiles(100);
        
        try {
            List<String> fileIds = client.uploadFilesWithProgress(
                    files,
                    20,
                    (completed, total) -> {
                        System.out.printf("Progress: %d/%d (%.1f%%)\n",
                                completed, total, completed * 100.0 / total);
                    }
            );
            
            System.out.println("All files uploaded successfully: " + fileIds.size());
            
        } catch (KnowledgeBaseException e) {
            System.err.println("Upload failed: " + e.getMessage());
            
            // Handle different error scenarios
            if (e.getStatusCode() == 401) {
                System.err.println("Authentication failed. Please check your token.");
            } else if (e.getStatusCode() == 413) {
                System.err.println("Payload too large. Try reducing batch size.");
            } else if (e.getMessage().contains("timeout")) {
                System.err.println("Upload timeout. Try smaller batch size or increase timeout.");
            } else {
                System.err.println("Unexpected error. Status code: " + e.getStatusCode());
            }
            
            // Option: Retry with smaller batch size
            System.out.println("Retrying with smaller batch size...");
            try {
                List<String> fileIds = client.uploadFilesBatch(files, 10);
                System.out.println("Retry successful: " + fileIds.size() + " files uploaded");
            } catch (KnowledgeBaseException retryError) {
                System.err.println("Retry also failed: " + retryError.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Batch Upload Examples ===\n");
            
            // Run examples
            // simpleBatchUpload();
            // batchUploadWithProgress();
            // oneStepUploadAndCreate();
            // performanceComparison();
            // batchUploadWithErrorHandling();
            
            System.out.println("\n✅ All examples completed!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
