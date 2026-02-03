package com.jgraph.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzes exception handling and error propagation in Java code
 */
public class ExceptionAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAnalyzer.class);
    
    /**
     * Extract all try-catch blocks from a method
     */
    public List<TryCatchInfo> extractTryCatchBlocks(MethodDeclaration method) {
        List<TryCatchInfo> tryCatchBlocks = new ArrayList<>();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return tryCatchBlocks;
        }
        
        // Find all try statements
        body.get().findAll(TryStmt.class).forEach(tryStmt -> {
            TryCatchInfo info = new TryCatchInfo();
            
            // Line number
            tryStmt.getBegin().ifPresent(pos -> info.startLine = pos.line);
            tryStmt.getEnd().ifPresent(pos -> info.endLine = pos.line);
            
            // Caught exception types
            for (CatchClause catchClause : tryStmt.getCatchClauses()) {
                String exceptionType = catchClause.getParameter().getTypeAsString();
                info.caughtTypes.add(exceptionType);
                
                // Analyze what happens in catch block
                BlockStmt catchBody = catchClause.getBody();
                info.hasRethrow = !catchBody.findAll(ThrowStmt.class).isEmpty();
                info.hasLogging = hasLoggingInBlock(catchBody);
            }
            
            // Check for finally block
            info.hasFinally = tryStmt.getFinallyBlock().isPresent();
            
            tryCatchBlocks.add(info);
        });
        
        return tryCatchBlocks;
    }
    
    /**
     * Extract all throw statements from a method
     */
    public List<ThrowInfo> extractThrowStatements(MethodDeclaration method) {
        List<ThrowInfo> throwStatements = new ArrayList<>();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return throwStatements;
        }
        
        // Find all throw statements
        body.get().findAll(ThrowStmt.class).forEach(throwStmt -> {
            ThrowInfo info = new ThrowInfo();
            
            // Line number
            throwStmt.getBegin().ifPresent(pos -> info.lineNumber = pos.line);
            
            // Expression being thrown
            String expression = throwStmt.getExpression().toString();
            info.expression = expression;
            
            // Try to extract exception type
            if (expression.startsWith("new ")) {
                int endIndex = expression.indexOf('(');
                if (endIndex > 4) {
                    info.exceptionType = expression.substring(4, endIndex).trim();
                }
            }
            
            // Check if it's inside a catch block (rethrow)
            info.isRethrow = throwStmt.findAncestor(CatchClause.class).isPresent();
            
            throwStatements.add(info);
        });
        
        return throwStatements;
    }
    
    /**
     * Extract method throws declarations
     */
    public List<String> extractThrowsDeclarations(MethodDeclaration method) {
        List<String> throwsTypes = new ArrayList<>();
        
        for (ReferenceType thrownType : method.getThrownExceptions()) {
            throwsTypes.add(thrownType.asString());
        }
        
        return throwsTypes;
    }
    
    /**
     * Find global exception handlers (@ControllerAdvice, @ExceptionHandler)
     */
    public List<ExceptionHandlerInfo> findExceptionHandlers(List<CompilationUnit> compilationUnits) {
        List<ExceptionHandlerInfo> handlers = new ArrayList<>();
        
        for (CompilationUnit cu : compilationUnits) {
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                // Check if class is annotated with @ControllerAdvice
                boolean isControllerAdvice = classDecl.getAnnotations().stream()
                    .anyMatch(ann -> ann.getNameAsString().equals("ControllerAdvice") || 
                                   ann.getNameAsString().equals("RestControllerAdvice"));
                
                if (isControllerAdvice) {
                    // Find methods annotated with @ExceptionHandler
                    classDecl.getMethods().forEach(method -> {
                        Optional<AnnotationExpr> handlerAnn = method.getAnnotationByName("ExceptionHandler");
                        if (handlerAnn.isPresent()) {
                            ExceptionHandlerInfo info = new ExceptionHandlerInfo();
                            info.handlerClass = getFullClassName(cu, classDecl);
                            info.handlerMethod = method.getNameAsString();
                            info.returnType = method.getTypeAsString();
                            method.getBegin().ifPresent(pos -> info.lineNumber = pos.line);
                            
                            // Extract handled exception types from annotation or method parameters
                            method.getParameters().forEach(param -> {
                                String paramType = param.getTypeAsString();
                                if (paramType.contains("Exception") || paramType.contains("Throwable")) {
                                    info.handledExceptionTypes.add(paramType);
                                }
                            });
                            
                            handlers.add(info);
                        }
                    });
                }
            });
        }
        
        return handlers;
    }
    
    /**
     * Check if a block contains logging statements
     */
    private boolean hasLoggingInBlock(BlockStmt block) {
        // Simple heuristic: look for common logger method calls
        return block.toString().contains("log.") || 
               block.toString().contains("logger.") ||
               block.toString().contains("LOG.") ||
               block.toString().contains("LOGGER.");
    }
    
    /**
     * Get full class name from compilation unit and class declaration
     */
    private String getFullClassName(CompilationUnit cu, ClassOrInterfaceDeclaration classDecl) {
        String packageName = cu.getPackageDeclaration()
            .map(pd -> pd.getNameAsString())
            .orElse("");
        String className = classDecl.getNameAsString();
        return packageName.isEmpty() ? className : packageName + "." + className;
    }
    
    // Inner classes for structured data
    
    public static class TryCatchInfo {
        public int startLine;
        public int endLine;
        public List<String> caughtTypes = new ArrayList<>();
        public boolean hasFinally;
        public boolean hasRethrow;
        public boolean hasLogging;
    }
    
    public static class ThrowInfo {
        public int lineNumber;
        public String exceptionType;
        public String expression;
        public boolean isRethrow;
    }
    
    public static class ExceptionHandlerInfo {
        public String handlerClass;
        public String handlerMethod;
        public String returnType;
        public int lineNumber;
        public List<String> handledExceptionTypes = new ArrayList<>();
    }
}
