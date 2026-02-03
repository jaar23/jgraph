package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a conditional branch (if/else, switch)
 */
public class Branch {
    
    @JsonProperty("type")
    private String type;                     // "if", "else", "switch", "case"
    
    @JsonProperty("condition")
    private String condition;                // Condition expression
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("hasElse")
    private boolean hasElse;
    
    // Constructors
    public Branch() {}
    
    public Branch(String type, String condition, int lineNumber) {
        this.type = type;
        this.condition = condition;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public boolean isHasElse() {
        return hasElse;
    }
    
    public void setHasElse(boolean hasElse) {
        this.hasElse = hasElse;
    }
}
