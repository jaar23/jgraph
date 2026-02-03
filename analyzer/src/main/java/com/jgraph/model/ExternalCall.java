package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an external service call (REST, messaging, cache, etc.)
 */
public class ExternalCall {
    @JsonProperty("type")
    private String type; // "REST", "KAFKA", "RABBITMQ", "REDIS", "HTTP_CLIENT"
    
    @JsonProperty("method")
    private String method; // HTTP method for REST calls or operation type
    
    @JsonProperty("endpoint")
    private String endpoint; // URL or topic/queue name
    
    @JsonProperty("callingMethod")
    private String callingMethod;
    
    @JsonProperty("callingClass")
    private String callingClass;
    
    @JsonProperty("clientType")
    private String clientType; // "RestTemplate", "WebClient", "FeignClient", etc.
    
    @JsonProperty("isAsync")
    private boolean isAsync; // true for reactive/async calls
    
    @JsonProperty("requestBody")
    private String requestBody; // type of request body
    
    @JsonProperty("responseType")
    private String responseType; // expected response type
    
    @JsonProperty("parameters")
    private List<String> parameters = new ArrayList<>();
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    public ExternalCall() {}
    
    public ExternalCall(String type, String method, String endpoint, String callingMethod,
                       String callingClass, String clientType, boolean isAsync,
                       String requestBody, String responseType, List<String> parameters,
                       String sourceFile, int lineNumber) {
        this.type = type;
        this.method = method;
        this.endpoint = endpoint;
        this.callingMethod = callingMethod;
        this.callingClass = callingClass;
        this.clientType = clientType;
        this.isAsync = isAsync;
        this.requestBody = requestBody;
        this.responseType = responseType;
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
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getCallingMethod() {
        return callingMethod;
    }
    
    public void setCallingMethod(String callingMethod) {
        this.callingMethod = callingMethod;
    }
    
    public String getCallingClass() {
        return callingClass;
    }
    
    public void setCallingClass(String callingClass) {
        this.callingClass = callingClass;
    }
    
    public String getClientType() {
        return clientType;
    }
    
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
    
    public boolean isAsync() {
        return isAsync;
    }
    
    public void setAsync(boolean async) {
        isAsync = async;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
    
    public String getResponseType() {
        return responseType;
    }
    
    public void setResponseType(String responseType) {
        this.responseType = responseType;
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
