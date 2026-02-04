package com.knowledgebase.wrapper.service;

import com.knowledgebase.wrapper.client.HttpClient;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import com.knowledgebase.wrapper.model.common.DataSource;
import com.knowledgebase.wrapper.model.common.IndexingTechnique;
import com.knowledgebase.wrapper.model.request.InitDatasetRequest;
import com.knowledgebase.wrapper.model.response.InitDatasetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Batch upload service for uploading multiple files efficiently.
 * Supports concurrent batch uploads with configurable batch size.
 */
public class BatchUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchUploadService.class);
    
    private final FileService fileService;
    private final DatasetService datasetService;
    private final ExecutorService executorService;
    
    /**
     * Creates a new batch upload service with default thread pool.
     * 
     * @param httpClient the HTTP client
     */
    public BatchUploadService(HttpClient httpClient) {
        this(httpClient, createDefaultExecutor());
    }
    
    /**
     * Creates a new batch upload service with custom thread pool.
     * 
     * @param httpClient the HTTP client
     * @param executorService custom executor service for parallel uploads
     */
    public BatchUploadService(HttpClient httpClient, ExecutorService executorService) {
        this.fileService = new FileService(httpClient);
        this.datasetService = new DatasetService(httpClient);
        this.executorService = executorService;
    }
    
    /**
     * Creates default executor for concurrent uploads (50 threads).
     */
    private static ExecutorService createDefaultExecutor() {
        return new ThreadPoolExecutor(
            10, // core pool size
            50, // maximum pool size - supports 50 concurrent uploads
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "BatchUpload-" + counter++);
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    /**
     * Uploads multiple files in batches and creates a dataset.
     * 
     * @param files list of files to upload
     * @param batchSize number of files to upload concurrently in each batch
     * @param indexingTechnique indexing technique for the dataset
     * @return the created dataset response
     * @throws KnowledgeBaseException if upload or dataset creation fails
     */
    public InitDatasetResponse uploadAndCreateDataset(
            List<File> files,
            int batchSize,
            IndexingTechnique indexingTechnique) throws KnowledgeBaseException {
        
        logger.info("Starting batch upload of {} files with batch size {}", files.size(), batchSize);
        
        // Upload files in batches
        List<String> allFileIds = uploadFilesBatch(files, batchSize);
        
        // Create dataset with all uploaded files
        InitDatasetRequest request = new InitDatasetRequest.Builder()
                .indexingTechnique(indexingTechnique)
                .dataSource(DataSource.forUploadFile(allFileIds))
                .build();
        
        InitDatasetResponse response = datasetService.initDataset(request);
        
        logger.info("Batch upload completed. Dataset ID: {}", response.getDataset().getId());
        return response;
    }
    
    /**
     * Uploads files in batches with concurrent execution.
     * 
     * @param files list of files to upload
     * @param batchSize number of files to upload concurrently in each batch
     * @return list of uploaded file IDs
     * @throws KnowledgeBaseException if any upload fails
     */
    public List<String> uploadFilesBatch(List<File> files, int batchSize) throws KnowledgeBaseException {
        List<String> allFileIds = new ArrayList<>();
        
        // Split files into batches
        for (int i = 0; i < files.size(); i += batchSize) {
            int end = Math.min(i + batchSize, files.size());
            List<File> batch = files.subList(i, end);
            
            logger.info("Uploading batch {}/{} ({} files)", 
                    (i / batchSize) + 1, 
                    (files.size() + batchSize - 1) / batchSize,
                    batch.size());
            
            // Upload this batch concurrently
            List<String> batchFileIds = uploadBatchConcurrent(batch);
            allFileIds.addAll(batchFileIds);
            
            logger.info("Batch {}/{} completed successfully", 
                    (i / batchSize) + 1,
                    (files.size() + batchSize - 1) / batchSize);
        }
        
        logger.info("All {} files uploaded successfully", files.size());
        return allFileIds;
    }
    
    /**
     * Uploads a batch of files concurrently.
     * 
     * @param files files to upload
     * @return list of uploaded file IDs
     * @throws KnowledgeBaseException if any upload fails
     */
    private List<String> uploadBatchConcurrent(List<File> files) throws KnowledgeBaseException {
        List<CompletableFuture<String>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        FileService.FileUploadResponse response = fileService.uploadFile(file);
                        logger.debug("Uploaded file: {} -> {}", file.getName(), response.getId());
                        return response.getId();
                    } catch (KnowledgeBaseException e) {
                        throw new CompletionException(e);
                    }
                }, executorService))
                .collect(Collectors.toList());
        
        try {
            // Wait for all uploads in this batch to complete
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            
            allOf.get(5, TimeUnit.MINUTES); // Timeout for batch
            
            // Collect all file IDs
            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                    
        } catch (TimeoutException e) {
            throw new KnowledgeBaseException("Batch upload timeout after 5 minutes", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KnowledgeBaseException("Batch upload interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompletionException && cause.getCause() instanceof KnowledgeBaseException) {
                throw (KnowledgeBaseException) cause.getCause();
            }
            throw new KnowledgeBaseException("Batch upload failed: " + cause.getMessage(), cause);
        }
    }
    
    /**
     * Uploads files with progress callback.
     * 
     * @param files files to upload
     * @param batchSize batch size
     * @param progressCallback callback to report progress (completed, total)
     * @return list of uploaded file IDs
     * @throws KnowledgeBaseException if upload fails
     */
    public List<String> uploadFilesWithProgress(
            List<File> files,
            int batchSize,
            ProgressCallback progressCallback) throws KnowledgeBaseException {
        
        List<String> allFileIds = new ArrayList<>();
        int completed = 0;
        
        for (int i = 0; i < files.size(); i += batchSize) {
            int end = Math.min(i + batchSize, files.size());
            List<File> batch = files.subList(i, end);
            
            List<String> batchFileIds = uploadBatchConcurrent(batch);
            allFileIds.addAll(batchFileIds);
            
            completed += batch.size();
            if (progressCallback != null) {
                progressCallback.onProgress(completed, files.size());
            }
        }
        
        return allFileIds;
    }
    
    /**
     * Shuts down the executor service.
     * Call this when the service is no longer needed.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Progress callback interface for upload progress reporting.
     */
    @FunctionalInterface
    public interface ProgressCallback {
        /**
         * Called when upload progress is updated.
         * 
         * @param completed number of files completed
         * @param total total number of files
         */
        void onProgress(int completed, int total);
    }
    
    /**
     * Builder for batch upload configuration.
     */
    public static class BatchUploadBuilder {
        private List<File> files;
        private int batchSize = 10;
        private IndexingTechnique indexingTechnique = IndexingTechnique.HIGH_QUALITY;
        private ProgressCallback progressCallback;
        
        public BatchUploadBuilder files(List<File> files) {
            this.files = files;
            return this;
        }
        
        public BatchUploadBuilder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }
        
        public BatchUploadBuilder indexingTechnique(IndexingTechnique technique) {
            this.indexingTechnique = technique;
            return this;
        }
        
        public BatchUploadBuilder onProgress(ProgressCallback callback) {
            this.progressCallback = callback;
            return this;
        }
        
        public List<File> getFiles() {
            return files;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public IndexingTechnique getIndexingTechnique() {
            return indexingTechnique;
        }
        
        public ProgressCallback getProgressCallback() {
            return progressCallback;
        }
    }
}
