package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a module or subproject within a multi-module project
 */
public class ModuleInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("relativePath")
    private String relativePath;
    
    @JsonProperty("type")
    private ModuleType type;
    
    @JsonProperty("buildSystem")
    private BuildSystem buildSystem;
    
    @JsonProperty("groupId")
    private String groupId;
    
    @JsonProperty("artifactId")
    private String artifactId;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("packaging")
    private String packaging;
    
    @JsonProperty("parent")
    private String parent;
    
    @JsonProperty("dependencies")
    private List<String> dependencies = new ArrayList<>();
    
    @JsonProperty("totalFiles")
    private int totalFiles;
    
    @JsonProperty("sourceDirectories")
    private List<String> sourceDirectories = new ArrayList<>();
    
    @JsonProperty("javaVersion")
    private String javaVersion;
    
    public ModuleInfo() {
    }
    
    public ModuleInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }
    
    public ModuleInfo(String name, String path, String relativePath) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
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
    
    public String getRelativePath() {
        return relativePath;
    }
    
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
    
    public ModuleType getType() {
        return type;
    }
    
    public void setType(ModuleType type) {
        this.type = type;
    }
    
    public BuildSystem getBuildSystem() {
        return buildSystem;
    }
    
    public void setBuildSystem(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getPackaging() {
        return packaging;
    }
    
    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }
    
    public String getParent() {
        return parent;
    }
    
    public void setParent(String parent) {
        this.parent = parent;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public int getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public List<String> getSourceDirectories() {
        return sourceDirectories;
    }
    
    public void setSourceDirectories(List<String> sourceDirectories) {
        this.sourceDirectories = sourceDirectories;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }
    
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
    
    /**
     * Module type enumeration
     */
    public enum ModuleType {
        PARENT,        // Parent/aggregator module
        LIBRARY,       // Library module
        APPLICATION,   // Standalone application
        WEB,          // Web application
        API,          // API module
        SERVICE,      // Service module
        DATA,         // Data/persistence module
        COMMON,       // Common/shared module
        UNKNOWN       // Unknown type
    }
    
    /**
     * Build system enumeration
     */
    public enum BuildSystem {
        MAVEN,
        GRADLE,
        UNKNOWN
    }
}
