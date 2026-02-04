package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jgraph.model.source.ComplexityMetrics;
import com.jgraph.model.source.OperationSummary;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a REST/Web endpoint (Controller method)
 */
public class Endpoint {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type; // REST, WebSocket, GraphQL
    
    @JsonProperty("httpMethod")
    private String httpMethod; // GET, POST, PUT, DELETE, PATCH
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("controllerClass")
    private String controllerClass;
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("parameters")
    private List<Parameter> parameters = new ArrayList<>();
    
    @JsonProperty("returnType")
    private String returnType;
    
    @JsonProperty("annotations")
    private List<String> annotations = new ArrayList<>();
    
    @JsonProperty("javadoc")
    private String javadoc;
    
    @JsonProperty("callChain")
    private List<String> callChain = new ArrayList<>();
    
    @JsonProperty("resolvedCalls")
    private List<ResolvedMethodCall> resolvedCalls = new ArrayList<>();
    
    @JsonProperty("controlFlow")
    private ControlFlow controlFlow;
    
    @JsonProperty("fullyQualifiedType")
    private String fullyQualifiedType;
    
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
    public Endpoint() {}
    
    public Endpoint(String id) {
        this.id = id;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getControllerClass() {
        return controllerClass;
    }
    
    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public List<Parameter> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<Parameter> parameters) {
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
    
    public String getJavadoc() {
        return javadoc;
    }
    
    public void setJavadoc(String javadoc) {
        this.javadoc = javadoc;
    }
    
    public List<String> getCallChain() {
        return callChain;
    }
    
    public void setCallChain(List<String> callChain) {
        this.callChain = callChain;
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
