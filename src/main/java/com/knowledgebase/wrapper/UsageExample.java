package com.knowledgebase.wrapper;

import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import com.knowledgebase.wrapper.model.common.DataSource;
import com.knowledgebase.wrapper.model.common.IndexingTechnique;
import com.knowledgebase.wrapper.model.request.InitDatasetRequest;
import com.knowledgebase.wrapper.model.response.InitDatasetResponse;
import com.knowledgebase.wrapper.service.FileService;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Example usage of the Knowledge Base API Wrapper.
 * This class demonstrates how to use the wrapper for common operations.
 * 
 * NOTE: This example uses try-with-resources for demonstration purposes only.
 * In production, you should create ONE client instance and reuse it throughout
 * your application lifecycle. See BEST_PRACTICES.md for proper usage patterns.
 */
public class UsageExample {
    
    /**
     * Demo example - for testing and understanding the API.
     * DO NOT use this pattern in production code!
     */
    public static void demoExample() {
        // Configuration - replace with your actual values
        String baseUrl = "https://your-kb-service.com";
        String authToken = "your-bearer-token-here";
        
        // Create configuration using builder pattern
        KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
                .baseUrl(baseUrl)
                .authToken(authToken)
                .connectTimeout(30)
                .readTimeout(60)
                .enableLogging(true)
                .maxRetries(3)
                .build();
        
        // For demo only: Use try-with-resources to automatically close client
        // In production: Create ONE client instance and reuse it!
        try (KnowledgeBaseClient client = new KnowledgeBaseClient(config)) {
            
            // Example 1: Upload a file
            System.out.println("=== Example 1: Upload File ===");
            File fileToUpload = new File("path/to/your/document.pdf");
            FileService.FileUploadResponse uploadResponse = client.uploadFile(fileToUpload);
            System.out.println("File uploaded with ID: " + uploadResponse.getId());
            
            // Example 2: Initialize dataset with uploaded file
            System.out.println("\n=== Example 2: Initialize Dataset ===");
            InitDatasetRequest initRequest = new InitDatasetRequest.Builder()
                    .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
                    .dataSource(DataSource.forUploadFile(
                            Collections.singletonList(uploadResponse.getId())
                    ))
                    .embeddingModel("text-embedding-v2")
                    .embeddingModelProvider("provider-name")
                    .build();
            
            InitDatasetResponse initResponse = client.initDataset(initRequest);
            String datasetId = initResponse.getDataset().getId();
            String batchId = initResponse.getBatch();
            System.out.println("Dataset initialized with ID: " + datasetId);
            
            // Example 3: Wait for indexing to complete
            System.out.println("\n=== Example 3: Check Indexing Status ===");
            Map<String, Object> indexingStatus = client.getIndexingStatus(datasetId, batchId);
            System.out.println("Indexing status: " + indexingStatus);
            
            // Example 4: List all datasets
            System.out.println("\n=== Example 4: List Datasets ===");
            List<Map<String, Object>> datasets = client.listDatasets();
            System.out.println("Total datasets: " + datasets.size());
            
            // Example 5: Get dataset details
            System.out.println("\n=== Example 5: Get Dataset Details ===");
            Map<String, Object> dataset = client.getDataset(datasetId);
            System.out.println("Dataset name: " + dataset.get("name"));
            
            // Example 6: Query the dataset
            System.out.println("\n=== Example 6: Query Dataset ===");
            String query = "What is the main topic of the document?";
            Map<String, Object> queryResults = client.queryDataset(datasetId, query);
            System.out.println("Query results: " + queryResults);
            
            // Example 7: Hit testing
            System.out.println("\n=== Example 7: Hit Testing ===");
            Map<String, Object> hitResults = client.hitTesting(datasetId, query);
            System.out.println("Hit test results: " + hitResults);
            
            // Example 8: List documents in dataset
            System.out.println("\n=== Example 8: List Documents ===");
            List<Map<String, Object>> documents = client.listDocuments(datasetId);
            System.out.println("Total documents: " + documents.size());
            
            if (!documents.isEmpty()) {
                String documentId = (String) documents.get(0).get("id");
                
                // Example 9: Rename document
                System.out.println("\n=== Example 9: Rename Document ===");
                Map<String, Object> renamedDoc = client.renameDocument(
                        datasetId, 
                        documentId, 
                        "New Document Name"
                );
                System.out.println("Document renamed: " + renamedDoc.get("name"));
                
                // Example 10: Delete document
                System.out.println("\n=== Example 10: Delete Document ===");
                client.deleteDocument(datasetId, documentId);
                System.out.println("Document deleted successfully");
            }
            
            // Example 11: Delete dataset
            System.out.println("\n=== Example 11: Delete Dataset ===");
            client.deleteDataset(datasetId);
            System.out.println("Dataset deleted successfully");
            
        } catch (KnowledgeBaseException e) {
            System.err.println("Error: " + e.getMessage());
            if (e.hasStatusCode()) {
                System.err.println("HTTP Status Code: " + e.getStatusCode());
            }
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        demoExample();
        // For production use, see ProductionExample below
    }
    
    /**
     * ✅ RECOMMENDED: Production-ready example with singleton pattern.
     * Create ONE client instance and reuse it throughout the application.
     */
    public static class ProductionExample {
        
        // Singleton instance - created once, reused everywhere
        private static volatile KnowledgeBaseClient instance;
        private static final Object lock = new Object();
        
        /**
         * Get the global singleton client instance.
         * This is the RECOMMENDED way to use the client in production.
         */
        public static KnowledgeBaseClient getClient() {
            if (instance == null) {
                synchronized (lock) {
                    if (instance == null) {
                        KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
                                .baseUrl(System.getenv("KB_SERVICE_URL"))
                                .authToken(System.getenv("KB_AUTH_TOKEN"))
                                .connectTimeout(30)
                                .readTimeout(120)
                                .maxIdleConnections(20)  // Connection pool
                                .keepAliveDuration(10)
                                .enableLogging(false)
                                .build();
                        
                        instance = new KnowledgeBaseClient(config);
                        
                        // Register shutdown hook to close client on application exit
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            if (instance != null) {
                                instance.close();
                            }
                        }));
                    }
                }
            }
            return instance;
        }
        
        /**
         * Example: Upload and query - reusing the same client instance
         */
        public static void example() throws KnowledgeBaseException {
            KnowledgeBaseClient client = getClient();
            
            // Multiple operations using the SAME client instance
            List<Map<String, Object>> datasets = client.listDatasets();
            
            if (!datasets.isEmpty()) {
                String datasetId = (String) datasets.get(0).get("id");
                Map<String, Object> results = client.queryDataset(datasetId, "test query");
                System.out.println("Query results: " + results);
            }
            
            // No need to close - will be closed on application shutdown
        }
    }
    
    /**
     * ✅ RECOMMENDED: Spring Boot example with dependency injection.
     * This is the BEST way to use the client in Spring Boot applications.
     */
    public static class SpringBootExample {
        
        // Injected singleton client - Spring manages lifecycle
        private final KnowledgeBaseClient client;
        
        // Constructor injection (recommended)
        public SpringBootExample(KnowledgeBaseClient client) {
            this.client = client;
        }
        
        /**
         * Business method: Create knowledge base from file.
         * Notice: Reuses the SAME client instance injected by Spring.
         */
        public String createKnowledgeBase(File file) throws KnowledgeBaseException {
            // Upload file using the singleton client
            FileService.FileUploadResponse uploadResponse = client.uploadFile(file);
            
            // Create dataset using the SAME client
            InitDatasetRequest request = new InitDatasetRequest.Builder()
                    .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
                    .dataSource(DataSource.forUploadFile(
                            Collections.singletonList(uploadResponse.getId())
                    ))
                    .embeddingModel("text-embedding-v2")
                    .embeddingModelProvider("provider-name")
                    .build();
            
            InitDatasetResponse response = client.initDataset(request);
            return response.getDataset().getId();
        }
        
        /**
         * Business method: Search in knowledge base.
         * Notice: Reuses the SAME client instance.
         */
        public Map<String, Object> search(String datasetId, String query) throws KnowledgeBaseException {
            // Reuse the singleton client - efficient and thread-safe
            return client.queryDataset(datasetId, query);
        }
        
        /**
         * Multiple operations example - all using the same client instance.
         */
        public void multipleOperations(String datasetId) throws KnowledgeBaseException {
            // All these calls reuse the same HTTP connection pool
            client.listDatasets();
            client.queryDataset(datasetId, "query 1");
            client.queryDataset(datasetId, "query 2");
            client.listDocuments(datasetId);
            // Very efficient! Connection pool is reused.
        }
        
        // No destroy() method needed - Spring will call close() automatically
        // because KnowledgeBaseClient implements AutoCloseable
    }
}
