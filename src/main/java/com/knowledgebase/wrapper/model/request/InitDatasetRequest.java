package com.knowledgebase.wrapper.model.request;

import com.google.gson.annotations.SerializedName;
import com.knowledgebase.wrapper.model.common.*;

/**
 * Request model for initializing a knowledge base with documents.
 * Uses Builder pattern for easier construction.
 */
public class InitDatasetRequest {
    
    @SerializedName("indexing_technique")
    private String indexingTechnique;
    
    @SerializedName("data_source")
    private DataSource dataSource;
    
    @SerializedName("process_rule")
    private ProcessRule processRule;
    
    @SerializedName("doc_form")
    private String docForm;
    
    @SerializedName("doc_language")
    private String docLanguage;
    
    @SerializedName("retrieval_model")
    private RetrievalModel retrievalModel;
    
    @SerializedName("embedding_model")
    private String embeddingModel;
    
    @SerializedName("embedding_model_provider")
    private String embeddingModelProvider;
    
    private InitDatasetRequest(Builder builder) {
        this.indexingTechnique = builder.indexingTechnique;
        this.dataSource = builder.dataSource;
        this.processRule = builder.processRule;
        this.docForm = builder.docForm;
        this.docLanguage = builder.docLanguage;
        this.retrievalModel = builder.retrievalModel;
        this.embeddingModel = builder.embeddingModel;
        this.embeddingModelProvider = builder.embeddingModelProvider;
    }
    
    public String getIndexingTechnique() {
        return indexingTechnique;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    public ProcessRule getProcessRule() {
        return processRule;
    }
    
    public String getDocForm() {
        return docForm;
    }
    
    public String getDocLanguage() {
        return docLanguage;
    }
    
    public RetrievalModel getRetrievalModel() {
        return retrievalModel;
    }
    
    public String getEmbeddingModel() {
        return embeddingModel;
    }
    
    public String getEmbeddingModelProvider() {
        return embeddingModelProvider;
    }
    
    public static class Builder {
        private String indexingTechnique;
        private DataSource dataSource;
        private ProcessRule processRule;
        private String docForm = "text_model";
        private String docLanguage = "English";
        private RetrievalModel retrievalModel;
        private String embeddingModel;
        private String embeddingModelProvider;
        
        public Builder indexingTechnique(IndexingTechnique technique) {
            this.indexingTechnique = technique.getValue();
            return this;
        }
        
        public Builder indexingTechnique(String technique) {
            this.indexingTechnique = technique;
            return this;
        }
        
        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }
        
        public Builder processRule(ProcessRule processRule) {
            this.processRule = processRule;
            return this;
        }
        
        public Builder docForm(String docForm) {
            this.docForm = docForm;
            return this;
        }
        
        public Builder docLanguage(String docLanguage) {
            this.docLanguage = docLanguage;
            return this;
        }
        
        public Builder retrievalModel(RetrievalModel retrievalModel) {
            this.retrievalModel = retrievalModel;
            return this;
        }
        
        public Builder embeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
            return this;
        }
        
        public Builder embeddingModelProvider(String embeddingModelProvider) {
            this.embeddingModelProvider = embeddingModelProvider;
            return this;
        }
        
        public InitDatasetRequest build() {
            if (dataSource == null) {
                throw new IllegalArgumentException("Data source is required");
            }
            if (processRule == null) {
                processRule = ProcessRule.createDefault();
            }
            return new InitDatasetRequest(this);
        }
    }
}
