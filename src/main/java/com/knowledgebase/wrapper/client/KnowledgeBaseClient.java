package com.knowledgebase.wrapper.client;

import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import com.knowledgebase.wrapper.model.request.InitDatasetRequest;
import com.knowledgebase.wrapper.model.response.InitDatasetResponse;
import com.knowledgebase.wrapper.service.DatasetService;
import com.knowledgebase.wrapper.service.DocumentService;
import com.knowledgebase.wrapper.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Main client facade for Knowledge Base API operations.
 * This is the primary entry point for all knowledge base operations.
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Create configuration
 * KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
 *     .baseUrl("https://your-kb-service.com")
 *     .authToken("your-bearer-token")
 *     .enableLogging(true)
 *     .build();
 * 
 * // Create client
 * KnowledgeBaseClient client = new KnowledgeBaseClient(config);
 * 
 * // Upload files and create dataset
 * FileService.FileUploadResponse file = client.uploadFile(new File("document.pdf"));
 * InitDatasetRequest request = new InitDatasetRequest.Builder()
 *     .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
 *     .dataSource(DataSource.forUploadFile(Collections.singletonList(file.getId())))
 *     .build();
 * InitDatasetResponse response = client.initDataset(request);
 * 
 * // Query the dataset
 * Map&lt;String, Object&gt; results = client.queryDataset(response.getDataset().getId(), "your query");
 * 
 * // Clean up
 * client.close();
 * </pre>
 */
public class KnowledgeBaseClient implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseClient.class);
    
    private final HttpClient httpClient;
    private final DatasetService datasetService;
    private final DocumentService documentService;
    private final FileService fileService;
    
    /**
     * Creates a new Knowledge Base client with the given configuration.
     * 
     * @param config the configuration for the client
     */
    public KnowledgeBaseClient(KnowledgeBaseConfig config) {
        logger.info("Initializing Knowledge Base Client for: {}", config.getBaseUrl());
        
        this.httpClient = new HttpClient(config);
        this.datasetService = new DatasetService(httpClient);
        this.documentService = new DocumentService(httpClient);
        this.fileService = new FileService(httpClient);
    }
    
    // ==================== File Operations ====================
    
    /**
     * Uploads a file to the knowledge base.
     * 
     * @param file the file to upload
     * @return the upload response containing file ID and metadata
     * @throws KnowledgeBaseException if the upload fails
     */
    public FileService.FileUploadResponse uploadFile(File file) throws KnowledgeBaseException {
        return fileService.uploadFile(file);
    }
    
    // ==================== Dataset Operations ====================
    
    /**
     * Initializes a new knowledge base with documents.
     * 
     * @param request the initialization request
     * @return the initialized dataset response
     * @throws KnowledgeBaseException if the request fails
     */
    public InitDatasetResponse initDataset(InitDatasetRequest request) throws KnowledgeBaseException {
        return datasetService.initDataset(request);
    }
    
    /**
     * Lists all datasets.
     * 
     * @return list of datasets
     * @throws KnowledgeBaseException if the request fails
     */
    public List<Map<String, Object>> listDatasets() throws KnowledgeBaseException {
        return datasetService.listDatasets();
    }
    
    /**
     * Gets details of a specific dataset.
     * 
     * @param datasetId the UUID of the dataset
     * @return dataset details
     * @throws KnowledgeBaseException if the request fails
     */
    public Map<String, Object> getDataset(String datasetId) throws KnowledgeBaseException {
        return datasetService.getDataset(datasetId);
    }
    
    /**
     * Deletes a dataset.
     * 
     * @param datasetId the UUID of the dataset to delete
     * @throws KnowledgeBaseException if the request fails
     */
    public void deleteDataset(String datasetId) throws KnowledgeBaseException {
        datasetService.deleteDataset(datasetId);
    }
    
    /**
     * Queries a dataset with a text query.
     * 
     * @param datasetId the UUID of the dataset
     * @param query the query text
     * @return query results
     * @throws KnowledgeBaseException if the request fails
     */
    public Map<String, Object> queryDataset(String datasetId, String query) throws KnowledgeBaseException {
        return datasetService.queryDataset(datasetId, query);
    }
    
    /**
     * Tests hit rate for a dataset query.
     * 
     * @param datasetId the UUID of the dataset
     * @param query the query text
     * @return hit testing results
     * @throws KnowledgeBaseException if the request fails
     */
    public Map<String, Object> hitTesting(String datasetId, String query) throws KnowledgeBaseException {
        return datasetService.hitTesting(datasetId, query);
    }
    
    // ==================== Document Operations ====================
    
    /**
     * Lists all documents in a dataset.
     * 
     * @param datasetId the UUID of the dataset
     * @return list of documents
     * @throws KnowledgeBaseException if the request fails
     */
    public List<Map<String, Object>> listDocuments(String datasetId) throws KnowledgeBaseException {
        return documentService.listDocuments(datasetId);
    }
    
    /**
     * Deletes a document from a dataset.
     * 
     * @param datasetId the UUID of the dataset
     * @param documentId the UUID of the document
     * @throws KnowledgeBaseException if the request fails
     */
    public void deleteDocument(String datasetId, String documentId) throws KnowledgeBaseException {
        documentService.deleteDocument(datasetId, documentId);
    }
    
    /**
     * Renames a document in a dataset.
     * 
     * @param datasetId the UUID of the dataset
     * @param documentId the UUID of the document
     * @param newName the new name for the document
     * @return the updated document information
     * @throws KnowledgeBaseException if the request fails
     */
    public Map<String, Object> renameDocument(String datasetId, String documentId, String newName) 
            throws KnowledgeBaseException {
        return documentService.renameDocument(datasetId, documentId, newName);
    }
    
    /**
     * Gets the indexing status for a batch of documents.
     * 
     * @param datasetId the UUID of the dataset
     * @param batchId the batch ID
     * @return indexing status information
     * @throws KnowledgeBaseException if the request fails
     */
    public Map<String, Object> getIndexingStatus(String datasetId, String batchId) 
            throws KnowledgeBaseException {
        return documentService.getIndexingStatus(datasetId, batchId);
    }
    
    /**
     * Closes the client and releases resources.
     */
    @Override
    public void close() {
        logger.info("Closing Knowledge Base Client");
        httpClient.shutdown();
    }
}
