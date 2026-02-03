package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a resolved method call with type information
 */
public class ResolvedMethodCall {
    
    @JsonProperty("targetClass")
    private String targetClass;              // Fully qualified class name
    
    @JsonProperty("targetMethod")
    private String targetMethod;             // Method name
    
    @JsonProperty("targetType")
    private String targetType;               // "interface", "class", "abstract"
    
    @JsonProperty("arguments")
    private List<String> arguments = new ArrayList<>();  // Passed parameters
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("scope")
    private String scope;                    // Variable/field name (e.g., "userService")
    
    // Constructors
    public ResolvedMethodCall() {}
    
    public ResolvedMethodCall(String targetClass, String targetMethod) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }
    
    // Getters and Setters
    public String getTargetClass() {
        return targetClass;
    }
    
    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }
    
    public String getTargetMethod() {
        return targetMethod;
    }
    
    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public List<String> getArguments() {
        return arguments;
    }
    
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
}
