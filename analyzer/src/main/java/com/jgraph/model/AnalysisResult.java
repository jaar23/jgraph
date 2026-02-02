package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Root analysis result containing all extracted information
 */
public class AnalysisResult {
    
    @JsonProperty("project")
    private ProjectInfo project;
    
    @JsonProperty("endpoints")
    private List<Endpoint> endpoints = new ArrayList<>();
    
    @JsonProperty("services")
    private List<ServiceMethod> services = new ArrayList<>();
    
    @JsonProperty("repositories")
    private List<RepositoryMethod> repositories = new ArrayList<>();
    
    @JsonProperty("dependencies")
    private List<Dependency> dependencies = new ArrayList<>();
    
    @JsonProperty("callGraph")
    private CallGraph callGraph = new CallGraph();
    
    @JsonProperty("statistics")
    private Statistics statistics = new Statistics();
    
    // Constructors
    public AnalysisResult() {}
    
    public AnalysisResult(ProjectInfo project) {
        this.project = project;
    }
    
    // Getters and Setters
    public ProjectInfo getProject() {
        return project;
    }
    
    public void setProject(ProjectInfo project) {
        this.project = project;
    }
    
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }
    
    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }
    
    public List<ServiceMethod> getServices() {
        return services;
    }
    
    public void setServices(List<ServiceMethod> services) {
        this.services = services;
    }
    
    public List<RepositoryMethod> getRepositories() {
        return repositories;
    }
    
    public void setRepositories(List<RepositoryMethod> repositories) {
        this.repositories = repositories;
    }
    
    public List<Dependency> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public CallGraph getCallGraph() {
        return callGraph;
    }
    
    public void setCallGraph(CallGraph callGraph) {
        this.callGraph = callGraph;
    }
    
    public Statistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
