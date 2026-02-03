package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents control flow information within a method
 */
public class ControlFlow {
    
    @JsonProperty("branches")
    private List<Branch> branches = new ArrayList<>();
    
    @JsonProperty("returnPoints")
    private List<ReturnPoint> returnPoints = new ArrayList<>();
    
    @JsonProperty("hasEarlyReturns")
    private boolean hasEarlyReturns;
    
    @JsonProperty("hasTryCatch")
    private boolean hasTryCatch;
    
    @JsonProperty("hasLoops")
    private boolean hasLoops;
    
    // Constructors
    public ControlFlow() {}
    
    // Getters and Setters
    public List<Branch> getBranches() {
        return branches;
    }
    
    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }
    
    public List<ReturnPoint> getReturnPoints() {
        return returnPoints;
    }
    
    public void setReturnPoints(List<ReturnPoint> returnPoints) {
        this.returnPoints = returnPoints;
    }
    
    public boolean isHasEarlyReturns() {
        return hasEarlyReturns;
    }
    
    public void setHasEarlyReturns(boolean hasEarlyReturns) {
        this.hasEarlyReturns = hasEarlyReturns;
    }
    
    public boolean isHasTryCatch() {
        return hasTryCatch;
    }
    
    public void setHasTryCatch(boolean hasTryCatch) {
        this.hasTryCatch = hasTryCatch;
    }
    
    public boolean isHasLoops() {
        return hasLoops;
    }
    
    public void setHasLoops(boolean hasLoops) {
        this.hasLoops = hasLoops;
    }
}
