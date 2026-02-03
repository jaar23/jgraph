package com.jgraph.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import java.util.*;

/**
 * Analyzes data flow through methods - tracks parameters, variables, and return values
 */
public class DataFlowAnalyzer {
    
    /**
     * Extract data flow information from a method
     */
    public DataFlowInfo extractDataFlow(MethodDeclaration method) {
        DataFlowInfo dataFlow = new DataFlowInfo();
        dataFlow.methodName = method.getNameAsString();
        
        // Extract parameters
        for (Parameter param : method.getParameters()) {
            ParameterInfo paramInfo = new ParameterInfo();
            paramInfo.name = param.getNameAsString();
            paramInfo.type = param.getTypeAsString();
            paramInfo.isAnnotated = !param.getAnnotations().isEmpty();
            
            // Check for parameter annotations (validation, injection, etc.)
            param.getAnnotations().forEach(ann -> {
                String annName = ann.getNameAsString();
                paramInfo.annotations.add(annName);
                
                // Check if it's a data transfer annotation
                if (annName.equals("RequestBody") || annName.equals("RequestParam") || 
                    annName.equals("PathVariable") || annName.equals("ModelAttribute")) {
                    paramInfo.isInputData = true;
                }
            });
            
            dataFlow.parameters.add(paramInfo);
        }
        
        // Track variable assignments and transformations
        method.getBody().ifPresent(body -> {
            extractVariableFlow(body, dataFlow);
            extractReturnFlow(body, dataFlow);
            extractMethodCallDataFlow(body, dataFlow);
        });
        
        return dataFlow;
    }
    
    /**
     * Extract variable declarations and assignments
     */
    private void extractVariableFlow(BlockStmt body, DataFlowInfo dataFlow) {
        // Find all variable declarations
        body.findAll(VariableDeclarator.class).forEach(var -> {
            VariableFlow flow = new VariableFlow();
            flow.variableName = var.getNameAsString();
            flow.type = var.getTypeAsString();
            
            var.getBegin().ifPresent(pos -> flow.lineNumber = pos.line);
            
            // Check if initialized from parameter
            var.getInitializer().ifPresent(init -> {
                flow.initializedFrom = init.toString();
                
                // Check if it's a transformation
                if (init instanceof MethodCallExpr) {
                    MethodCallExpr methodCall = (MethodCallExpr) init;
                    flow.transformationType = "METHOD_CALL";
                    flow.transformationMethod = methodCall.getNameAsString();
                } else if (init instanceof ObjectCreationExpr) {
                    flow.transformationType = "OBJECT_CREATION";
                    ObjectCreationExpr objCreation = (ObjectCreationExpr) init;
                    flow.transformationMethod = objCreation.getTypeAsString();
                } else if (init instanceof NameExpr) {
                    flow.transformationType = "ASSIGNMENT";
                }
            });
            
            dataFlow.variableFlows.add(flow);
        });
        
        // Find all assignments
        body.findAll(AssignExpr.class).forEach(assign -> {
            if (assign.getTarget() instanceof NameExpr) {
                String varName = ((NameExpr) assign.getTarget()).getNameAsString();
                
                VariableFlow flow = new VariableFlow();
                flow.variableName = varName;
                flow.transformationType = "REASSIGNMENT";
                flow.initializedFrom = assign.getValue().toString();
                
                assign.getBegin().ifPresent(pos -> flow.lineNumber = pos.line);
                
                dataFlow.variableFlows.add(flow);
            }
        });
    }
    
    /**
     * Extract return statements and what they return
     */
    private void extractReturnFlow(BlockStmt body, DataFlowInfo dataFlow) {
        body.findAll(ReturnStmt.class).forEach(returnStmt -> {
            returnStmt.getExpression().ifPresent(expr -> {
                ReturnFlow returnFlow = new ReturnFlow();
                returnFlow.expression = expr.toString();
                returnStmt.getBegin().ifPresent(pos -> returnFlow.lineNumber = pos.line);
                
                // Determine what type of return
                if (expr instanceof NameExpr) {
                    returnFlow.returnType = "VARIABLE";
                    returnFlow.variableName = ((NameExpr) expr).getNameAsString();
                } else if (expr instanceof MethodCallExpr) {
                    returnFlow.returnType = "METHOD_RESULT";
                    MethodCallExpr methodCall = (MethodCallExpr) expr;
                    returnFlow.methodName = methodCall.getNameAsString();
                } else if (expr instanceof ObjectCreationExpr) {
                    returnFlow.returnType = "NEW_OBJECT";
                    ObjectCreationExpr objCreation = (ObjectCreationExpr) expr;
                    returnFlow.objectType = objCreation.getTypeAsString();
                } else {
                    returnFlow.returnType = "EXPRESSION";
                }
                
                dataFlow.returnFlows.add(returnFlow);
            });
        });
    }
    
    /**
     * Extract method calls and their parameter passing
     */
    private void extractMethodCallDataFlow(BlockStmt body, DataFlowInfo dataFlow) {
        body.findAll(MethodCallExpr.class).forEach(methodCall -> {
            MethodCallFlow callFlow = new MethodCallFlow();
            callFlow.methodName = methodCall.getNameAsString();
            methodCall.getBegin().ifPresent(pos -> callFlow.lineNumber = pos.line);
            
            // Extract scope (which object the method is called on)
            methodCall.getScope().ifPresent(scope -> {
                callFlow.calledOn = scope.toString();
            });
            
            // Extract arguments passed to the method
            for (int i = 0; i < methodCall.getArguments().size(); i++) {
                Expression arg = methodCall.getArguments().get(i);
                ArgumentFlow argFlow = new ArgumentFlow();
                argFlow.position = i;
                argFlow.expression = arg.toString();
                
                if (arg instanceof NameExpr) {
                    argFlow.type = "VARIABLE";
                    argFlow.variableName = ((NameExpr) arg).getNameAsString();
                } else if (arg instanceof MethodCallExpr) {
                    argFlow.type = "METHOD_RESULT";
                    argFlow.variableName = ((MethodCallExpr) arg).getNameAsString();
                } else if (arg instanceof FieldAccessExpr) {
                    argFlow.type = "FIELD_ACCESS";
                    argFlow.variableName = arg.toString();
                } else {
                    argFlow.type = "LITERAL";
                }
                
                callFlow.arguments.add(argFlow);
            }
            
            dataFlow.methodCallFlows.add(callFlow);
        });
    }
    
    // Inner classes for data structures
    
    public static class DataFlowInfo {
        public String methodName;
        public List<ParameterInfo> parameters = new ArrayList<>();
        public List<VariableFlow> variableFlows = new ArrayList<>();
        public List<ReturnFlow> returnFlows = new ArrayList<>();
        public List<MethodCallFlow> methodCallFlows = new ArrayList<>();
    }
    
    public static class ParameterInfo {
        public String name;
        public String type;
        public boolean isAnnotated;
        public boolean isInputData;
        public List<String> annotations = new ArrayList<>();
    }
    
    public static class VariableFlow {
        public String variableName;
        public String type;
        public String initializedFrom;
        public String transformationType; // ASSIGNMENT, METHOD_CALL, OBJECT_CREATION, REASSIGNMENT
        public String transformationMethod;
        public int lineNumber;
    }
    
    public static class ReturnFlow {
        public String expression;
        public String returnType; // VARIABLE, METHOD_RESULT, NEW_OBJECT, EXPRESSION
        public String variableName;
        public String methodName;
        public String objectType;
        public int lineNumber;
    }
    
    public static class MethodCallFlow {
        public String methodName;
        public String calledOn;
        public List<ArgumentFlow> arguments = new ArrayList<>();
        public int lineNumber;
    }
    
    public static class ArgumentFlow {
        public int position;
        public String expression;
        public String type; // VARIABLE, METHOD_RESULT, FIELD_ACCESS, LITERAL
        public String variableName;
    }
}
