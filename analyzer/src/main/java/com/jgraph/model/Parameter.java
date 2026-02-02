package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a method parameter
 */
public class Parameter {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("required")
    private boolean required;
    
    @JsonProperty("annotation")
    private String annotation;
    
    public Parameter() {}
    
    public Parameter(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    public Parameter(String name, String type, boolean required, String annotation) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.annotation = annotation;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public String getAnnotation() {
        return annotation;
    }
    
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}
