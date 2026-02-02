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
}
