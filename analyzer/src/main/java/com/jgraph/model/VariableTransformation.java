package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a variable transformation (assignment, method call result, etc.)
 */
public class VariableTransformation {
    @JsonProperty("variableName")
    private String variableName;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("transformationType")
    private String transformationType; // ASSIGNMENT, METHOD_CALL, OBJECT_CREATION, REASSIGNMENT
    
    @JsonProperty("source")
    private String source; // What it was created from
    
    @JsonProperty("transformationMethod")
    private String transformationMethod; // Method or constructor used
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public VariableTransformation() {}
    
    public VariableTransformation(String variableName, String type, String transformationType, 
                                 String source, int lineNumber) {
        this.variableName = variableName;
        this.type = type;
        this.transformationType = transformationType;
        this.source = source;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
    public String getVariableName() {
        return variableName;
    }
    
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTransformationType() {
        return transformationType;
    }
    
    public void setTransformationType(String transformationType) {
        this.transformationType = transformationType;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getTransformationMethod() {
        return transformationMethod;
    }
    
    public void setTransformationMethod(String transformationMethod) {
        this.transformationMethod = transformationMethod;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
