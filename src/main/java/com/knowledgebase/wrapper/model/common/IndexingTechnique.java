package com.knowledgebase.wrapper.model.common;

/**
 * Enum for indexing techniques.
 */
public enum IndexingTechnique {
    HIGH_QUALITY("high_quality"),
    ECONOMY("economy");
    
    private final String value;
    
    IndexingTechnique(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
