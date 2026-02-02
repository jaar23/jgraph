package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Project metadata
 */
public class ProjectInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("analyzedAt")
    private String analyzedAt;
    
    @JsonProperty("totalFiles")
    private int totalFiles;
    
    @JsonProperty("javaVersion")
    private String javaVersion;
    
    public ProjectInfo() {
        this.analyzedAt = Instant.now().toString();
    }
    
    public ProjectInfo(String name, String path) {
        this.name = name;
        this.path = path;
        this.analyzedAt = Instant.now().toString();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getAnalyzedAt() {
        return analyzedAt;
    }
    
    public void setAnalyzedAt(String analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
    
    public int getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }
    
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
}
