package com.knowledgebase.wrapper.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Response model for dataset initialization.
 */
public class InitDatasetResponse {
    
    @SerializedName("dataset")
    private Dataset dataset;
    
    @SerializedName("documents")
    private List<Document> documents;
    
    @SerializedName("batch")
    private String batch;
    
    public Dataset getDataset() {
        return dataset;
    }
    
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
    
    public List<Document> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    
    public String getBatch() {
        return batch;
    }
    
    public void setBatch(String batch) {
        this.batch = batch;
    }
    
    public static class Dataset {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("permission")
        private String permission;
        
        @SerializedName("data_source_type")
        private String dataSourceType;
        
        @SerializedName("indexing_technique")
        private String indexingTechnique;
        
        @SerializedName("created_by")
        private String createdBy;
        
        @SerializedName("created_at")
        private long createdAt;
        
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
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getPermission() {
            return permission;
        }
        
        public void setPermission(String permission) {
            this.permission = permission;
        }
        
        public String getDataSourceType() {
            return dataSourceType;
        }
        
        public void setDataSourceType(String dataSourceType) {
            this.dataSourceType = dataSourceType;
        }
        
        public String getIndexingTechnique() {
            return indexingTechnique;
        }
        
        public void setIndexingTechnique(String indexingTechnique) {
            this.indexingTechnique = indexingTechnique;
        }
        
        public String getCreatedBy() {
            return createdBy;
        }
        
        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
    }
    
    public static class Document {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("position")
        private int position;
        
        @SerializedName("dataset_id")
        private String datasetId;
        
        @SerializedName("data_source_type")
        private String dataSourceType;
        
        @SerializedName("data_source_info")
        private Map<String, Object> dataSourceInfo;
        
        @SerializedName("indexing_status")
        private String indexingStatus;
        
        @SerializedName("enabled")
        private boolean enabled;
        
        @SerializedName("created_by")
        private String createdBy;
        
        @SerializedName("created_at")
        private long createdAt;
        
        @SerializedName("word_count")
        private int wordCount;
        
        @SerializedName("tokens")
        private int tokens;
        
        @SerializedName("error")
        private String error;
        
        @SerializedName("display_status")
        private String displayStatus;
        
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
        
        public int getPosition() {
            return position;
        }
        
        public void setPosition(int position) {
            this.position = position;
        }
        
        public String getDatasetId() {
            return datasetId;
        }
        
        public void setDatasetId(String datasetId) {
            this.datasetId = datasetId;
        }
        
        public String getDataSourceType() {
            return dataSourceType;
        }
        
        public void setDataSourceType(String dataSourceType) {
            this.dataSourceType = dataSourceType;
        }
        
        public Map<String, Object> getDataSourceInfo() {
            return dataSourceInfo;
        }
        
        public void setDataSourceInfo(Map<String, Object> dataSourceInfo) {
            this.dataSourceInfo = dataSourceInfo;
        }
        
        public String getIndexingStatus() {
            return indexingStatus;
        }
        
        public void setIndexingStatus(String indexingStatus) {
            this.indexingStatus = indexingStatus;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getCreatedBy() {
            return createdBy;
        }
        
        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
        
        public int getWordCount() {
            return wordCount;
        }
        
        public void setWordCount(int wordCount) {
            this.wordCount = wordCount;
        }
        
        public int getTokens() {
            return tokens;
        }
        
        public void setTokens(int tokens) {
            this.tokens = tokens;
        }
        
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
        
        public String getDisplayStatus() {
            return displayStatus;
        }
        
        public void setDisplayStatus(String displayStatus) {
            this.displayStatus = displayStatus;
        }
    }
}
