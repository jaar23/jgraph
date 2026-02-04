package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    
    @JsonProperty("isMultiModule")
    private boolean isMultiModule = false;
    
    @JsonProperty("buildSystem")
    private String buildSystem;
    
    @JsonProperty("modules")
    private List<ModuleInfo> modules = new ArrayList<>();
    
    @JsonProperty("totalModules")
    private int totalModules;
    
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
    
    public boolean isMultiModule() {
        return isMultiModule;
    }
    
    public void setMultiModule(boolean multiModule) {
        isMultiModule = multiModule;
    }
    
    public String getBuildSystem() {
        return buildSystem;
    }
    
    public void setBuildSystem(String buildSystem) {
        this.buildSystem = buildSystem;
    }
    
    public List<ModuleInfo> getModules() {
        return modules;
    }
    
    public void setModules(List<ModuleInfo> modules) {
        this.modules = modules;
        this.totalModules = modules.size();
        this.isMultiModule = modules.size() > 1;
    }
    
    public int getTotalModules() {
        return totalModules;
    }
    
    public void setTotalModules(int totalModules) {
        this.totalModules = totalModules;
    }
    
    public void addModule(ModuleInfo module) {
        this.modules.add(module);
        this.totalModules = this.modules.size();
        this.isMultiModule = this.modules.size() > 1;
    }
}
