package com.knowledgebase.wrapper.service;

import com.knowledgebase.wrapper.client.HttpClient;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import com.knowledgebase.wrapper.model.request.InitDatasetRequest;
import com.knowledgebase.wrapper.model.response.InitDatasetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Service for managing knowledge base datasets.
 * Provides CRUD operations for knowledge bases.
 */
public class DatasetService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);
    private final HttpClient httpClient;
    
    public DatasetService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    /**
     * Initializes a new knowledge base with documents.
     * 
     * @param request the initialization request containing dataset configuration
     * @return the initialized dataset response with dataset and document information
     * @throws KnowledgeBaseException if the request fails
     */
    public InitDatasetResponse initDataset(InitDatasetRequest request) throws KnowledgeBaseException {
        logger.info("Initializing dataset with {} file(s)", 
                request.getDataSource().getInfoList().getFileInfoList().getFileIds().size());
        
        InitDatasetResponse response = httpClient.post(
                "/console/api/datasets/init",
                request,
                InitDatasetResponse.class
        );
        
        logger.info("Dataset initialized successfully with ID: {}", 
                response.getDataset().getId());
        
        return response;
    }
    
    /**
     * Lists all datasets.
     * 
     * @return list of datasets
     * @throws KnowledgeBaseException if the request fails
     */
    public List<Map<String, Object>> listDatasets() throws KnowledgeBaseException {
        logger.info("Listing all datasets");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> datasets = (List<Map<String, Object>>) httpClient.get(
                "/console/api/datasets",
                Object.class
        );
        
        return datasets;
    }
    
    /**
     * Gets details of a specific dataset.
     * 
     * @param datasetId the UUID of the dataset
     * @return dataset details
     * @throws KnowledgeBaseException if the request fails
     */
    public Map<String, Object> getDataset(String datasetId) throws KnowledgeBaseException {
        logger.info("Getting dataset details for ID: {}", datasetId);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> dataset = httpClient.get(
                "/datasets/" + datasetId,
                Map.class
        );
        
        return dataset;
    }
    
    /**
     * Deletes a dataset.
     * 
     * @param datasetId the UUID of the dataset to delete
     * @throws KnowledgeBaseException if the request fails
     */
    public void deleteDataset(String datasetId) throws KnowledgeBaseException {
        logger.info("Deleting dataset with ID: {}", datasetId);
        
        httpClient.delete(
                "/datasets/" + datasetId,
                Void.class
        );
        
        logger.info("Dataset deleted successfully");
    }
    
    /**
     * Queries a dataset.
     * 
     * @param datasetId the UUID of the dataset to query
     * @param query the query text
     * @return query results
     * @throws KnowledgeBaseException if the request fails
     */
    public Map<String, Object> queryDataset(String datasetId, String query) throws KnowledgeBaseException {
        logger.info("Querying dataset {} with query: {}", datasetId, query);
        
        Map<String, Object> requestBody = Map.of("query", query);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> results = httpClient.post(
                "/datasets/" + datasetId + "/queries",
                requestBody,
                Map.class
        );
        
        return results;
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
        logger.info("Performing hit testing on dataset {} with query: {}", datasetId, query);
        
        Map<String, Object> requestBody = Map.of("query", query);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> results = httpClient.post(
                "/datasets/" + datasetId + "/hit-testing",
                requestBody,
                Map.class
        );
        
        return results;
    }
}
