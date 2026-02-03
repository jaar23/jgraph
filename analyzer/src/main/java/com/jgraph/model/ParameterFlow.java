package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method parameter and how it flows through the code
 */
public class ParameterFlow {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("annotations")
    private List<String> annotations = new ArrayList<>();
    
    @JsonProperty("isInputData")
    private boolean isInputData; // true for @RequestBody, @RequestParam, etc.
    
    @JsonProperty("usedIn")
    private List<String> usedIn = new ArrayList<>(); // Variable names or method calls where this parameter is used
    
    public ParameterFlow() {}
    
    public ParameterFlow(String name, String type, boolean isInputData) {
        this.name = name;
        this.type = type;
        this.isInputData = isInputData;
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
    
    public List<String> getAnnotations() {
        return annotations;
    }
    
    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
    
    public boolean isInputData() {
        return isInputData;
    }
    
    public void setInputData(boolean inputData) {
        isInputData = inputData;
    }
    
    public List<String> getUsedIn() {
        return usedIn;
    }
    
    public void setUsedIn(List<String> usedIn) {
        this.usedIn = usedIn;
    }
}
