package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single step in the logic flow
 */
public class LogicStep {
    
    @JsonProperty("step")
    private int step;
    
    @JsonProperty("operation")
    private String operation;      // AUTHENTICATE, VALIDATE, QUERY_DATABASE, 
                                   // TRANSFORM_DATA, CALL_SERVICE, RETURN_RESPONSE, etc.
    
    @JsonProperty("description")
    private String description;    // Natural language description
    
    @JsonProperty("startLine")
    private int startLine;
    
    @JsonProperty("endLine")
    private int endLine;
    
    @JsonProperty("variables")
    private List<String> variables = new ArrayList<>(); // Variables involved
    
    @JsonProperty("methods")
    private List<String> methods = new ArrayList<>();   // Methods called
    
    @JsonProperty("outcome")
    private String outcome;        // What this step produces
    
    public LogicStep() {
    }
    
    public LogicStep(int step, String operation, String description) {
        this.step = step;
        this.operation = operation;
        this.description = description;
    }
    
    // Getters and Setters
    public int getStep() {
        return step;
    }
    
    public void setStep(int step) {
        this.step = step;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getStartLine() {
        return startLine;
    }
    
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }
    
    public List<String> getVariables() {
        return variables;
    }
    
    public void setVariables(List<String> variables) {
        this.variables = variables;
    }
    
    public void addVariable(String variable) {
        this.variables.add(variable);
    }
    
    public List<String> getMethods() {
        return methods;
    }
    
    public void setMethods(List<String> methods) {
        this.methods = methods;
    }
    
    public void addMethod(String method) {
        this.methods.add(method);
    }
    
    public String getOutcome() {
        return outcome;
    }
    
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
