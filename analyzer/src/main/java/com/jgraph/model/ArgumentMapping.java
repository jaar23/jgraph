package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps an argument passed to a method call
 */
public class ArgumentMapping {
    @JsonProperty("position")
    private int position; // 0-based position
    
    @JsonProperty("sourceType")
    private String sourceType; // VARIABLE, METHOD_RESULT, FIELD_ACCESS, LITERAL, PARAMETER
    
    @JsonProperty("sourceName")
    private String sourceName; // Name of variable, field, or parameter
    
    @JsonProperty("expression")
    private String expression; // Full expression passed as argument
    
    public ArgumentMapping() {}
    
    public ArgumentMapping(int position, String sourceType, String sourceName, String expression) {
        this.position = position;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.expression = expression;
    }
    
    // Getters and Setters
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
}
