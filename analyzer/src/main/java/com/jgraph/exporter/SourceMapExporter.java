package com.jgraph.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jgraph.model.source.SourceMap;
import com.jgraph.util.SourceMapCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Exports source map to JSON file
 */
public class SourceMapExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(SourceMapExporter.class);
    private final ObjectMapper objectMapper;
    private final boolean enableCompression;
    
    public SourceMapExporter() {
        this(true); // Enable compression by default
    }
    
    public SourceMapExporter(boolean enableCompression) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.enableCompression = enableCompression;
    }
    
    /**
     * Export source map to JSON file
     */
    public void export(SourceMap sourceMap, String outputPath) {
        try {
            // Apply compression if enabled
            if (enableCompression) {
                SourceMapCompressor compressor = new SourceMapCompressor();
                compressor.compress(sourceMap);
                
                // Log compression stats
                Map<String, Object> stats = compressor.getCompressionStats();
                logger.info("  Compression applied:");
                logger.info("    Trivial methods skipped: {}", stats.get("trivialMethodsSkipped"));
                logger.info("    Duplicate blocks removed: {}", stats.get("duplicatesRemoved"));
                logger.info("    Unique blocks: {}", stats.get("uniqueBlocks"));
            }
            
            // Calculate statistics before export
            calculateStatistics(sourceMap);
            
            // Write to file
            File outputFile = new File(outputPath);
            objectMapper.writeValue(outputFile, sourceMap);
            
            logger.info("Source map exported to: {}", outputPath);
            logger.info("  Methods in source map: {}", sourceMap.getMethods().size());
            
            // Log file size
            long fileSizeKB = outputFile.length() / 1024;
            logger.info("  Source map file size: {} KB", fileSizeKB);
            
        } catch (IOException e) {
            logger.error("Error exporting source map: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export source map", e);
        }
    }
    
    /**
     * Calculate statistics about the source map
     */
    private void calculateStatistics(SourceMap sourceMap) {
        SourceMap.Statistics stats = new SourceMap.Statistics();
        
        int totalMethods = sourceMap.getMethods().size();
        stats.setTotalMethodsAnalyzed(totalMethods);
        stats.setMethodsInSourceMap(totalMethods);
        
        // Calculate average complexity
        if (totalMethods > 0) {
            double totalComplexity = 0;
            double totalLoc = 0;
            
            for (com.jgraph.model.source.MethodSource method : sourceMap.getMethods().values()) {
                if (method.getComplexity() != null) {
                    totalComplexity += method.getComplexity().getCyclomaticComplexity();
                    totalLoc += method.getComplexity().getLinesOfCode();
                }
            }
            
            stats.setAverageComplexity(totalComplexity / totalMethods);
            stats.setAverageLinesOfCode(totalLoc / totalMethods);
        }
        
        sourceMap.setStatistics(stats);
        
        // Set compression info (for now, no actual compression applied)
        SourceMap.CompressionInfo compression = new SourceMap.CompressionInfo();
        compression.setTotalMethods(totalMethods);
        compression.setTrivialMethodsSkipped(0); // TODO: track this
        compression.setDuplicatesRemoved(0);
        compression.setCompressionRatio(1.0); // No compression yet
        
        sourceMap.setCompression(compression);
    }
}
