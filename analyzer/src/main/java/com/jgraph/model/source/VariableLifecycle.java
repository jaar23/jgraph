package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete lifecycle tracking for a variable
 */
public class VariableLifecycle {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("declarationLine")
    private int declarationLine;
    
    @JsonProperty("initialValue")
    private String initialValue;
    
    @JsonProperty("usages")
    private List<VariableUsage> usages = new ArrayList<>();
    
    @JsonProperty("lastUsedLine")
    private int lastUsedLine;
    
    @JsonProperty("scope")
    private String scope;          // method, if-block, loop, etc.
    
    @JsonProperty("isParameter")
    private boolean isParameter;
    
    @JsonProperty("isMutated")
    private boolean isMutated;     // Changed after initialization
    
    public VariableLifecycle() {
    }
    
    public VariableLifecycle(String name, String type, int declarationLine) {
        this.name = name;
        this.type = type;
        this.declarationLine = declarationLine;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getDeclarationLine() {
        return declarationLine;
    }
    
    public void setDeclarationLine(int declarationLine) {
        this.declarationLine = declarationLine;
    }
    
    public String getInitialValue() {
        return initialValue;
    }
    
    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }
    
    public List<VariableUsage> getUsages() {
        return usages;
    }
    
    public void setUsages(List<VariableUsage> usages) {
        this.usages = usages;
    }
    
    public void addUsage(VariableUsage usage) {
        this.usages.add(usage);
        this.lastUsedLine = Math.max(this.lastUsedLine, usage.getLineNumber());
    }
    
    public int getLastUsedLine() {
        return lastUsedLine;
    }
    
    public void setLastUsedLine(int lastUsedLine) {
        this.lastUsedLine = lastUsedLine;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public boolean isParameter() {
        return isParameter;
    }
    
    public void setParameter(boolean parameter) {
        isParameter = parameter;
    }
    
    public boolean isMutated() {
        return isMutated;
    }
    
    public void setMutated(boolean mutated) {
        isMutated = mutated;
    }
}
