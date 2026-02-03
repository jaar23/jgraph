package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics about the analyzed project
 */
public class Statistics {
    
    @JsonProperty("totalEndpoints")
    private int totalEndpoints;
    
    @JsonProperty("totalServices")
    private int totalServices;
    
    @JsonProperty("totalRepositories")
    private int totalRepositories;
    
    @JsonProperty("circularDependencies")
    private int circularDependencies;
    
    @JsonProperty("maxCallDepth")
    private int maxCallDepth;
    
    @JsonProperty("totalLogStatements")
    private int totalLogStatements;
    
    @JsonProperty("logsByLevel")
    private java.util.Map<String, Integer> logsByLevel = new java.util.HashMap<>();
    
    @JsonProperty("totalExceptionHandlers")
    private int totalExceptionHandlers;
    
    @JsonProperty("totalTryCatchBlocks")
    private int totalTryCatchBlocks;
    
    @JsonProperty("totalDatabaseOperations")
    private int totalDatabaseOperations;
    
    @JsonProperty("totalTransactions")
    private int totalTransactions;
    
    @JsonProperty("totalExternalCalls")
    private int totalExternalCalls;
    
    @JsonProperty("externalCallsByType")
    private java.util.Map<String, Integer> externalCallsByType = new java.util.HashMap<>();
    
    @JsonProperty("totalDataFlows")
    private int totalDataFlows;
    
    public Statistics() {}
    
    // Getters and Setters
    public int getTotalEndpoints() {
        return totalEndpoints;
    }
    
    public void setTotalEndpoints(int totalEndpoints) {
        this.totalEndpoints = totalEndpoints;
    }
    
    public int getTotalServices() {
        return totalServices;
    }
    
    public void setTotalServices(int totalServices) {
        this.totalServices = totalServices;
    }
    
    public int getTotalRepositories() {
        return totalRepositories;
    }
    
    public void setTotalRepositories(int totalRepositories) {
        this.totalRepositories = totalRepositories;
    }
    
    public int getCircularDependencies() {
        return circularDependencies;
    }
    
    public void setCircularDependencies(int circularDependencies) {
        this.circularDependencies = circularDependencies;
    }
    
    public int getMaxCallDepth() {
        return maxCallDepth;
    }
    
    public void setMaxCallDepth(int maxCallDepth) {
        this.maxCallDepth = maxCallDepth;
    }
    
    public int getTotalLogStatements() {
        return totalLogStatements;
    }
    
    public void setTotalLogStatements(int totalLogStatements) {
        this.totalLogStatements = totalLogStatements;
    }
    
    public java.util.Map<String, Integer> getLogsByLevel() {
        return logsByLevel;
    }
    
    public void setLogsByLevel(java.util.Map<String, Integer> logsByLevel) {
        this.logsByLevel = logsByLevel;
    }
    
    public int getTotalExceptionHandlers() {
        return totalExceptionHandlers;
    }
    
    public void setTotalExceptionHandlers(int totalExceptionHandlers) {
        this.totalExceptionHandlers = totalExceptionHandlers;
    }
    
    public int getTotalTryCatchBlocks() {
        return totalTryCatchBlocks;
    }
    
    public void setTotalTryCatchBlocks(int totalTryCatchBlocks) {
        this.totalTryCatchBlocks = totalTryCatchBlocks;
    }
    
    public int getTotalDatabaseOperations() {
        return totalDatabaseOperations;
    }
    
    public void setTotalDatabaseOperations(int totalDatabaseOperations) {
        this.totalDatabaseOperations = totalDatabaseOperations;
    }
    
    public int getTotalTransactions() {
        return totalTransactions;
    }
    
    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
    
    public int getTotalExternalCalls() {
        return totalExternalCalls;
    }
    
    public void setTotalExternalCalls(int totalExternalCalls) {
        this.totalExternalCalls = totalExternalCalls;
    }
    
    public java.util.Map<String, Integer> getExternalCallsByType() {
        return externalCallsByType;
    }
    
    public void setExternalCallsByType(java.util.Map<String, Integer> externalCallsByType) {
        this.externalCallsByType = externalCallsByType;
    }
    
    public int getTotalDataFlows() {
        return totalDataFlows;
    }
    
    public void setTotalDataFlows(int totalDataFlows) {
        this.totalDataFlows = totalDataFlows;
    }
}
