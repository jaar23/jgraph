package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jgraph.model.source.ComplexityMetrics;
import com.jgraph.model.source.OperationSummary;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a service layer method
 */
public class ServiceMethod {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("className")
    private String className;
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("parameters")
    private List<String> parameters = new ArrayList<>();
    
    @JsonProperty("returnType")
    private String returnType;
    
    @JsonProperty("annotations")
    private List<String> annotations = new ArrayList<>();
    
    @JsonProperty("dependencies")
    private List<String> dependencies = new ArrayList<>();
    
    @JsonProperty("calledBy")
    private List<String> calledBy = new ArrayList<>();
    
    @JsonProperty("calls")
    private List<String> calls = new ArrayList<>();
    
    @JsonProperty("resolvedCalls")
    private List<ResolvedMethodCall> resolvedCalls = new ArrayList<>();
    
    @JsonProperty("controlFlow")
    private ControlFlow controlFlow;
    
    @JsonProperty("fullyQualifiedType")
    private String fullyQualifiedType;
    
    @JsonProperty("javadoc")
    private String javadoc;
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("filePath")
    private String filePath;
    
    // Source code enhancements
    @JsonProperty("sourceMapRef")
    private String sourceMapRef;  // Reference to source-map.json entry
    
    @JsonProperty("complexity")
    private ComplexityMetrics complexity;
    
    @JsonProperty("operations")
    private OperationSummary operations;
    
    @JsonProperty("summary")
    private String summary;  // Natural language summary
    
    // Constructors
    public ServiceMethod() {}
    
    public ServiceMethod(String id) {
        this.id = id;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    public List<String> getAnnotations() {
        return annotations;
    }
    
    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public List<String> getCalledBy() {
        return calledBy;
    }
    
    public void setCalledBy(List<String> calledBy) {
        this.calledBy = calledBy;
    }
    
    public List<String> getCalls() {
        return calls;
    }
    
    public void setCalls(List<String> calls) {
        this.calls = calls;
    }
    
    public String getJavadoc() {
        return javadoc;
    }
    
    public void setJavadoc(String javadoc) {
        this.javadoc = javadoc;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public List<ResolvedMethodCall> getResolvedCalls() {
        return resolvedCalls;
    }
    
    public void setResolvedCalls(List<ResolvedMethodCall> resolvedCalls) {
        this.resolvedCalls = resolvedCalls;
    }
    
    public ControlFlow getControlFlow() {
        return controlFlow;
    }
    
    public void setControlFlow(ControlFlow controlFlow) {
        this.controlFlow = controlFlow;
    }
    
    public String getFullyQualifiedType() {
        return fullyQualifiedType;
    }
    
    public void setFullyQualifiedType(String fullyQualifiedType) {
        this.fullyQualifiedType = fullyQualifiedType;
    }
    
    public String getSourceMapRef() {
        return sourceMapRef;
    }
    
    public void setSourceMapRef(String sourceMapRef) {
        this.sourceMapRef = sourceMapRef;
    }
    
    public ComplexityMetrics getComplexity() {
        return complexity;
    }
    
    public void setComplexity(ComplexityMetrics complexity) {
        this.complexity = complexity;
    }
    
    public OperationSummary getOperations() {
        return operations;
    }
    
    public void setOperations(OperationSummary operations) {
        this.operations = operations;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
}
