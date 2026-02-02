package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a dependency injection relationship
 */
public class Dependency {
    
    @JsonProperty("from")
    private String from;
    
    @JsonProperty("to")
    private String to;
    
    @JsonProperty("type")
    private String type; // FIELD_INJECTION, CONSTRUCTOR, SETTER
    
    @JsonProperty("annotation")
    private String annotation;
    
    @JsonProperty("fieldName")
    private String fieldName;
    
    public Dependency() {}
    
    public Dependency(String from, String to, String type, String annotation) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.annotation = annotation;
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
    
    public String getAnnotation() {
        return annotation;
    }
    
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
