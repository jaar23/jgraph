package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single usage of a variable
 */
public class VariableUsage {
    
    @JsonProperty("variableName")
    private String variableName;
    
    @JsonProperty("variableType")
    private String variableType;
    
    @JsonProperty("usageType")
    private String usageType;      // READ, WRITE, READ_WRITE
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("context")
    private String context;        // Where used (in condition, assignment, call, etc.)
    
    @JsonProperty("expression")
    private String expression;     // The expression using the variable
    
    public VariableUsage() {
    }
    
    public VariableUsage(String variableName, String usageType, int lineNumber) {
        this.variableName = variableName;
        this.usageType = usageType;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
    public String getVariableName() {
        return variableName;
    }
    
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
    
    public String getVariableType() {
        return variableType;
    }
    
    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }
    
    public String getUsageType() {
        return usageType;
    }
    
    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
}
