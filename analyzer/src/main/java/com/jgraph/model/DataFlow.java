package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents data flow through a method - parameters, variables, and returns
 */
public class DataFlow {
    @JsonProperty("methodId")
    private String methodId; // Reference to parent method
    
    @JsonProperty("className")
    private String className;
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("parameters")
    private List<ParameterFlow> parameters = new ArrayList<>();
    
    @JsonProperty("variables")
    private List<VariableTransformation> variables = new ArrayList<>();
    
    @JsonProperty("returns")
    private List<ReturnValue> returns = new ArrayList<>();
    
    @JsonProperty("dataTransfers")
    private List<DataTransfer> dataTransfers = new ArrayList<>();
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    public DataFlow() {}
    
    public DataFlow(String methodId, String className, String methodName, String sourceFile) {
        this.methodId = methodId;
        this.className = className;
        this.methodName = methodName;
        this.sourceFile = sourceFile;
    }
    
    // Getters and Setters
    public String getMethodId() {
        return methodId;
    }
    
    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }
    
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
    
    public List<ParameterFlow> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<ParameterFlow> parameters) {
        this.parameters = parameters;
    }
    
    public List<VariableTransformation> getVariables() {
        return variables;
    }
    
    public void setVariables(List<VariableTransformation> variables) {
        this.variables = variables;
    }
    
    public List<ReturnValue> getReturns() {
        return returns;
    }
    
    public void setReturns(List<ReturnValue> returns) {
        this.returns = returns;
    }
    
    public List<DataTransfer> getDataTransfers() {
        return dataTransfers;
    }
    
    public void setDataTransfers(List<DataTransfer> dataTransfers) {
        this.dataTransfers = dataTransfers;
    }
    
    public String getSourceFile() {
        return sourceFile;
    }
    
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
}
