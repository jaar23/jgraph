package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level logic flow of a method
 */
public class LogicFlow {
    
    @JsonProperty("steps")
    private List<LogicStep> steps = new ArrayList<>();
    
    @JsonProperty("summary")
    private String summary;        // Natural language summary
    
    public LogicFlow() {
    }
    
    public LogicFlow(String summary) {
        this.summary = summary;
    }
    
    // Getters and Setters
    public List<LogicStep> getSteps() {
        return steps;
    }
    
    public void setSteps(List<LogicStep> steps) {
        this.steps = steps;
    }
    
    public void addStep(LogicStep step) {
        this.steps.add(step);
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
}
