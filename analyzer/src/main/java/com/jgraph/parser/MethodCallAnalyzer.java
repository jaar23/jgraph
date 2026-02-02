package com.jgraph.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.jgraph.model.Endpoint;
import com.jgraph.model.ServiceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Analyzes method calls within methods to build call chains
 */
public class MethodCallAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodCallAnalyzer.class);
    
    /**
     * Extract method calls from endpoint
     */
    public List<String> extractMethodCalls(Endpoint endpoint, Map<String, ClassOrInterfaceDeclaration> classMap) {
        List<String> calls = new ArrayList<>();
        
        // Find the class
        ClassOrInterfaceDeclaration classDecl = classMap.get(endpoint.getControllerClass());
        if (classDecl == null) {
            return calls;
        }
        
        // Find the method
        Optional<MethodDeclaration> methodOpt = classDecl.getMethodsByName(endpoint.getMethodName())
            .stream()
            .findFirst();
        
        if (methodOpt.isPresent()) {
            MethodDeclaration method = methodOpt.get();
            calls = extractMethodCallsFromMethod(method);
        }
        
        return calls;
    }
    
    /**
     * Extract method calls from service method
     */
    public List<String> extractMethodCalls(ServiceMethod service, Map<String, ClassOrInterfaceDeclaration> classMap) {
        List<String> calls = new ArrayList<>();
        
        // Find the class
        ClassOrInterfaceDeclaration classDecl = classMap.get(service.getClassName());
        if (classDecl == null) {
            return calls;
        }
        
        // Find the method
        Optional<MethodDeclaration> methodOpt = classDecl.getMethodsByName(service.getMethodName())
            .stream()
            .findFirst();
        
        if (methodOpt.isPresent()) {
            MethodDeclaration method = methodOpt.get();
            calls = extractMethodCallsFromMethod(method);
        }
        
        return calls;
    }
    
    /**
     * Extract all method calls from a method body
     */
    private List<String> extractMethodCallsFromMethod(MethodDeclaration method) {
        List<String> calls = new ArrayList<>();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return calls;
        }
        
        // Find all method call expressions
        body.get().findAll(MethodCallExpr.class).forEach(methodCall -> {
            String callString = buildMethodCallString(methodCall);
            if (!callString.isEmpty()) {
                calls.add(callString);
            }
        });
        
        return calls;
    }
    
    /**
     * Build a string representation of a method call
     * e.g., "userService.findById" or "this.questRepo.save"
     */
    private String buildMethodCallString(MethodCallExpr methodCall) {
        StringBuilder sb = new StringBuilder();
        
        // Get the scope (object on which method is called)
        methodCall.getScope().ifPresent(scope -> {
            String scopeStr = scope.toString();
            // Remove "this." prefix if present
            if (scopeStr.equals("this")) {
                scopeStr = "";
            } else {
                sb.append(scopeStr).append(".");
            }
        });
        
        // Add method name
        sb.append(methodCall.getNameAsString());
        
        return sb.toString();
    }
}
