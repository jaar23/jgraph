package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete source representation for a single method
 */
public class MethodSource {
    
    @JsonProperty("methodId")
    private String methodId;
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("complexity")
    private ComplexityMetrics complexity;
    
    @JsonProperty("operations")
    private OperationSummary operations;
    
    // Enhanced code representation
    @JsonProperty("codeBlocks")
    private List<CodeBlock> codeBlocks = new ArrayList<>();
    
    @JsonProperty("logicFlow")
    private LogicFlow logicFlow;
    
    // Variable tracking (only for DETAILED level)
    @JsonProperty("variables")
    private List<VariableLifecycle> variables;
    
    // Natural language
    @JsonProperty("summary")
    private String summary;        // 1-2 sentence summary
    
    @JsonProperty("detailedDescription")
    private String detailedDescription; // Longer description if complex
    
    // Metadata
    @JsonProperty("isTrivial")
    private boolean isTrivial;     // Simple getter/setter
    
    @JsonProperty("isGenerated")
    private boolean isGenerated;   // Generated code (Lombok, etc.)
    
    @JsonProperty("sourceLines")
    private int sourceLines;       // Lines in source
    
    public MethodSource() {
    }
    
    public MethodSource(String methodId, String signature) {
        this.methodId = methodId;
        this.signature = signature;
    }
    
    // Getters and Setters
    public String getMethodId() {
        return methodId;
    }
    
    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
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
    
    public List<CodeBlock> getCodeBlocks() {
        return codeBlocks;
    }
    
    public void setCodeBlocks(List<CodeBlock> codeBlocks) {
        this.codeBlocks = codeBlocks;
    }
    
    public void addCodeBlock(CodeBlock block) {
        this.codeBlocks.add(block);
    }
    
    public LogicFlow getLogicFlow() {
        return logicFlow;
    }
    
    public void setLogicFlow(LogicFlow logicFlow) {
        this.logicFlow = logicFlow;
    }
    
    public List<VariableLifecycle> getVariables() {
        return variables;
    }
    
    public void setVariables(List<VariableLifecycle> variables) {
        this.variables = variables;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getDetailedDescription() {
        return detailedDescription;
    }
    
    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }
    
    public boolean isTrivial() {
        return isTrivial;
    }
    
    public void setTrivial(boolean trivial) {
        isTrivial = trivial;
    }
    
    public boolean isGenerated() {
        return isGenerated;
    }
    
    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }
    
    public int getSourceLines() {
        return sourceLines;
    }
    
    public void setSourceLines(int sourceLines) {
        this.sourceLines = sourceLines;
    }
}
