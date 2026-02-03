package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a logging statement in the code
 */
public class LogStatement {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("level")
    private LogLevel level;                  // DEBUG, INFO, WARN, ERROR, TRACE
    
    @JsonProperty("message")
    private String message;                  // Log message template
    
    @JsonProperty("variables")
    private List<String> variables = new ArrayList<>();  // Variables logged
    
    @JsonProperty("methodId")
    private String methodId;                 // Method containing this log
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("filePath")
    private String filePath;
    
    @JsonProperty("hasException")
    private boolean hasException;            // If logging an exception
    
    @JsonProperty("exceptionVariable")
    private String exceptionVariable;        // Exception variable name
    
    @JsonProperty("mdcKeys")
    private List<String> mdcKeys = new ArrayList<>();    // MDC context keys
    
    @JsonProperty("conditionalContext")
    private String conditionalContext;       // If inside if/try block
    
    @JsonProperty("loggerFramework")
    private String loggerFramework;          // SLF4J, Log4j2, JUL, etc.
    
    // Constructors
    public LogStatement() {}
    
    public LogStatement(String id) {
        this.id = id;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LogLevel getLevel() {
        return level;
    }
    
    public void setLevel(LogLevel level) {
        this.level = level;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<String> getVariables() {
        return variables;
    }
    
    public void setVariables(List<String> variables) {
        this.variables = variables;
    }
    
    public String getMethodId() {
        return methodId;
    }
    
    public void setMethodId(String methodId) {
        this.methodId = methodId;
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
    
    public boolean isHasException() {
        return hasException;
    }
    
    public void setHasException(boolean hasException) {
        this.hasException = hasException;
    }
    
    public String getExceptionVariable() {
        return exceptionVariable;
    }
    
    public void setExceptionVariable(String exceptionVariable) {
        this.exceptionVariable = exceptionVariable;
    }
    
    public List<String> getMdcKeys() {
        return mdcKeys;
    }
    
    public void setMdcKeys(List<String> mdcKeys) {
        this.mdcKeys = mdcKeys;
    }
    
    public String getConditionalContext() {
        return conditionalContext;
    }
    
    public void setConditionalContext(String conditionalContext) {
        this.conditionalContext = conditionalContext;
    }
    
    public String getLoggerFramework() {
        return loggerFramework;
    }
    
    public void setLoggerFramework(String loggerFramework) {
        this.loggerFramework = loggerFramework;
    }
}
