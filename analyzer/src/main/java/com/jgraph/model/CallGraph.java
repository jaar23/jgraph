package com.jgraph.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the call graph structure
 */
public class CallGraph {
    
    @JsonProperty("nodes")
    private List<GraphNode> nodes = new ArrayList<>();
    
    @JsonProperty("edges")
    private List<GraphEdge> edges = new ArrayList<>();
    
    public CallGraph() {}
    
    // Getters and Setters
    public List<GraphNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<GraphNode> nodes) {
        this.nodes = nodes;
    }
    
    public List<GraphEdge> getEdges() {
        return edges;
    }
    
    public void setEdges(List<GraphEdge> edges) {
        this.edges = edges;
    }
    
    public void addNode(GraphNode node) {
        this.nodes.add(node);
    }
    
    public void addEdge(GraphEdge edge) {
        this.edges.add(edge);
    }
}
