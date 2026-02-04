package com.knowledgebase.wrapper.service;

import com.knowledgebase.wrapper.client.HttpClient;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Service for managing documents within datasets.
 * Provides operations for document CRUD and management.
 */
public class DocumentService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final HttpClient httpClient;
    
    public DocumentService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    /**
     * Lists all documents in a dataset.
     * 
     * @param datasetId the UUID of the dataset
     * @return list of documents
     * @throws KnowledgeBaseException if the request fails
     */
    public List<Map<String, Object>> listDocuments(String datasetId) throws KnowledgeBaseException {
        logger.info("Listing documents for dataset: {}", datasetId);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> documents = (List<Map<String, Object>>) httpClient.get(
                "/datasets/" + datasetId + "/documents",
                Object.class
        );
        
        return documents;
    }
    
    /**
     * Deletes a document from a dataset.
     * 
     * @param datasetId the UUID of the dataset
     * @param documentId the UUID of the document to delete
     * @throws KnowledgeBaseException if the request fails
     */
    public void deleteDocument(String datasetId, String documentId) throws KnowledgeBaseException {
        logger.info("Deleting document {} from dataset {}", documentId, datasetId);
        
        httpClient.delete(
                "/datasets/" + datasetId + "/documents/" + documentId,
                Void.class
        );
        
        logger.info("Document deleted successfully");
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
        logger.info("Renaming document {} in dataset {} to: {}", documentId, datasetId, newName);
        
        Map<String, Object> requestBody = Map.of("name", newName);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = httpClient.post(
                "/datasets/" + datasetId + "/documents/" + documentId + "/rename",
                requestBody,
                Map.class
        );
        
        logger.info("Document renamed successfully");
        return result;
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
        logger.info("Getting indexing status for batch {} in dataset {}", batchId, datasetId);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> status = httpClient.get(
                "/datasets/" + datasetId + "/batch/" + batchId + "/indexing-status",
                Map.class
        );
        
        return status;
    }
}
