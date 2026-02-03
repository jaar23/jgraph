package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a database operation (query, insert, update, delete)
 */
public class DatabaseOperation {
    @JsonProperty("type")
    private String type; // "JPA_QUERY", "JDBC", "DERIVED_QUERY", "REPOSITORY_METHOD"
    
    @JsonProperty("operationType")
    private String operationType; // "SELECT", "INSERT", "UPDATE", "DELETE", "UNKNOWN"
    
    @JsonProperty("query")
    private String query; // SQL query if available
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("className")
    private String className;
    
    @JsonProperty("repositoryInterface")
    private String repositoryInterface; // for JPA repositories
    
    @JsonProperty("entityType")
    private String entityType; // entity being operated on
    
    @JsonProperty("isNativeQuery")
    private boolean isNativeQuery;
    
    @JsonProperty("parameters")
    private List<String> parameters = new ArrayList<>();
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public DatabaseOperation() {}
    
    public DatabaseOperation(String type, String operationType, String query, String methodName, 
                            String className, String repositoryInterface, String entityType,
                            boolean isNativeQuery, List<String> parameters, 
                            String sourceFile, int lineNumber) {
        this.type = type;
        this.operationType = operationType;
        this.query = query;
        this.methodName = methodName;
        this.className = className;
        this.repositoryInterface = repositoryInterface;
        this.entityType = entityType;
        this.isNativeQuery = isNativeQuery;
        this.parameters = parameters;
        this.sourceFile = sourceFile;
        this.lineNumber = lineNumber;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getRepositoryInterface() {
        return repositoryInterface;
    }
    
    public void setRepositoryInterface(String repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public boolean isNativeQuery() {
        return isNativeQuery;
    }
    
    public void setNativeQuery(boolean nativeQuery) {
        isNativeQuery = nativeQuery;
    }
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
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
