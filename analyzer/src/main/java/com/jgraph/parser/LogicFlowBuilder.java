package com.jgraph.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;
import com.jgraph.model.source.CodeBlock;
import com.jgraph.model.source.LogicFlow;
import com.jgraph.model.source.LogicStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Builds high-level logic flow with natural language descriptions
 */
public class LogicFlowBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(LogicFlowBuilder.class);
    
    /**
     * Build logic flow from code blocks
     */
    public LogicFlow buildLogicFlow(MethodDeclaration method, List<CodeBlock> codeBlocks) {
        LogicFlow flow = new LogicFlow();
        List<LogicStep> steps = new ArrayList<>();
        
        int stepNumber = 1;
        
        // Analyze code blocks and create logic steps
        for (CodeBlock block : codeBlocks) {
            String operation = classifyOperation(block, method);
            String description = generateDescription(block, operation);
            
            LogicStep step = new LogicStep(stepNumber++, operation, description);
            step.setStartLine(block.getLineNumber());
            step.setEndLine(block.getLineNumber());
            
            // Add methods called
            if (block.getMethodName() != null) {
                step.addMethod(block.getMethodName());
            }
            
            steps.add(step);
        }
        
        // Merge related steps
        steps = mergeRelatedSteps(steps);
        
        // Renumber steps after merging
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStep(i + 1);
        }
        
        flow.setSteps(steps);
        
        // Generate method summary
        String summary = generateMethodSummary(method, steps);
        flow.setSummary(summary);
        
        return flow;
    }
    
    /**
     * Classify operation type based on code block
     */
    private String classifyOperation(CodeBlock block, MethodDeclaration method) {
        String type = block.getType();
        String code = block.getCode();
        String methodName = block.getMethodName();
        
        if (code == null) {
            code = "";
        }
        code = code.toLowerCase();
        
        // Check for authentication/authorization
        if (containsPattern(code, "auth", "login", "token", "session", "principal", "user")) {
            if (containsPattern(code, "check", "verify", "validate")) {
                return "VALIDATE_AUTH";
            }
            return "AUTHENTICATE";
        }
        
        // Check for validation
        if (containsPattern(code, "valid", "check", "verify", "assert")) {
            if (type.equals("CONDITIONAL")) {
                return "VALIDATE";
            }
        }
        
        // Check for database operations
        if (methodName != null) {
            String methodLower = methodName.toLowerCase();
            if (methodLower.startsWith("find") || methodLower.startsWith("get") || 
                methodLower.startsWith("select") || methodLower.contains("query")) {
                return "QUERY_DATABASE";
            } else if (methodLower.startsWith("save") || methodLower.startsWith("insert") ||
                       methodLower.startsWith("update") || methodLower.startsWith("delete")) {
                return "UPDATE_DATABASE";
            } else if (methodLower.contains("repository") || methodLower.contains("dao")) {
                return "QUERY_DATABASE";
            }
        }
        
        // Check for external service calls
        if (containsPattern(code, "resttemplate", "webclient", "httpclient", "feign", "rest")) {
            return "CALL_EXTERNAL_SERVICE";
        }
        
        // Check for data transformation
        if (containsPattern(code, "map", "convert", "transform", "dto", "todto", "fromdto")) {
            return "TRANSFORM_DATA";
        }
        
        // Check for stream operations
        if (block.getStreamOperation() != null) {
            return "PROCESS_STREAM";
        }
        
        // Check for loops
        if (type.equals("LOOP")) {
            return "ITERATE_COLLECTION";
        }
        
        // Check for conditionals
        if (type.equals("CONDITIONAL")) {
            if (containsPattern(code, "null", "empty", "blank")) {
                return "VALIDATE";
            }
            if (containsPattern(code, "permission", "role", "access")) {
                return "CHECK_PERMISSION";
            }
            return "CONDITIONAL_LOGIC";
        }
        
        // Check for error handling
        if (type.equals("TRY_CATCH")) {
            return "HANDLE_ERROR";
        }
        
        // Check for return statements
        if (type.equals("RETURN")) {
            if (containsPattern(code, "response", "responseentity", "ok", "status")) {
                return "RETURN_RESPONSE";
            }
            return "RETURN_RESULT";
        }
        
        // Check for logging
        if (containsPattern(code, "log.", "logger.", "log(")) {
            return "LOG";
        }
        
        // Default classifications
        if (type.equals("METHOD_CALL")) {
            return "CALL_METHOD";
        } else if (type.equals("VARIABLE_DECLARATION")) {
            return "DECLARE_VARIABLE";
        } else if (type.equals("ASSIGNMENT")) {
            return "ASSIGN_VALUE";
        }
        
        return "EXECUTE_LOGIC";
    }
    
    /**
     * Generate natural language description for a code block
     */
    private String generateDescription(CodeBlock block, String operation) {
        String code = block.getCode();
        String methodName = block.getMethodName();
        String condition = block.getCondition();
        
        switch (operation) {
            case "AUTHENTICATE":
                return "Authenticate user and retrieve credentials";
                
            case "VALIDATE_AUTH":
                return "Validate user authentication and authorization";
                
            case "VALIDATE":
                if (condition != null) {
                    return "Validate condition: " + simplify(condition);
                }
                return "Validate input data";
                
            case "CHECK_PERMISSION":
                if (condition != null) {
                    return "Check permission: " + simplify(condition);
                }
                return "Check user permissions";
                
            case "QUERY_DATABASE":
                if (methodName != null) {
                    return "Query database using " + methodName;
                }
                return "Fetch data from database";
                
            case "UPDATE_DATABASE":
                if (methodName != null) {
                    return "Update database using " + methodName;
                }
                return "Save data to database";
                
            case "CALL_EXTERNAL_SERVICE":
                return "Call external service or API";
                
            case "TRANSFORM_DATA":
                return "Transform data format or structure";
                
            case "PROCESS_STREAM":
                return "Process data stream: " + (block.getStreamOperation() != null ? block.getStreamOperation() : "");
                
            case "ITERATE_COLLECTION":
                return "Iterate over collection";
                
            case "CONDITIONAL_LOGIC":
                if (condition != null) {
                    return "Evaluate condition: " + simplify(condition);
                }
                return "Execute conditional logic";
                
            case "HANDLE_ERROR":
                return "Handle exceptions and errors";
                
            case "RETURN_RESPONSE":
                return "Return HTTP response to client";
                
            case "RETURN_RESULT":
                return "Return result";
                
            case "LOG":
                return "Log information";
                
            case "CALL_METHOD":
                if (methodName != null) {
                    return "Call method: " + methodName;
                }
                return "Execute method call";
                
            case "DECLARE_VARIABLE":
                return "Declare and initialize variable";
                
            case "ASSIGN_VALUE":
                return "Assign value to variable";
                
            default:
                if (code != null && !code.isEmpty()) {
                    return simplify(code);
                }
                return "Execute operation";
        }
    }
    
    /**
     * Generate method summary based on logic steps
     */
    public String generateMethodSummary(MethodDeclaration method, List<LogicStep> steps) {
        if (steps.isEmpty()) {
            return "Empty method with no operations.";
        }
        
        String methodName = method.getNameAsString();
        String returnType = method.getTypeAsString();
        
        // Determine main operation types
        Set<String> operationTypes = new HashSet<>();
        for (LogicStep step : steps) {
            operationTypes.add(step.getOperation());
        }
        
        // Identify method purpose
        String purpose = identifyPurpose(methodName, operationTypes);
        
        // Build summary
        StringBuilder summary = new StringBuilder();
        
        // Main action
        summary.append(purpose);
        
        // Key operations
        List<String> keyOps = new ArrayList<>();
        if (operationTypes.contains("AUTHENTICATE") || operationTypes.contains("VALIDATE_AUTH")) {
            keyOps.add("authenticates user");
        }
        if (operationTypes.contains("VALIDATE") || operationTypes.contains("CHECK_PERMISSION")) {
            keyOps.add("validates permissions");
        }
        if (operationTypes.contains("QUERY_DATABASE")) {
            keyOps.add("fetches from database");
        }
        if (operationTypes.contains("UPDATE_DATABASE")) {
            keyOps.add("updates database");
        }
        if (operationTypes.contains("CALL_EXTERNAL_SERVICE")) {
            keyOps.add("calls external service");
        }
        if (operationTypes.contains("TRANSFORM_DATA")) {
            keyOps.add("transforms data");
        }
        
        if (!keyOps.isEmpty()) {
            summary.append(". ");
            summary.append(capitalize(String.join(", ", keyOps)));
        }
        
        // Return information
        if (!returnType.equals("void")) {
            if (operationTypes.contains("RETURN_RESPONSE")) {
                summary.append(". Returns HTTP response");
            } else {
                summary.append(". Returns ").append(simplifyType(returnType));
            }
            
            // Add error conditions
            if (operationTypes.contains("HANDLE_ERROR") || hasErrorHandling(steps)) {
                summary.append(" or error if operation fails");
            }
        }
        
        summary.append(".");
        
        return summary.toString();
    }
    
    /**
     * Identify method purpose from name and operations
     */
    private String identifyPurpose(String methodName, Set<String> operations) {
        String lowerName = methodName.toLowerCase();
        
        // CRUD operations with more variations
        if (lowerName.startsWith("get") || lowerName.startsWith("find") || 
            lowerName.startsWith("fetch") || lowerName.startsWith("load") ||
            lowerName.startsWith("read") || lowerName.startsWith("retrieve")) {
            if (lowerName.contains("all") || lowerName.contains("list")) {
                return "Retrieves list of entities";
            } else if (lowerName.contains("by")) {
                return "Retrieves specific entity";
            }
            return "Retrieves data";
        } else if (lowerName.startsWith("create") || lowerName.startsWith("add") || 
                   lowerName.startsWith("insert") || lowerName.startsWith("save") ||
                   lowerName.startsWith("persist")) {
            return "Creates new entity";
        } else if (lowerName.startsWith("update") || lowerName.startsWith("modify") || 
                   lowerName.startsWith("change") || lowerName.startsWith("edit") ||
                   lowerName.startsWith("set")) {
            return "Updates existing entity";
        } else if (lowerName.startsWith("delete") || lowerName.startsWith("remove")) {
            return "Deletes entity";
        } else if (lowerName.startsWith("save")) {
            return "Saves entity";
        }
        
        // Other common patterns
        if (lowerName.startsWith("validate") || lowerName.startsWith("check") || lowerName.startsWith("verify")) {
            return "Validates data or conditions";
        } else if (lowerName.startsWith("process")) {
            return "Processes data";
        } else if (lowerName.startsWith("calculate") || lowerName.startsWith("compute")) {
            return "Calculates value";
        } else if (lowerName.startsWith("convert") || lowerName.startsWith("transform")) {
            return "Transforms data";
        } else if (lowerName.startsWith("send") || lowerName.startsWith("notify")) {
            return "Sends notification";
        }
        
        // Based on operations
        if (operations.contains("QUERY_DATABASE")) {
            return "Retrieves data from database";
        } else if (operations.contains("UPDATE_DATABASE")) {
            return "Updates database";
        } else if (operations.contains("CALL_EXTERNAL_SERVICE")) {
            return "Calls external service";
        }
        
        return "Performs operation";
    }
    
    /**
     * Merge related steps (e.g., validation followed by database query)
     */
    private List<LogicStep> mergeRelatedSteps(List<LogicStep> steps) {
        // For now, return as-is. Could implement smart merging later.
        return steps;
    }
    
    /**
     * Check if there's error handling in steps
     */
    private boolean hasErrorHandling(List<LogicStep> steps) {
        for (LogicStep step : steps) {
            if (step.getOperation().equals("HANDLE_ERROR") || 
                step.getDescription().toLowerCase().contains("error") ||
                step.getDescription().toLowerCase().contains("exception")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if string contains any of the patterns
     */
    private boolean containsPattern(String text, String... patterns) {
        if (text == null) {
            return false;
        }
        text = text.toLowerCase();
        for (String pattern : patterns) {
            if (text.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Simplify text for display
     */
    private String simplify(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() > 60) {
            return text.substring(0, 60) + "...";
        }
        return text;
    }
    
    /**
     * Simplify type name
     */
    private String simplifyType(String type) {
        if (type == null) {
            return "result";
        }
        // Remove package names
        int lastDot = type.lastIndexOf('.');
        if (lastDot > 0) {
            type = type.substring(lastDot + 1);
        }
        // Remove generics for simplicity
        int genericStart = type.indexOf('<');
        if (genericStart > 0) {
            type = type.substring(0, genericStart);
        }
        return type;
    }
    
    /**
     * Capitalize first letter
     */
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
