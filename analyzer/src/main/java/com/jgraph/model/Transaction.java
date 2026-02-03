package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a transactional method marked with @Transactional
 */
public class Transaction {
    @JsonProperty("className")
    private String className;
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("propagation")
    private String propagation; // REQUIRED, REQUIRES_NEW, etc.
    
    @JsonProperty("isolation")
    private String isolation; // DEFAULT, READ_COMMITTED, etc.
    
    @JsonProperty("readOnly")
    private boolean readOnly;
    
    @JsonProperty("timeout")
    private int timeout; // in seconds, -1 if not set
    
    @JsonProperty("rollbackFor")
    private String rollbackFor; // exceptions that trigger rollback
    
    @JsonProperty("noRollbackFor")
    private String noRollbackFor; // exceptions that don't trigger rollback
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public Transaction() {}
    
    public Transaction(String className, String methodName, String propagation, String isolation,
                      boolean readOnly, int timeout, String rollbackFor, String noRollbackFor,
                      String sourceFile, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.propagation = propagation;
        this.isolation = isolation;
        this.readOnly = readOnly;
        this.timeout = timeout;
        this.rollbackFor = rollbackFor;
        this.noRollbackFor = noRollbackFor;
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
    
    public String getPropagation() {
        return propagation;
    }
    
    public void setPropagation(String propagation) {
        this.propagation = propagation;
    }
    
    public String getIsolation() {
        return isolation;
    }
    
    public void setIsolation(String isolation) {
        this.isolation = isolation;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public String getRollbackFor() {
        return rollbackFor;
    }
    
    public void setRollbackFor(String rollbackFor) {
        this.rollbackFor = rollbackFor;
    }
    
    public String getNoRollbackFor() {
        return noRollbackFor;
    }
    
    public void setNoRollbackFor(String noRollbackFor) {
        this.noRollbackFor = noRollbackFor;
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
