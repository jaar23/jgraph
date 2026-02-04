package com.jgraph.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.Node;
import com.jgraph.model.source.ComplexityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Calculates complexity metrics for methods
 */
public class ComplexityCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplexityCalculator.class);
    
    /**
     * Calculate all complexity metrics for a method
     */
    public ComplexityMetrics calculate(MethodDeclaration method) {
        ComplexityMetrics metrics = new ComplexityMetrics();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            // Abstract or interface method - no complexity
            return metrics;
        }
        
        BlockStmt block = body.get();
        
        // Calculate cyclomatic complexity
        int cyclomaticComplexity = calculateCyclomaticComplexity(block);
        metrics.setCyclomaticComplexity(cyclomaticComplexity);
        
        // Calculate cognitive complexity
        int cognitiveComplexity = calculateCognitiveComplexity(block, 0);
        metrics.setCognitiveComplexity(cognitiveComplexity);
        
        // Calculate lines of code
        int linesOfCode = calculateLinesOfCode(method);
        metrics.setLinesOfCode(linesOfCode);
        
        // Count statements
        int statementCount = countStatements(block);
        metrics.setStatementCount(statementCount);
        
        // Calculate maximum nesting depth
        int nestingDepth = calculateMaxNestingDepth(block, 0);
        metrics.setNestingDepth(nestingDepth);
        
        // Calculate maintainability index
        double maintainabilityIndex = calculateMaintainabilityIndex(
            linesOfCode, cyclomaticComplexity, statementCount
        );
        metrics.setMaintainabilityIndex(maintainabilityIndex);
        
        return metrics;
    }
    
    /**
     * Calculate cyclomatic complexity (McCabe)
     * Formula: M = E - N + 2P (edges - nodes + 2*programs)
     * Simplified: M = 1 + number of decision points
     */
    private int calculateCyclomaticComplexity(BlockStmt block) {
        int complexity = 1; // Base complexity
        
        // Count if statements
        complexity += block.findAll(IfStmt.class).size();
        
        // Count for loops
        complexity += block.findAll(ForStmt.class).size();
        complexity += block.findAll(ForEachStmt.class).size();
        
        // Count while loops
        complexity += block.findAll(WhileStmt.class).size();
        complexity += block.findAll(DoStmt.class).size();
        
        // Count case statements in switch
        for (SwitchStmt switchStmt : block.findAll(SwitchStmt.class)) {
            complexity += switchStmt.getEntries().size();
        }
        
        // Count catch blocks
        for (TryStmt tryStmt : block.findAll(TryStmt.class)) {
            complexity += tryStmt.getCatchClauses().size();
        }
        
        // Count ternary operators
        complexity += block.findAll(com.github.javaparser.ast.expr.ConditionalExpr.class).size();
        
        // Count logical operators (&&, ||) in conditions
        complexity += countLogicalOperators(block);
        
        return complexity;
    }
    
    /**
     * Calculate cognitive complexity (more accurate for readability)
     * Considers nesting depth and structural complexity
     */
    private int calculateCognitiveComplexity(Node node, int nestingLevel) {
        int complexity = 0;
        
        for (Node child : node.getChildNodes()) {
            // Increment for control flow structures
            if (child instanceof IfStmt) {
                complexity += 1 + nestingLevel;
                IfStmt ifStmt = (IfStmt) child;
                complexity += calculateCognitiveComplexity(ifStmt.getThenStmt(), nestingLevel + 1);
                if (ifStmt.getElseStmt().isPresent()) {
                    complexity += 1; // +1 for else
                    complexity += calculateCognitiveComplexity(ifStmt.getElseStmt().get(), nestingLevel + 1);
                }
            } else if (child instanceof ForStmt || child instanceof ForEachStmt || 
                       child instanceof WhileStmt || child instanceof DoStmt) {
                complexity += 1 + nestingLevel;
                complexity += calculateCognitiveComplexity(child, nestingLevel + 1);
            } else if (child instanceof SwitchStmt) {
                complexity += 1 + nestingLevel;
                SwitchStmt switchStmt = (SwitchStmt) child;
                for (SwitchEntry entry : switchStmt.getEntries()) {
                    complexity += calculateCognitiveComplexity(entry, nestingLevel + 1);
                }
            } else if (child instanceof TryStmt) {
                TryStmt tryStmt = (TryStmt) child;
                complexity += calculateCognitiveComplexity(tryStmt.getTryBlock(), nestingLevel);
                for (CatchClause catchClause : tryStmt.getCatchClauses()) {
                    complexity += 1 + nestingLevel;
                    complexity += calculateCognitiveComplexity(catchClause.getBody(), nestingLevel + 1);
                }
            } else if (child instanceof BinaryExpr) {
                BinaryExpr binaryExpr = (BinaryExpr) child;
                if (binaryExpr.getOperator() == BinaryExpr.Operator.AND || 
                    binaryExpr.getOperator() == BinaryExpr.Operator.OR) {
                    complexity += 1;
                }
            } else {
                // Recursively check other nodes
                complexity += calculateCognitiveComplexity(child, nestingLevel);
            }
        }
        
        return complexity;
    }
    
    /**
     * Count logical operators (&& and ||) which add to complexity
     */
    private int countLogicalOperators(BlockStmt block) {
        int count = 0;
        for (BinaryExpr expr : block.findAll(BinaryExpr.class)) {
            if (expr.getOperator() == BinaryExpr.Operator.AND || 
                expr.getOperator() == BinaryExpr.Operator.OR) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Calculate lines of code (physical LOC)
     */
    private int calculateLinesOfCode(MethodDeclaration method) {
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return 0;
        }
        
        int startLine = method.getBegin().map(pos -> pos.line).orElse(0);
        int endLine = method.getEnd().map(pos -> pos.line).orElse(0);
        
        return Math.max(0, endLine - startLine + 1);
    }
    
    /**
     * Count total statements in method
     */
    private int countStatements(BlockStmt block) {
        int count = 0;
        
        // Count various statement types
        count += block.findAll(ExpressionStmt.class).size();
        count += block.findAll(ReturnStmt.class).size();
        count += block.findAll(IfStmt.class).size();
        count += block.findAll(ForStmt.class).size();
        count += block.findAll(ForEachStmt.class).size();
        count += block.findAll(WhileStmt.class).size();
        count += block.findAll(DoStmt.class).size();
        count += block.findAll(SwitchStmt.class).size();
        count += block.findAll(TryStmt.class).size();
        count += block.findAll(ThrowStmt.class).size();
        count += block.findAll(BreakStmt.class).size();
        count += block.findAll(ContinueStmt.class).size();
        
        return count;
    }
    
    /**
     * Calculate maximum nesting depth
     */
    private int calculateMaxNestingDepth(Node node, int currentDepth) {
        int maxDepth = currentDepth;
        
        for (Node child : node.getChildNodes()) {
            int childDepth = currentDepth;
            
            // Increment depth for nesting structures
            if (child instanceof IfStmt || child instanceof ForStmt || 
                child instanceof ForEachStmt || child instanceof WhileStmt || 
                child instanceof DoStmt || child instanceof SwitchStmt ||
                child instanceof TryStmt) {
                childDepth = currentDepth + 1;
            }
            
            int depth = calculateMaxNestingDepth(child, childDepth);
            maxDepth = Math.max(maxDepth, depth);
        }
        
        return maxDepth;
    }
    
    /**
     * Calculate maintainability index
     * Formula (simplified Microsoft version):
     * MI = MAX(0, (171 - 5.2 * ln(Halstead Volume) - 0.23 * (Cyclomatic Complexity) - 16.2 * ln(Lines of Code)) * 100 / 171)
     * 
     * For simplicity, we use an approximation:
     * MI = 171 - 5.2 * ln(LOC) - 0.23 * CC - 16.2 * ln(Statements)
     */
    private double calculateMaintainabilityIndex(int linesOfCode, int cyclomaticComplexity, int statementCount) {
        if (linesOfCode == 0) {
            return 100.0; // Empty method is perfectly maintainable
        }
        
        // Ensure we don't take log of 0
        double loc = Math.max(1, linesOfCode);
        double statements = Math.max(1, statementCount);
        
        double mi = 171.0 
                    - 5.2 * Math.log(loc)
                    - 0.23 * cyclomaticComplexity
                    - 16.2 * Math.log(statements);
        
        // Normalize to 0-100 scale
        mi = (mi * 100.0) / 171.0;
        
        // Clamp between 0 and 100
        return Math.max(0.0, Math.min(100.0, mi));
    }
}
