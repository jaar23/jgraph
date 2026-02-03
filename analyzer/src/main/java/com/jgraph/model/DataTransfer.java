package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents data being transferred from one method to another
 */
public class DataTransfer {
    @JsonProperty("fromMethodId")
    private String fromMethodId;
    
    @JsonProperty("toMethodId")
    private String toMethodId;
    
    @JsonProperty("targetMethod")
    private String targetMethod; // Name of the called method
    
    @JsonProperty("arguments")
    private List<ArgumentMapping> arguments = new ArrayList<>();
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public DataTransfer() {}
    
    public DataTransfer(String fromMethodId, String targetMethod, int lineNumber) {
        this.fromMethodId = fromMethodId;
        this.targetMethod = targetMethod;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
    public String getFromMethodId() {
        return fromMethodId;
    }
    
    public void setFromMethodId(String fromMethodId) {
        this.fromMethodId = fromMethodId;
    }
    
    public String getToMethodId() {
        return toMethodId;
    }
    
    public void setToMethodId(String toMethodId) {
        this.toMethodId = toMethodId;
    }
    
    public String getTargetMethod() {
        return targetMethod;
    }
    
    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }
    
    public List<ArgumentMapping> getArguments() {
        return arguments;
    }
    
    public void setArguments(List<ArgumentMapping> arguments) {
        this.arguments = arguments;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
