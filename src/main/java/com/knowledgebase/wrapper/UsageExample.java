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
 */
public class UsageExample {
    
    public static void main(String[] args) {
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
        
        // Use try-with-resources to automatically close client
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
    
    /**
     * Example of using the client in a Spring Boot service or similar.
     */
    public static class ServiceExample {
        
        private final KnowledgeBaseClient client;
        
        public ServiceExample(String baseUrl, String authToken) {
            KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
                    .baseUrl(baseUrl)
                    .authToken(authToken)
                    .enableLogging(false) // Disable in production
                    .build();
            
            this.client = new KnowledgeBaseClient(config);
        }
        
        /**
         * Business method: Create knowledge base from file.
         */
        public String createKnowledgeBase(File file) throws KnowledgeBaseException {
            // Upload file
            FileService.FileUploadResponse uploadResponse = client.uploadFile(file);
            
            // Create dataset
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
         */
        public Map<String, Object> search(String datasetId, String query) throws KnowledgeBaseException {
            return client.queryDataset(datasetId, query);
        }
        
        /**
         * Cleanup method - call when service is destroyed.
         */
        public void destroy() {
            client.close();
        }
    }
}
