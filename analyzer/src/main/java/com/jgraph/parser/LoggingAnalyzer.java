package com.jgraph.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.jgraph.model.LogLevel;
import com.jgraph.model.LogStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Analyzes logging statements in Java code
 * Supports: SLF4J, Log4j2, Logback, java.util.logging, Apache Commons Logging
 */
public class LoggingAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAnalyzer.class);
    
    // Logger variable names
    private static final List<String> LOGGER_VAR_NAMES = Arrays.asList(
        "log", "logger", "LOG", "LOGGER", "_log", "_logger"
    );
    
    // Log method names by level
    private static final List<String> TRACE_METHODS = Arrays.asList("trace");
    private static final List<String> DEBUG_METHODS = Arrays.asList("debug");
    private static final List<String> INFO_METHODS = Arrays.asList("info");
    private static final List<String> WARN_METHODS = Arrays.asList("warn", "warning");
    private static final List<String> ERROR_METHODS = Arrays.asList("error", "severe", "fatal");
    
    private String detectedFramework = "UNKNOWN";
    
    /**
     * Detect logging framework from imports
     */
    public String detectFramework(CompilationUnit cu) {
        for (ImportDeclaration imp : cu.getImports()) {
            String importName = imp.getNameAsString();
            
            if (importName.startsWith("org.slf4j")) {
                detectedFramework = "SLF4J";
                return detectedFramework;
            } else if (importName.startsWith("org.apache.logging.log4j")) {
                detectedFramework = "Log4j2";
                return detectedFramework;
            } else if (importName.startsWith("ch.qos.logback")) {
                detectedFramework = "Logback";
                return detectedFramework;
            } else if (importName.startsWith("java.util.logging")) {
                detectedFramework = "JUL";
                return detectedFramework;
            } else if (importName.startsWith("org.apache.commons.logging")) {
                detectedFramework = "Commons";
                return detectedFramework;
            }
        }
        
        detectedFramework = "UNKNOWN";
        return detectedFramework;
    }
    
    /**
     * Extract all log statements from a method
     */
    public List<LogStatement> extractLogStatements(MethodDeclaration method, String methodId, String filePath) {
        List<LogStatement> logStatements = new ArrayList<>();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return logStatements;
        }
        
        // Find all method call expressions
        List<MethodCallExpr> methodCalls = body.get().findAll(MethodCallExpr.class);
        
        int logIndex = 0;
        for (MethodCallExpr methodCall : methodCalls) {
            Optional<LogStatement> logOpt = parseLogStatement(methodCall, methodId, filePath, logIndex);
            if (logOpt.isPresent()) {
                logStatements.add(logOpt.get());
                logIndex++;
            }
        }
        
        return logStatements;
    }
    
    /**
     * Parse a method call to check if it's a log statement
     */
    private Optional<LogStatement> parseLogStatement(MethodCallExpr methodCall, String methodId, 
                                                      String filePath, int index) {
        // Check if the scope is a logger
        Optional<Expression> scopeOpt = methodCall.getScope();
        if (!scopeOpt.isPresent()) {
            return Optional.empty();
        }
        
        String scope = scopeOpt.get().toString();
        if (!isLoggerVariable(scope)) {
            return Optional.empty();
        }
        
        // Get method name
        String methodName = methodCall.getNameAsString();
        
        // Determine log level
        Optional<LogLevel> levelOpt = determineLogLevel(methodName);
        if (!levelOpt.isPresent()) {
            return Optional.empty();
        }
        
        // Create log statement
        LogStatement logStmt = new LogStatement();
        logStmt.setId("log-" + methodId + "-" + index);
        logStmt.setLevel(levelOpt.get());
        logStmt.setMethodId(methodId);
        logStmt.setFilePath(filePath);
        logStmt.setLoggerFramework(detectedFramework);
        
        // Get line number
        int lineNumber = methodCall.getBegin().map(pos -> pos.line).orElse(0);
        logStmt.setLineNumber(lineNumber);
        
        // Extract message and variables
        extractMessageAndVariables(methodCall, logStmt);
        
        // Check for exception logging
        checkExceptionLogging(methodCall, logStmt);
        
        return Optional.of(logStmt);
    }
    
    /**
     * Check if variable name is a logger
     */
    private boolean isLoggerVariable(String varName) {
        return LOGGER_VAR_NAMES.stream()
            .anyMatch(loggerName -> varName.equals(loggerName) || varName.endsWith("." + loggerName));
    }
    
    /**
     * Determine log level from method name
     */
    private Optional<LogLevel> determineLogLevel(String methodName) {
        if (TRACE_METHODS.contains(methodName)) {
            return Optional.of(LogLevel.TRACE);
        } else if (DEBUG_METHODS.contains(methodName)) {
            return Optional.of(LogLevel.DEBUG);
        } else if (INFO_METHODS.contains(methodName)) {
            return Optional.of(LogLevel.INFO);
        } else if (WARN_METHODS.contains(methodName)) {
            return Optional.of(LogLevel.WARN);
        } else if (ERROR_METHODS.contains(methodName)) {
            return Optional.of(LogLevel.ERROR);
        }
        return Optional.empty();
    }
    
    /**
     * Extract log message and variables from method call arguments
     */
    private void extractMessageAndVariables(MethodCallExpr methodCall, LogStatement logStmt) {
        List<Expression> args = methodCall.getArguments();
        if (args.isEmpty()) {
            return;
        }
        
        // First argument is usually the message
        Expression firstArg = args.get(0);
        
        // Handle java.util.logging (first arg is Level)
        if (detectedFramework.equals("JUL") && args.size() > 1) {
            firstArg = args.get(1);
        }
        
        String message = extractMessage(firstArg);
        logStmt.setMessage(message);
        
        // Remaining arguments are variables
        List<String> variables = new ArrayList<>();
        int startIndex = (detectedFramework.equals("JUL") && args.size() > 1) ? 2 : 1;
        
        for (int i = startIndex; i < args.size(); i++) {
            Expression arg = args.get(i);
            // Skip if it's an exception (handled separately)
            if (!isExceptionType(arg)) {
                variables.add(arg.toString());
            }
        }
        
        logStmt.setVariables(variables);
    }
    
    /**
     * Extract message string from expression
     */
    private String extractMessage(Expression expr) {
        if (expr instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) expr).getValue();
        }
        return expr.toString();
    }
    
    /**
     * Check if expression is an exception type
     */
    private boolean isExceptionType(Expression expr) {
        String exprStr = expr.toString();
        return exprStr.contains("Exception") || exprStr.contains("Throwable") || 
               exprStr.contains("Error") || exprStr.equals("e") || 
               exprStr.equals("ex") || exprStr.equals("t");
    }
    
    /**
     * Check if logging an exception
     */
    private void checkExceptionLogging(MethodCallExpr methodCall, LogStatement logStmt) {
        List<Expression> args = methodCall.getArguments();
        
        // Check last argument for exception
        if (!args.isEmpty()) {
            Expression lastArg = args.get(args.size() - 1);
            if (isExceptionType(lastArg)) {
                logStmt.setHasException(true);
                logStmt.setExceptionVariable(lastArg.toString());
            }
        }
    }
    
    /**
     * Extract MDC keys from method (looks for MDC.put calls)
     */
    public List<String> extractMDCKeys(MethodDeclaration method) {
        List<String> mdcKeys = new ArrayList<>();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return mdcKeys;
        }
        
        // Find MDC.put calls
        body.get().findAll(MethodCallExpr.class).forEach(methodCall -> {
            if (methodCall.getScope().isPresent() && 
                methodCall.getScope().get().toString().equals("MDC") &&
                methodCall.getNameAsString().equals("put")) {
                
                List<Expression> args = methodCall.getArguments();
                if (!args.isEmpty() && args.get(0) instanceof StringLiteralExpr) {
                    String key = ((StringLiteralExpr) args.get(0)).getValue();
                    mdcKeys.add(key);
                }
            }
        });
        
        return mdcKeys;
    }
}
