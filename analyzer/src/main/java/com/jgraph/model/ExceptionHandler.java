package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Spring @ExceptionHandler or @ControllerAdvice exception handler
 */
public class ExceptionHandler {
    @JsonProperty("className")
    private String className;
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("handledExceptions")
    private List<String> handledExceptions = new ArrayList<>();
    
    @JsonProperty("isGlobal")
    private boolean isGlobal; // true if @ControllerAdvice
    
    @JsonProperty("responseStatus")
    private String responseStatus; // HTTP status code if @ResponseStatus present
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public ExceptionHandler() {}
    
    public ExceptionHandler(String className, String methodName, List<String> handledExceptions, 
                           boolean isGlobal, String responseStatus, String sourceFile, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.handledExceptions = handledExceptions;
        this.isGlobal = isGlobal;
        this.responseStatus = responseStatus;
        this.sourceFile = sourceFile;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
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
    
    public List<String> getHandledExceptions() {
        return handledExceptions;
    }
    
    public void setHandledExceptions(List<String> handledExceptions) {
        this.handledExceptions = handledExceptions;
    }
    
    public boolean isGlobal() {
        return isGlobal;
    }
    
    public void setGlobal(boolean global) {
        isGlobal = global;
    }
    
    public String getResponseStatus() {
        return responseStatus;
    }
    
    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }
    
    public String getSourceFile() {
        return sourceFile;
    }
    
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
