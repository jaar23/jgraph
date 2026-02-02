package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an edge in the call graph
 */
public class GraphEdge {
    
    @JsonProperty("from")
    private String from;
    
    @JsonProperty("to")
    private String to;
    
    @JsonProperty("type")
    private String type; // method_call, dependency_injection
    
    @JsonProperty("label")
    private String label;
    
    public GraphEdge() {}
    
    public GraphEdge(String from, String to, String type, String label) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.label = label;
    }
    
    // Getters and Setters
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
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
}
