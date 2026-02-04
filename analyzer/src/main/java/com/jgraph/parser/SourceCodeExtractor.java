package com.jgraph.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.jgraph.model.source.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Orchestrator for extracting enhanced source code information
 */
public class SourceCodeExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(SourceCodeExtractor.class);
    
    private final ComplexityCalculator complexityCalculator;
    private final CodeBlockExtractor codeBlockExtractor;
    private final VariableTracker variableTracker;
    private final LogicFlowBuilder logicFlowBuilder;
    
    public enum DetailLevel {
        MINIMAL,    // Only complexity + operations
        STANDARD,   // + code blocks + logic flow (no variable lifecycle)
        DETAILED    // Everything including variable lifecycle
    }
    
    public SourceCodeExtractor() {
        this.complexityCalculator = new ComplexityCalculator();
        this.codeBlockExtractor = new CodeBlockExtractor();
        this.variableTracker = new VariableTracker();
        this.logicFlowBuilder = new LogicFlowBuilder();
    }
    
    /**
     * Extract all source information for a method
     */
    public MethodSource extractMethodSource(
            MethodDeclaration method, 
            String methodId,
            DetailLevel detailLevel) {
        
        // Check if method should be skipped
        if (isTrivial(method)) {
            return createTrivialMethodSource(method, methodId);
        }
        
        MethodSource methodSource = new MethodSource(methodId, method.getDeclarationAsString());
        
        // Calculate source lines
        int startLine = method.getBegin().map(pos -> pos.line).orElse(0);
        int endLine = method.getEnd().map(pos -> pos.line).orElse(0);
        methodSource.setSourceLines(Math.max(0, endLine - startLine + 1));
        
        // Mark if generated
        methodSource.setGenerated(isGenerated(method));
        methodSource.setTrivial(false);
        
        try {
            // ALWAYS calculate complexity and operations (even for MINIMAL)
            ComplexityMetrics complexity = complexityCalculator.calculate(method);
            methodSource.setComplexity(complexity);
            
            OperationSummary operations = codeBlockExtractor.countOperations(method);
            methodSource.setOperations(operations);
            
            if (detailLevel == DetailLevel.MINIMAL) {
                // Minimal: Only complexity + operations
                methodSource.setSummary("Method with " + complexity.getLinesOfCode() + " lines of code.");
                return methodSource;
            }
            
            // STANDARD and DETAILED: Extract code blocks
            List<CodeBlock> codeBlocks = codeBlockExtractor.extractCodeBlocks(method);
            methodSource.setCodeBlocks(codeBlocks);
            
            // Build logic flow
            LogicFlow logicFlow = logicFlowBuilder.buildLogicFlow(method, codeBlocks);
            methodSource.setLogicFlow(logicFlow);
            methodSource.setSummary(logicFlow.getSummary());
            
            if (detailLevel == DetailLevel.DETAILED) {
                // DETAILED: Also track variable lifecycles
                List<VariableLifecycle> variables = variableTracker.trackVariables(method);
                methodSource.setVariables(variables);
            }
            
        } catch (Exception e) {
            logger.error("Error extracting source for method: {} - {}", methodId, e.getMessage());
            methodSource.setSummary("Error analyzing method: " + e.getMessage());
        }
        
        return methodSource;
    }
    
    /**
     * Create minimal source info for trivial methods
     */
    private MethodSource createTrivialMethodSource(MethodDeclaration method, String methodId) {
        MethodSource methodSource = new MethodSource(methodId, method.getDeclarationAsString());
        methodSource.setTrivial(true);
        
        // Simple complexity
        ComplexityMetrics complexity = new ComplexityMetrics();
        complexity.setCyclomaticComplexity(1);
        complexity.setCognitiveComplexity(0);
        
        int startLine = method.getBegin().map(pos -> pos.line).orElse(0);
        int endLine = method.getEnd().map(pos -> pos.line).orElse(0);
        int loc = Math.max(0, endLine - startLine + 1);
        complexity.setLinesOfCode(loc);
        complexity.setStatementCount(1);
        
        methodSource.setComplexity(complexity);
        methodSource.setSourceLines(loc);
        
        // Simple operations
        OperationSummary operations = new OperationSummary();
        operations.setReturns(1);
        methodSource.setOperations(operations);
        
        // Simple summary
        String methodName = method.getNameAsString();
        if (methodName.startsWith("get")) {
            methodSource.setSummary("Simple getter method.");
        } else if (methodName.startsWith("set")) {
            methodSource.setSummary("Simple setter method.");
        } else if (methodName.startsWith("is") || methodName.startsWith("has")) {
            methodSource.setSummary("Simple boolean check method.");
        } else {
            methodSource.setSummary("Trivial method.");
        }
        
        return methodSource;
    }
    
    /**
     * Determine if method is trivial (getter/setter/simple delegation)
     */
    public boolean isTrivial(MethodDeclaration method) {
        Optional<com.github.javaparser.ast.stmt.BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            // Abstract or interface method
            return true;
        }
        
        com.github.javaparser.ast.stmt.BlockStmt blockStmt = body.get();
        
        // Count statements
        int statementCount = blockStmt.getStatements().size();
        
        // Trivial if only 1-2 statements
        if (statementCount == 0) {
            return true;
        }
        
        if (statementCount == 1) {
            // Check if it's a simple return or assignment
            com.github.javaparser.ast.stmt.Statement stmt = blockStmt.getStatement(0);
            if (stmt instanceof com.github.javaparser.ast.stmt.ReturnStmt) {
                return true; // Simple return
            }
            if (stmt instanceof com.github.javaparser.ast.stmt.ExpressionStmt) {
                com.github.javaparser.ast.expr.Expression expr = 
                    ((com.github.javaparser.ast.stmt.ExpressionStmt) stmt).getExpression();
                if (expr instanceof com.github.javaparser.ast.expr.AssignExpr) {
                    return true; // Simple assignment
                }
            }
        }
        
        // Check for getter/setter pattern
        String methodName = method.getNameAsString();
        if (methodName.startsWith("get") || methodName.startsWith("set") || 
            methodName.startsWith("is") || methodName.startsWith("has")) {
            if (statementCount <= 2) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determine if method is generated code
     */
    private boolean isGenerated(MethodDeclaration method) {
        // Check for Lombok annotations
        for (com.github.javaparser.ast.expr.AnnotationExpr annotation : method.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (annotationName.contains("Generated") || 
                annotationName.contains("Lombok")) {
                return true;
            }
        }
        
        // Check parent class for Lombok annotations
        method.getParentNode().ifPresent(parent -> {
            if (parent instanceof com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) {
                com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDecl = 
                    (com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) parent;
                for (com.github.javaparser.ast.expr.AnnotationExpr annotation : classDecl.getAnnotations()) {
                    String annotationName = annotation.getNameAsString();
                    if (annotationName.equals("Data") || annotationName.equals("Getter") || 
                        annotationName.equals("Setter") || annotationName.equals("Builder")) {
                        // Lombok generated
                        return;
                    }
                }
            }
        });
        
        // Check for JPA derived query method patterns
        String methodName = method.getNameAsString();
        if (methodName.startsWith("findBy") || methodName.startsWith("deleteBy") ||
            methodName.startsWith("countBy") || methodName.startsWith("existsBy") ||
            methodName.startsWith("readBy") || methodName.startsWith("queryBy") ||
            methodName.startsWith("getBy")) {
            // Likely JPA derived query
            return true;
        }
        
        return false;
    }
}
