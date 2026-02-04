package com.jgraph.parser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.jgraph.model.source.VariableLifecycle;
import com.jgraph.model.source.VariableUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Tracks variable lifecycles throughout methods
 */
public class VariableTracker {
    
    private static final Logger logger = LoggerFactory.getLogger(VariableTracker.class);
    
    /**
     * Track all variables in a method
     */
    public List<VariableLifecycle> trackVariables(MethodDeclaration method) {
        Map<String, VariableLifecycle> variableMap = new HashMap<>();
        
        // Track parameters
        for (Parameter param : method.getParameters()) {
            String varName = param.getNameAsString();
            String varType = param.getTypeAsString();
            int lineNumber = param.getBegin().map(pos -> pos.line).orElse(0);
            
            VariableLifecycle lifecycle = new VariableLifecycle(varName, varType, lineNumber);
            lifecycle.setParameter(true);
            lifecycle.setScope("method");
            
            variableMap.put(varName, lifecycle);
        }
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return new ArrayList<>(variableMap.values());
        }
        
        BlockStmt blockStmt = body.get();
        
        // Track variable declarations
        for (VariableDeclarator var : blockStmt.findAll(VariableDeclarator.class)) {
            String varName = var.getNameAsString();
            String varType = var.getTypeAsString();
            int lineNumber = var.getBegin().map(pos -> pos.line).orElse(0);
            
            VariableLifecycle lifecycle = new VariableLifecycle(varName, varType, lineNumber);
            lifecycle.setParameter(false);
            lifecycle.setScope(determineScope(var));
            
            // Track initial value
            if (var.getInitializer().isPresent()) {
                lifecycle.setInitialValue(var.getInitializer().get().toString());
            }
            
            variableMap.put(varName, lifecycle);
        }
        
        // Track variable usages
        for (Map.Entry<String, VariableLifecycle> entry : variableMap.entrySet()) {
            String varName = entry.getKey();
            VariableLifecycle lifecycle = entry.getValue();
            
            // Find all usages
            List<VariableUsage> usages = findUsages(varName, blockStmt);
            for (VariableUsage usage : usages) {
                lifecycle.addUsage(usage);
                
                // Mark as mutated if written to (except initial declaration)
                if ("WRITE".equals(usage.getUsageType()) && 
                    usage.getLineNumber() != lifecycle.getDeclarationLine()) {
                    lifecycle.setMutated(true);
                }
            }
        }
        
        return new ArrayList<>(variableMap.values());
    }
    
    /**
     * Find all usages of a variable
     */
    private List<VariableUsage> findUsages(String varName, BlockStmt body) {
        List<VariableUsage> usages = new ArrayList<>();
        
        // Find NameExpr (variable references)
        for (NameExpr nameExpr : body.findAll(NameExpr.class)) {
            if (nameExpr.getNameAsString().equals(varName)) {
                int lineNumber = nameExpr.getBegin().map(pos -> pos.line).orElse(0);
                String usageType = determineUsageType(nameExpr);
                String context = determineContext(nameExpr);
                
                VariableUsage usage = new VariableUsage(varName, usageType, lineNumber);
                usage.setContext(context);
                usage.setExpression(getExpression(nameExpr));
                
                usages.add(usage);
            }
        }
        
        // Find field access (e.g., this.varName or object.varName)
        for (FieldAccessExpr fieldAccess : body.findAll(FieldAccessExpr.class)) {
            if (fieldAccess.getNameAsString().equals(varName)) {
                int lineNumber = fieldAccess.getBegin().map(pos -> pos.line).orElse(0);
                String usageType = determineUsageType(fieldAccess);
                String context = determineContext(fieldAccess);
                
                VariableUsage usage = new VariableUsage(varName, usageType, lineNumber);
                usage.setContext(context);
                usage.setExpression(fieldAccess.toString());
                
                usages.add(usage);
            }
        }
        
        return usages;
    }
    
    /**
     * Determine if usage is READ, WRITE, or READ_WRITE
     */
    private String determineUsageType(Expression expr) {
        Node parent = expr.getParentNode().orElse(null);
        
        if (parent instanceof AssignExpr) {
            AssignExpr assignExpr = (AssignExpr) parent;
            // Check if variable is on left side (WRITE) or right side (READ)
            if (assignExpr.getTarget().equals(expr)) {
                // It's being written to
                return "WRITE";
            } else {
                // It's being read from
                return "READ";
            }
        } else if (parent instanceof UnaryExpr) {
            UnaryExpr unaryExpr = (UnaryExpr) parent;
            // ++ and -- operators modify the variable
            if (unaryExpr.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT ||
                unaryExpr.getOperator() == UnaryExpr.Operator.PREFIX_DECREMENT ||
                unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT ||
                unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT) {
                return "READ_WRITE";
            }
        } else if (parent instanceof VariableDeclarator) {
            // Variable initialization
            return "WRITE";
        }
        
        // Default to READ
        return "READ";
    }
    
    /**
     * Determine context of usage (conditional, loop, method call, etc.)
     */
    private String determineContext(Expression expr) {
        Node parent = expr.getParentNode().orElse(null);
        
        while (parent != null) {
            if (parent instanceof com.github.javaparser.ast.stmt.IfStmt) {
                return "conditional";
            } else if (parent instanceof com.github.javaparser.ast.stmt.ForStmt ||
                       parent instanceof com.github.javaparser.ast.stmt.ForEachStmt ||
                       parent instanceof com.github.javaparser.ast.stmt.WhileStmt) {
                return "loop";
            } else if (parent instanceof MethodCallExpr) {
                return "method_call";
            } else if (parent instanceof com.github.javaparser.ast.stmt.ReturnStmt) {
                return "return";
            } else if (parent instanceof AssignExpr) {
                return "assignment";
            } else if (parent instanceof BinaryExpr) {
                return "expression";
            }
            
            parent = parent.getParentNode().orElse(null);
        }
        
        return "unknown";
    }
    
    /**
     * Get the full expression containing the variable
     */
    private String getExpression(Expression expr) {
        Node parent = expr.getParentNode().orElse(null);
        
        if (parent instanceof AssignExpr) {
            return parent.toString();
        } else if (parent instanceof MethodCallExpr) {
            return parent.toString();
        } else if (parent instanceof BinaryExpr) {
            return parent.toString();
        } else if (parent instanceof com.github.javaparser.ast.stmt.ReturnStmt) {
            return parent.toString();
        }
        
        return expr.toString();
    }
    
    /**
     * Determine scope of variable (method, block, loop, etc.)
     */
    private String determineScope(VariableDeclarator var) {
        Node parent = var.getParentNode().orElse(null);
        
        while (parent != null) {
            if (parent instanceof com.github.javaparser.ast.stmt.ForStmt ||
                parent instanceof com.github.javaparser.ast.stmt.ForEachStmt) {
                return "loop";
            } else if (parent instanceof com.github.javaparser.ast.stmt.IfStmt) {
                return "conditional";
            } else if (parent instanceof com.github.javaparser.ast.stmt.TryStmt) {
                return "try_catch";
            } else if (parent instanceof BlockStmt) {
                Node blockParent = parent.getParentNode().orElse(null);
                if (blockParent instanceof MethodDeclaration) {
                    return "method";
                }
                return "block";
            }
            
            parent = parent.getParentNode().orElse(null);
        }
        
        return "method";
    }
}
