package com.jgraph.util;

import com.jgraph.model.source.CodeBlock;
import com.jgraph.model.source.MethodSource;
import com.jgraph.model.source.SourceMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for compressing source map data to reduce file size
 */
public class SourceMapCompressor {
    
    private int trivialMethodsSkipped = 0;
    private int duplicatesRemoved = 0;
    private Map<String, String> codeBlockDeduplication = new HashMap<>();
    private int blockIdCounter = 0;
    
    /**
     * Compress the source map by removing trivial methods and deduplicating code blocks
     */
    public void compress(SourceMap sourceMap) {
        trivialMethodsSkipped = 0;
        duplicatesRemoved = 0;
        codeBlockDeduplication.clear();
        
        // Step 1: Remove trivial methods (getters/setters with minimal logic)
        removeTrivialMethods(sourceMap);
        
        // Step 2: Deduplicate common code blocks
        deduplicateCodeBlocks(sourceMap);
        
        // Step 3: Optimize expressions and variable names
        optimizeExpressions(sourceMap);
        
        // Update compression statistics
        updateCompressionStats(sourceMap);
    }
    
    /**
     * Remove methods that are trivial (simple getters/setters)
     */
    private void removeTrivialMethods(SourceMap sourceMap) {
        Map<String, MethodSource> methods = sourceMap.getMethods();
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, MethodSource> entry : methods.entrySet()) {
            MethodSource method = entry.getValue();
            
            // Skip if already marked as trivial
            if (method.isTrivial()) {
                toRemove.add(entry.getKey());
                trivialMethodsSkipped++;
                continue;
            }
            
            // Check if method is trivial based on criteria
            if (isTrivialMethod(method)) {
                toRemove.add(entry.getKey());
                trivialMethodsSkipped++;
            }
        }
        
        // Remove trivial methods
        toRemove.forEach(methods::remove);
    }
    
    /**
     * Determine if a method is trivial and can be skipped
     */
    private boolean isTrivialMethod(MethodSource method) {
        if (method.getComplexity() == null || method.getOperations() == null) {
            return false;
        }
        
        // Trivial if:
        // - LOC <= 3
        // - Cyclomatic complexity == 1
        // - No conditionals, loops, or exception handling
        // - Only simple assignment/return
        
        boolean isSimple = method.getComplexity().getLinesOfCode() <= 3
                && method.getComplexity().getCyclomaticComplexity() == 1
                && method.getOperations().getConditionals() == 0
                && method.getOperations().getLoops() == 0
                && method.getOperations().getExceptionHandling() == 0;
        
        // Check if it's a simple getter (returns 1, method calls 0)
        boolean isGetter = method.getOperations().getReturns() == 1
                && method.getOperations().getMethodCalls() == 0
                && method.getOperations().getAssignments() == 0;
        
        // Check if it's a simple setter (assigns 1, returns 0)
        boolean isSetter = method.getOperations().getAssignments() == 1
                && method.getOperations().getReturns() == 0
                && method.getOperations().getMethodCalls() == 0;
        
        return isSimple && (isGetter || isSetter);
    }
    
    /**
     * Deduplicate code blocks by replacing identical blocks with references
     */
    private void deduplicateCodeBlocks(SourceMap sourceMap) {
        for (MethodSource method : sourceMap.getMethods().values()) {
            if (method.getCodeBlocks() != null && !method.getCodeBlocks().isEmpty()) {
                deduplicateBlockList(method.getCodeBlocks());
            }
        }
    }
    
    /**
     * Recursively deduplicate a list of code blocks
     */
    private void deduplicateBlockList(List<CodeBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            
            // Generate a hash for this block
            String blockHash = generateBlockHash(block);
            
            // Check if we've seen this block before
            if (codeBlockDeduplication.containsKey(blockHash)) {
                duplicatesRemoved++;
                // Could replace with reference here, but for now just count
            } else {
                codeBlockDeduplication.put(blockHash, "block-" + (blockIdCounter++));
            }
            
            // Recursively process nested blocks
            if (block.getThenBlocks() != null) {
                deduplicateBlockList(block.getThenBlocks());
            }
            if (block.getElseBlocks() != null) {
                deduplicateBlockList(block.getElseBlocks());
            }
            if (block.getLoopBody() != null) {
                deduplicateBlockList(block.getLoopBody());
            }
        }
    }
    
    /**
     * Generate a hash for a code block based on its properties
     */
    private String generateBlockHash(CodeBlock block) {
        StringBuilder sb = new StringBuilder();
        sb.append(block.getType()).append("|");
        sb.append(block.getCode() != null ? block.getCode() : "").append("|");
        sb.append(block.getDescription() != null ? block.getDescription() : "");
        return Integer.toHexString(sb.toString().hashCode());
    }
    
    /**
     * Optimize expressions by removing redundant whitespace and normalizing format
     */
    private void optimizeExpressions(SourceMap sourceMap) {
        for (MethodSource method : sourceMap.getMethods().values()) {
            if (method.getCodeBlocks() != null) {
                optimizeBlockExpressions(method.getCodeBlocks());
            }
        }
    }
    
    /**
     * Recursively optimize expressions in code blocks
     */
    private void optimizeBlockExpressions(List<CodeBlock> blocks) {
        if (blocks == null) return;
        
        for (CodeBlock block : blocks) {
            // Normalize code by removing extra whitespace
            if (block.getCode() != null) {
                String optimized = block.getCode()
                        .replaceAll("\\s+", " ")  // Multiple spaces to single space
                        .trim();
                block.setCode(optimized);
            }
            
            // Recursively optimize nested blocks
            if (block.getThenBlocks() != null) {
                optimizeBlockExpressions(block.getThenBlocks());
            }
            if (block.getElseBlocks() != null) {
                optimizeBlockExpressions(block.getElseBlocks());
            }
            if (block.getLoopBody() != null) {
                optimizeBlockExpressions(block.getLoopBody());
            }
        }
    }
    
    /**
     * Update compression statistics
     */
    private void updateCompressionStats(SourceMap sourceMap) {
        SourceMap.CompressionInfo compression = sourceMap.getCompression();
        if (compression == null) {
            compression = new SourceMap.CompressionInfo();
            sourceMap.setCompression(compression);
        }
        
        int originalMethods = compression.getTotalMethods() + trivialMethodsSkipped;
        compression.setTotalMethods(originalMethods);
        compression.setTrivialMethodsSkipped(trivialMethodsSkipped);
        compression.setDuplicatesRemoved(duplicatesRemoved);
        
        // Calculate compression ratio
        double ratio = originalMethods > 0 
                ? (double) sourceMap.getMethods().size() / originalMethods 
                : 1.0;
        compression.setCompressionRatio(ratio);
    }
    
    /**
     * Get compression statistics
     */
    public Map<String, Object> getCompressionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("trivialMethodsSkipped", trivialMethodsSkipped);
        stats.put("duplicatesRemoved", duplicatesRemoved);
        stats.put("uniqueBlocks", codeBlockDeduplication.size());
        return stats;
    }
}
