package com.jgraph.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jgraph.model.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Exports analysis results to JSON format
 */
public class JsonExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonExporter.class);
    private final ObjectMapper objectMapper;
    
    public JsonExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Export analysis result to JSON file
     */
    public void export(AnalysisResult result, String outputPath) throws IOException {
        logger.info("Exporting analysis to: {}", outputPath);
        
        File outputFile = new File(outputPath);
        
        // Create parent directories if needed
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // Write JSON
        objectMapper.writeValue(outputFile, result);
        
        logger.info("Export complete. File size: {} bytes", outputFile.length());
    }
    
    /**
     * Export analysis result and source map to JSON files
     */
    public void export(AnalysisResult result, com.jgraph.model.source.SourceMap sourceMap, String outputPath) throws IOException {
        // Export main analysis
        export(result, outputPath);
        
        // Export source map if it has content
        if (sourceMap != null && !sourceMap.getMethods().isEmpty()) {
            String sourceMapPath = outputPath.replace(".json", "-source-map.json");
            SourceMapExporter sourceMapExporter = new SourceMapExporter();
            sourceMapExporter.export(sourceMap, sourceMapPath);
        }
    }
    
    /**
     * Convert analysis result to JSON string
     */
    public String toJsonString(AnalysisResult result) throws IOException {
        return objectMapper.writeValueAsString(result);
    }
}
