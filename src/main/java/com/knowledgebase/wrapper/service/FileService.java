package com.knowledgebase.wrapper.service;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.knowledgebase.wrapper.client.HttpClient;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Service for uploading files to the knowledge base.
 */
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final HttpClient httpClient;
    private final Gson gson;
    
    public FileService(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new Gson();
    }
    
    /**
     * Uploads a file to the knowledge base.
     * 
     * @param file the file to upload
     * @return the upload response containing file ID and metadata
     * @throws KnowledgeBaseException if the upload fails
     */
    public FileUploadResponse uploadFile(File file) throws KnowledgeBaseException {
        if (!file.exists()) {
            throw new KnowledgeBaseException("File does not exist: " + file.getAbsolutePath());
        }
        
        if (!file.canRead()) {
            throw new KnowledgeBaseException("Cannot read file: " + file.getAbsolutePath());
        }
        
        logger.info("Uploading file: {} (size: {} bytes)", file.getName(), file.length());
        
        try {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            RequestBody fileBody = RequestBody.create(
                    file,
                    MediaType.parse(mimeType)
            );
            
            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();
            
            FileUploadResponse response = httpClient.postMultipart(
                    "/console/api/files/upload?source=datasets",
                    multipartBody,
                    FileUploadResponse.class
            );
            
            logger.info("File uploaded successfully with ID: {}", response.getId());
            return response;
            
        } catch (IOException e) {
            throw new KnowledgeBaseException("Failed to read file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Response model for file upload.
     */
    public static class FileUploadResponse {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("size")
        private long size;
        
        @SerializedName("extension")
        private String extension;
        
        @SerializedName("mime_type")
        private String mimeType;
        
        @SerializedName("created_by")
        private String createdBy;
        
        @SerializedName("created_at")
        private double createdAt;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public String getExtension() {
            return extension;
        }
        
        public void setExtension(String extension) {
            this.extension = extension;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
        
        public String getCreatedBy() {
            return createdBy;
        }
        
        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
        
        public double getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(double createdAt) {
            this.createdAt = createdAt;
        }
    }
}
