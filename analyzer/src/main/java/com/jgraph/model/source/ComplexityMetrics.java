package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Complexity metrics for a method
 */
public class ComplexityMetrics {
    
    @JsonProperty("cyclomaticComplexity")
    private int cyclomaticComplexity;    // McCabe complexity
    
    @JsonProperty("cognitiveComplexity")
    private int cognitiveComplexity;     // Cognitive complexity
    
    @JsonProperty("linesOfCode")
    private int linesOfCode;             // Physical LOC
    
    @JsonProperty("statementCount")
    private int statementCount;          // Total statements
    
    @JsonProperty("nestingDepth")
    private int nestingDepth;            // Maximum nesting level
    
    @JsonProperty("maintainabilityIndex")
    private double maintainabilityIndex; // 0-100 score
    
    public ComplexityMetrics() {
    }
    
    public ComplexityMetrics(int cyclomaticComplexity, int cognitiveComplexity, 
                            int linesOfCode, int statementCount) {
        this.cyclomaticComplexity = cyclomaticComplexity;
        this.cognitiveComplexity = cognitiveComplexity;
        this.linesOfCode = linesOfCode;
        this.statementCount = statementCount;
    }
    
    // Getters and Setters
    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
    
    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }
    
    public int getCognitiveComplexity() {
        return cognitiveComplexity;
    }
    
    public void setCognitiveComplexity(int cognitiveComplexity) {
        this.cognitiveComplexity = cognitiveComplexity;
    }
    
    public int getLinesOfCode() {
        return linesOfCode;
    }
    
    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }
    
    public int getStatementCount() {
        return statementCount;
    }
    
    public void setStatementCount(int statementCount) {
        this.statementCount = statementCount;
    }
    
    public int getNestingDepth() {
        return nestingDepth;
    }
    
    public void setNestingDepth(int nestingDepth) {
        this.nestingDepth = nestingDepth;
    }
    
    public double getMaintainabilityIndex() {
        return maintainabilityIndex;
    }
    
    public void setMaintainabilityIndex(double maintainabilityIndex) {
        this.maintainabilityIndex = maintainabilityIndex;
    }
}
