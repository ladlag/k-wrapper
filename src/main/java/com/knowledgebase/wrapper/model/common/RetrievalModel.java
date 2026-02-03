package com.knowledgebase.wrapper.model.common;

import com.google.gson.annotations.SerializedName;

/**
 * Retrieval model configuration for knowledge base search.
 */
public class RetrievalModel {
    
    @SerializedName("search_method")
    private String searchMethod;
    
    @SerializedName("reranking_enable")
    private boolean rerankingEnable;
    
    @SerializedName("reranking_model")
    private RerankingModel rerankingModel;
    
    @SerializedName("top_k")
    private int topK;
    
    @SerializedName("score_threshold_enabled")
    private boolean scoreThresholdEnabled;
    
    @SerializedName("score_threshold")
    private double scoreThreshold;
    
    @SerializedName("reranking_mode")
    private String rerankingMode;
    
    @SerializedName("weights")
    private Weights weights;
    
    public RetrievalModel() {
    }
    
    public String getSearchMethod() {
        return searchMethod;
    }
    
    public void setSearchMethod(String searchMethod) {
        this.searchMethod = searchMethod;
    }
    
    public boolean isRerankingEnable() {
        return rerankingEnable;
    }
    
    public void setRerankingEnable(boolean rerankingEnable) {
        this.rerankingEnable = rerankingEnable;
    }
    
    public RerankingModel getRerankingModel() {
        return rerankingModel;
    }
    
    public void setRerankingModel(RerankingModel rerankingModel) {
        this.rerankingModel = rerankingModel;
    }
    
    public int getTopK() {
        return topK;
    }
    
    public void setTopK(int topK) {
        this.topK = topK;
    }
    
    public boolean isScoreThresholdEnabled() {
        return scoreThresholdEnabled;
    }
    
    public void setScoreThresholdEnabled(boolean scoreThresholdEnabled) {
        this.scoreThresholdEnabled = scoreThresholdEnabled;
    }
    
    public double getScoreThreshold() {
        return scoreThreshold;
    }
    
    public void setScoreThreshold(double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }
    
    public String getRerankingMode() {
        return rerankingMode;
    }
    
    public void setRerankingMode(String rerankingMode) {
        this.rerankingMode = rerankingMode;
    }
    
    public Weights getWeights() {
        return weights;
    }
    
    public void setWeights(Weights weights) {
        this.weights = weights;
    }
    
    public static class RerankingModel {
        @SerializedName("reranking_provider_name")
        private String rerankingProviderName;
        
        @SerializedName("reranking_model_name")
        private String rerankingModelName;
        
        public RerankingModel() {
        }
        
        public RerankingModel(String rerankingProviderName, String rerankingModelName) {
            this.rerankingProviderName = rerankingProviderName;
            this.rerankingModelName = rerankingModelName;
        }
        
        public String getRerankingProviderName() {
            return rerankingProviderName;
        }
        
        public void setRerankingProviderName(String rerankingProviderName) {
            this.rerankingProviderName = rerankingProviderName;
        }
        
        public String getRerankingModelName() {
            return rerankingModelName;
        }
        
        public void setRerankingModelName(String rerankingModelName) {
            this.rerankingModelName = rerankingModelName;
        }
    }
    
    public static class Weights {
        @SerializedName("weight_type")
        private String weightType;
        
        @SerializedName("vector_setting")
        private VectorSetting vectorSetting;
        
        @SerializedName("keyword_setting")
        private KeywordSetting keywordSetting;
        
        public Weights() {
        }
        
        public String getWeightType() {
            return weightType;
        }
        
        public void setWeightType(String weightType) {
            this.weightType = weightType;
        }
        
        public VectorSetting getVectorSetting() {
            return vectorSetting;
        }
        
        public void setVectorSetting(VectorSetting vectorSetting) {
            this.vectorSetting = vectorSetting;
        }
        
        public KeywordSetting getKeywordSetting() {
            return keywordSetting;
        }
        
        public void setKeywordSetting(KeywordSetting keywordSetting) {
            this.keywordSetting = keywordSetting;
        }
    }
    
    public static class VectorSetting {
        @SerializedName("vector_weight")
        private double vectorWeight;
        
        @SerializedName("embedding_provider_name")
        private String embeddingProviderName;
        
        @SerializedName("embedding_model_name")
        private String embeddingModelName;
        
        public VectorSetting() {
        }
        
        public VectorSetting(double vectorWeight) {
            this.vectorWeight = vectorWeight;
            this.embeddingProviderName = "";
            this.embeddingModelName = "";
        }
        
        public double getVectorWeight() {
            return vectorWeight;
        }
        
        public void setVectorWeight(double vectorWeight) {
            this.vectorWeight = vectorWeight;
        }
        
        public String getEmbeddingProviderName() {
            return embeddingProviderName;
        }
        
        public void setEmbeddingProviderName(String embeddingProviderName) {
            this.embeddingProviderName = embeddingProviderName;
        }
        
        public String getEmbeddingModelName() {
            return embeddingModelName;
        }
        
        public void setEmbeddingModelName(String embeddingModelName) {
            this.embeddingModelName = embeddingModelName;
        }
    }
    
    public static class KeywordSetting {
        @SerializedName("keyword_weight")
        private double keywordWeight;
        
        public KeywordSetting() {
        }
        
        public KeywordSetting(double keywordWeight) {
            this.keywordWeight = keywordWeight;
        }
        
        public double getKeywordWeight() {
            return keywordWeight;
        }
        
        public void setKeywordWeight(double keywordWeight) {
            this.keywordWeight = keywordWeight;
        }
    }
}
