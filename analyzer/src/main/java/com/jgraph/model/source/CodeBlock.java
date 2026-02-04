package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a logical code block
 */
public class CodeBlock {
    
    @JsonProperty("type")
    private String type;           // DECLARATION, ASSIGNMENT, CONDITIONAL, LOOP, 
                                   // METHOD_CALL, RETURN, TRY_CATCH, LAMBDA, STREAM
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("code")
    private String code;           // Simplified code representation
    
    @JsonProperty("description")
    private String description;    // Natural language description
    
    // For conditionals
    @JsonProperty("condition")
    private String condition;
    
    @JsonProperty("thenBlocks")
    private List<CodeBlock> thenBlocks;
    
    @JsonProperty("elseBlocks")
    private List<CodeBlock> elseBlocks;
    
    // For loops
    @JsonProperty("loopType")
    private String loopType;       // for, while, do-while, forEach
    
    @JsonProperty("iterator")
    private String iterator;
    
    @JsonProperty("loopBody")
    private List<CodeBlock> loopBody;
    
    // For method calls
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("targetClass")
    private String targetClass;
    
    @JsonProperty("arguments")
    private List<String> arguments;
    
    @JsonProperty("returnVariable")
    private String returnVariable;
    
    // For lambdas/streams
    @JsonProperty("streamOperation")
    private String streamOperation;  // map, filter, collect, etc.
    
    @JsonProperty("lambdaExpression")
    private String lambdaExpression;
    
    // Variable tracking
    @JsonProperty("variablesUsed")
    private List<VariableUsage> variablesUsed;
    
    @JsonProperty("variablesModified")
    private List<VariableUsage> variablesModified;
    
    public CodeBlock() {
    }
    
    public CodeBlock(String type, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
    }
    
    public CodeBlock(String type, int lineNumber, String code) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.code = code;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public List<CodeBlock> getThenBlocks() {
        return thenBlocks;
    }
    
    public void setThenBlocks(List<CodeBlock> thenBlocks) {
        this.thenBlocks = thenBlocks;
    }
    
    public void addThenBlock(CodeBlock block) {
        if (this.thenBlocks == null) {
            this.thenBlocks = new ArrayList<>();
        }
        this.thenBlocks.add(block);
    }
    
    public List<CodeBlock> getElseBlocks() {
        return elseBlocks;
    }
    
    public void setElseBlocks(List<CodeBlock> elseBlocks) {
        this.elseBlocks = elseBlocks;
    }
    
    public void addElseBlock(CodeBlock block) {
        if (this.elseBlocks == null) {
            this.elseBlocks = new ArrayList<>();
        }
        this.elseBlocks.add(block);
    }
    
    public String getLoopType() {
        return loopType;
    }
    
    public void setLoopType(String loopType) {
        this.loopType = loopType;
    }
    
    public String getIterator() {
        return iterator;
    }
    
    public void setIterator(String iterator) {
        this.iterator = iterator;
    }
    
    public List<CodeBlock> getLoopBody() {
        return loopBody;
    }
    
    public void setLoopBody(List<CodeBlock> loopBody) {
        this.loopBody = loopBody;
    }
    
    public void addLoopBodyBlock(CodeBlock block) {
        if (this.loopBody == null) {
            this.loopBody = new ArrayList<>();
        }
        this.loopBody.add(block);
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getTargetClass() {
        return targetClass;
    }
    
    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }
    
    public List<String> getArguments() {
        return arguments;
    }
    
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
    
    public String getReturnVariable() {
        return returnVariable;
    }
    
    public void setReturnVariable(String returnVariable) {
        this.returnVariable = returnVariable;
    }
    
    public String getStreamOperation() {
        return streamOperation;
    }
    
    public void setStreamOperation(String streamOperation) {
        this.streamOperation = streamOperation;
    }
    
    public String getLambdaExpression() {
        return lambdaExpression;
    }
    
    public void setLambdaExpression(String lambdaExpression) {
        this.lambdaExpression = lambdaExpression;
    }
    
    public List<VariableUsage> getVariablesUsed() {
        return variablesUsed;
    }
    
    public void setVariablesUsed(List<VariableUsage> variablesUsed) {
        this.variablesUsed = variablesUsed;
    }
    
    public void addVariableUsed(VariableUsage usage) {
        if (this.variablesUsed == null) {
            this.variablesUsed = new ArrayList<>();
        }
        this.variablesUsed.add(usage);
    }
    
    public List<VariableUsage> getVariablesModified() {
        return variablesModified;
    }
    
    public void setVariablesModified(List<VariableUsage> variablesModified) {
        this.variablesModified = variablesModified;
    }
    
    public void addVariableModified(VariableUsage usage) {
        if (this.variablesModified == null) {
            this.variablesModified = new ArrayList<>();
        }
        this.variablesModified.add(usage);
    }
}
