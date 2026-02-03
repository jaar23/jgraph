package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a return statement in a method
 */
public class ReturnValue {
    @JsonProperty("expression")
    private String expression;
    
    @JsonProperty("returnType")
    private String returnType; // VARIABLE, METHOD_RESULT, NEW_OBJECT, EXPRESSION
    
    @JsonProperty("sourceVariable")
    private String sourceVariable; // If returning a variable
    
    @JsonProperty("sourceMethod")
    private String sourceMethod; // If returning method call result
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public ReturnValue() {}
    
    public ReturnValue(String expression, String returnType, int lineNumber) {
        this.expression = expression;
        this.returnType = returnType;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    public String getSourceVariable() {
        return sourceVariable;
    }
    
    public void setSourceVariable(String sourceVariable) {
        this.sourceVariable = sourceVariable;
    }
    
    public String getSourceMethod() {
        return sourceMethod;
    }
    
    public void setSourceMethod(String sourceMethod) {
        this.sourceMethod = sourceMethod;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
