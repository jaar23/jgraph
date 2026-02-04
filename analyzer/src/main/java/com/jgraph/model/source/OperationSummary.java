package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Summary of operations in a method
 */
public class OperationSummary {
    
    @JsonProperty("conditionals")
    private int conditionals;       // if/else/switch count
    
    @JsonProperty("loops")
    private int loops;              // for/while/do-while count
    
    @JsonProperty("methodCalls")
    private int methodCalls;        // Total method invocations
    
    @JsonProperty("assignments")
    private int assignments;        // Variable assignments
    
    @JsonProperty("returns")
    private int returns;            // Return statement count
    
    @JsonProperty("exceptionHandling")
    private int exceptionHandling;  // try/catch/throw count
    
    @JsonProperty("lambdas")
    private int lambdas;            // Lambda expressions count
    
    @JsonProperty("streams")
    private int streams;            // Stream operations count
    
    public OperationSummary() {
    }
    
    // Getters and Setters
    public int getConditionals() {
        return conditionals;
    }
    
    public void setConditionals(int conditionals) {
        this.conditionals = conditionals;
    }
    
    public int getLoops() {
        return loops;
    }
    
    public void setLoops(int loops) {
        this.loops = loops;
    }
    
    public int getMethodCalls() {
        return methodCalls;
    }
    
    public void setMethodCalls(int methodCalls) {
        this.methodCalls = methodCalls;
    }
    
    public int getAssignments() {
        return assignments;
    }
    
    public void setAssignments(int assignments) {
        this.assignments = assignments;
    }
    
    public int getReturns() {
        return returns;
    }
    
    public void setReturns(int returns) {
        this.returns = returns;
    }
    
    public int getExceptionHandling() {
        return exceptionHandling;
    }
    
    public void setExceptionHandling(int exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }
    
    public int getLambdas() {
        return lambdas;
    }
    
    public void setLambdas(int lambdas) {
        this.lambdas = lambdas;
    }
    
    public int getStreams() {
        return streams;
    }
    
    public void setStreams(int streams) {
        this.streams = streams;
    }
    
    public void incrementConditionals() {
        this.conditionals++;
    }
    
    public void incrementLoops() {
        this.loops++;
    }
    
    public void incrementMethodCalls() {
        this.methodCalls++;
    }
    
    public void incrementAssignments() {
        this.assignments++;
    }
    
    public void incrementReturns() {
        this.returns++;
    }
    
    public void incrementExceptionHandling() {
        this.exceptionHandling++;
    }
    
    public void incrementLambdas() {
        this.lambdas++;
    }
    
    public void incrementStreams() {
        this.streams++;
    }
}
