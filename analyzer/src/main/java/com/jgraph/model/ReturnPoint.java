package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a return statement in a method
 */
public class ReturnPoint {
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("returnValue")
    private String returnValue;              // Expression being returned
    
    @JsonProperty("conditional")
    private boolean conditional;             // Inside if/else
    
    @JsonProperty("condition")
    private String condition;                // Condition for this return
    
    // Constructors
    public ReturnPoint() {}
    
    public ReturnPoint(int lineNumber, String returnValue) {
        this.lineNumber = lineNumber;
        this.returnValue = returnValue;
    }
    
    // Getters and Setters
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getReturnValue() {
        return returnValue;
    }
    
    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }
    
    public boolean isConditional() {
        return conditional;
    }
    
    public void setConditional(boolean conditional) {
        this.conditional = conditional;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
