package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a try-catch block with exception handling details
 */
public class TryCatchBlock {
    @JsonProperty("parentClassName")
    private String parentClassName;
    
    @JsonProperty("parentMethodName")
    private String parentMethodName;
    
    @JsonProperty("caughtExceptions")
    private List<String> caughtExceptions = new ArrayList<>();
    
    @JsonProperty("hasFinally")
    private boolean hasFinally;
    
    @JsonProperty("rethrows")
    private boolean rethrows; // true if exception is rethrown
    
    @JsonProperty("wrapsException")
    private String wrapsException; // if wrapping in another exception type
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public TryCatchBlock() {}
    
    public TryCatchBlock(String parentClassName, String parentMethodName, List<String> caughtExceptions,
                         boolean hasFinally, boolean rethrows, String wrapsException, 
                         String sourceFile, int lineNumber) {
        this.parentClassName = parentClassName;
        this.parentMethodName = parentMethodName;
        this.caughtExceptions = caughtExceptions;
        this.hasFinally = hasFinally;
        this.rethrows = rethrows;
        this.wrapsException = wrapsException;
        this.sourceFile = sourceFile;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
    public String getParentClassName() {
        return parentClassName;
    }
    
    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }
    
    public String getParentMethodName() {
        return parentMethodName;
    }
    
    public void setParentMethodName(String parentMethodName) {
        this.parentMethodName = parentMethodName;
    }
    
    public List<String> getCaughtExceptions() {
        return caughtExceptions;
    }
    
    public void setCaughtExceptions(List<String> caughtExceptions) {
        this.caughtExceptions = caughtExceptions;
    }
    
    public boolean isHasFinally() {
        return hasFinally;
    }
    
    public void setHasFinally(boolean hasFinally) {
        this.hasFinally = hasFinally;
    }
    
    public boolean isRethrows() {
        return rethrows;
    }
    
    public void setRethrows(boolean rethrows) {
        this.rethrows = rethrows;
    }
    
    public String getWrapsException() {
        return wrapsException;
    }
    
    public void setWrapsException(String wrapsException) {
        this.wrapsException = wrapsException;
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
