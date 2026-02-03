package com.knowledgebase.wrapper.model.common;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Process rule configuration for document processing.
 */
public class ProcessRule {
    
    @SerializedName("mode")
    private String mode;
    
    @SerializedName("rules")
    private Rules rules;
    
    public ProcessRule() {
    }
    
    public ProcessRule(String mode, Rules rules) {
        this.mode = mode;
        this.rules = rules;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public Rules getRules() {
        return rules;
    }
    
    public void setRules(Rules rules) {
        this.rules = rules;
    }
    
    public static class Rules {
        @SerializedName("pre_processing_rules")
        private List<PreProcessingRule> preProcessingRules;
        
        @SerializedName("segmentation")
        private Segmentation segmentation;
        
        public Rules() {
            this.preProcessingRules = new ArrayList<>();
        }
        
        public Rules(List<PreProcessingRule> preProcessingRules, Segmentation segmentation) {
            this.preProcessingRules = preProcessingRules;
            this.segmentation = segmentation;
        }
        
        public List<PreProcessingRule> getPreProcessingRules() {
            return preProcessingRules;
        }
        
        public void setPreProcessingRules(List<PreProcessingRule> preProcessingRules) {
            this.preProcessingRules = preProcessingRules;
        }
        
        public Segmentation getSegmentation() {
            return segmentation;
        }
        
        public void setSegmentation(Segmentation segmentation) {
            this.segmentation = segmentation;
        }
    }
    
    public static class PreProcessingRule {
        @SerializedName("id")
        private String id;
        
        @SerializedName("enabled")
        private boolean enabled;
        
        public PreProcessingRule() {
        }
        
        public PreProcessingRule(String id, boolean enabled) {
            this.id = id;
            this.enabled = enabled;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Segmentation {
        @SerializedName("separator")
        private String separator;
        
        @SerializedName("max_tokens")
        private int maxTokens;
        
        @SerializedName("chunk_overlap")
        private int chunkOverlap;
        
        public Segmentation() {
        }
        
        public Segmentation(String separator, int maxTokens, int chunkOverlap) {
            this.separator = separator;
            this.maxTokens = maxTokens;
            this.chunkOverlap = chunkOverlap;
        }
        
        public String getSeparator() {
            return separator;
        }
        
        public void setSeparator(String separator) {
            this.separator = separator;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public int getChunkOverlap() {
            return chunkOverlap;
        }
        
        public void setChunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
        }
    }
    
    /**
     * Creates a default custom process rule with common settings.
     */
    public static ProcessRule createDefault() {
        List<PreProcessingRule> preProcessingRules = new ArrayList<>();
        preProcessingRules.add(new PreProcessingRule("remove_extra_spaces", true));
        preProcessingRules.add(new PreProcessingRule("remove_urls_emails", false));
        
        Segmentation segmentation = new Segmentation("\\n\\n", 500, 50);
        Rules rules = new Rules(preProcessingRules, segmentation);
        
        return new ProcessRule("custom", rules);
    }
}
