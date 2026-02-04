package com.jgraph.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Root container for source-map.json
 */
public class SourceMap {
    
    @JsonProperty("generatedAt")
    private String generatedAt;
    
    @JsonProperty("projectName")
    private String projectName;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("detailLevel")
    private String detailLevel;    // minimal, standard, detailed
    
    @JsonProperty("methods")
    private Map<String, MethodSource> methods = new HashMap<>();  // Indexed by methodId
    
    @JsonProperty("compression")
    private CompressionInfo compression;
    
    @JsonProperty("statistics")
    private Statistics statistics;
    
    public SourceMap() {
        this.generatedAt = Instant.now().toString();
        this.version = "1.0.0";
    }
    
    // Getters and Setters
    public String getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDetailLevel() {
        return detailLevel;
    }
    
    public void setDetailLevel(String detailLevel) {
        this.detailLevel = detailLevel;
    }
    
    public Map<String, MethodSource> getMethods() {
        return methods;
    }
    
    public void setMethods(Map<String, MethodSource> methods) {
        this.methods = methods;
    }
    
    public void addMethod(String methodId, MethodSource source) {
        this.methods.put(methodId, source);
    }
    
    public MethodSource getMethod(String methodId) {
        return this.methods.get(methodId);
    }
    
    public CompressionInfo getCompression() {
        return compression;
    }
    
    public void setCompression(CompressionInfo compression) {
        this.compression = compression;
    }
    
    public Statistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
    
    /**
     * Compression information
     */
    public static class CompressionInfo {
        @JsonProperty("totalMethods")
        private int totalMethods;
        
        @JsonProperty("trivialMethodsSkipped")
        private int trivialMethodsSkipped;
        
        @JsonProperty("duplicatesRemoved")
        private int duplicatesRemoved;
        
        @JsonProperty("compressionRatio")
        private double compressionRatio;
        
        public int getTotalMethods() {
            return totalMethods;
        }
        
        public void setTotalMethods(int totalMethods) {
            this.totalMethods = totalMethods;
        }
        
        public int getTrivialMethodsSkipped() {
            return trivialMethodsSkipped;
        }
        
        public void setTrivialMethodsSkipped(int trivialMethodsSkipped) {
            this.trivialMethodsSkipped = trivialMethodsSkipped;
        }
        
        public int getDuplicatesRemoved() {
            return duplicatesRemoved;
        }
        
        public void setDuplicatesRemoved(int duplicatesRemoved) {
            this.duplicatesRemoved = duplicatesRemoved;
        }
        
        public double getCompressionRatio() {
            return compressionRatio;
        }
        
        public void setCompressionRatio(double compressionRatio) {
            this.compressionRatio = compressionRatio;
        }
    }
    
    /**
     * Statistics about the source map
     */
    public static class Statistics {
        @JsonProperty("totalMethodsAnalyzed")
        private int totalMethodsAnalyzed;
        
        @JsonProperty("methodsInSourceMap")
        private int methodsInSourceMap;
        
        @JsonProperty("averageComplexity")
        private double averageComplexity;
        
        @JsonProperty("averageLinesOfCode")
        private double averageLinesOfCode;
        
        public int getTotalMethodsAnalyzed() {
            return totalMethodsAnalyzed;
        }
        
        public void setTotalMethodsAnalyzed(int totalMethodsAnalyzed) {
            this.totalMethodsAnalyzed = totalMethodsAnalyzed;
        }
        
        public int getMethodsInSourceMap() {
            return methodsInSourceMap;
        }
        
        public void setMethodsInSourceMap(int methodsInSourceMap) {
            this.methodsInSourceMap = methodsInSourceMap;
        }
        
        public double getAverageComplexity() {
            return averageComplexity;
        }
        
        public void setAverageComplexity(double averageComplexity) {
            this.averageComplexity = averageComplexity;
        }
        
        public double getAverageLinesOfCode() {
            return averageLinesOfCode;
        }
        
        public void setAverageLinesOfCode(double averageLinesOfCode) {
            this.averageLinesOfCode = averageLinesOfCode;
        }
    }
}
