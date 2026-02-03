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
    
    @JsonProperty("logStatements")
    private List<LogStatement> logStatements = new ArrayList<>();
    
    @JsonProperty("exceptionHandlers")
    private List<ExceptionHandler> exceptionHandlers = new ArrayList<>();
    
    @JsonProperty("tryCatchBlocks")
    private List<TryCatchBlock> tryCatchBlocks = new ArrayList<>();
    
    @JsonProperty("databaseOperations")
    private List<DatabaseOperation> databaseOperations = new ArrayList<>();
    
    @JsonProperty("transactions")
    private List<Transaction> transactions = new ArrayList<>();
    
    @JsonProperty("externalCalls")
    private List<ExternalCall> externalCalls = new ArrayList<>();
    
    @JsonProperty("dataFlows")
    private List<DataFlow> dataFlows = new ArrayList<>();
    
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
    
    public List<LogStatement> getLogStatements() {
        return logStatements;
    }
    
    public void setLogStatements(List<LogStatement> logStatements) {
        this.logStatements = logStatements;
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
    
    public List<ExceptionHandler> getExceptionHandlers() {
        return exceptionHandlers;
    }
    
    public void setExceptionHandlers(List<ExceptionHandler> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }
    
    public List<TryCatchBlock> getTryCatchBlocks() {
        return tryCatchBlocks;
    }
    
    public void setTryCatchBlocks(List<TryCatchBlock> tryCatchBlocks) {
        this.tryCatchBlocks = tryCatchBlocks;
    }
    
    public List<DatabaseOperation> getDatabaseOperations() {
        return databaseOperations;
    }
    
    public void setDatabaseOperations(List<DatabaseOperation> databaseOperations) {
        this.databaseOperations = databaseOperations;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public List<ExternalCall> getExternalCalls() {
        return externalCalls;
    }
    
    public void setExternalCalls(List<ExternalCall> externalCalls) {
        this.externalCalls = externalCalls;
    }
    
    public List<DataFlow> getDataFlows() {
        return dataFlows;
    }
    
    public void setDataFlows(List<DataFlow> dataFlows) {
        this.dataFlows = dataFlows;
    }
}
