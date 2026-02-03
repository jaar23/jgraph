package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a node in the call graph
 */
public class GraphNode {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type; // endpoint, service, repository, log
    
    @JsonProperty("label")
    private String label;
    
    @JsonProperty("className")
    private String className;
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("logLevel")
    private String logLevel;
    
    @JsonProperty("logMessage")
    private String logMessage;
    
    public GraphNode() {}
    
    public GraphNode(String id, String type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    public String getLogMessage() {
        return logMessage;
    }
    
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
}
