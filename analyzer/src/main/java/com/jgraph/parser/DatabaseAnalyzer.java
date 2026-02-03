package com.jgraph.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes database operations in Java code
 */
public class DatabaseAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAnalyzer.class);
    
    // SQL keywords for query type detection
    private static final Pattern SELECT_PATTERN = Pattern.compile("\\bSELECT\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern INSERT_PATTERN = Pattern.compile("\\bINSERT\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern UPDATE_PATTERN = Pattern.compile("\\bUPDATE\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DELETE_PATTERN = Pattern.compile("\\bDELETE\\b", Pattern.CASE_INSENSITIVE);
    
    // JPA repository method patterns
    private static final List<String> JPA_QUERY_METHODS = Arrays.asList(
        "findBy", "findAllBy", "findOneBy", "getBy", "queryBy", "searchBy", "streamBy", "countBy",
        "existsBy", "deleteBy", "removeBy"
    );
    
    // JDBC and JPA template methods
    private static final List<String> JDBC_METHODS = Arrays.asList(
        "execute", "query", "queryForObject", "queryForList", "queryForMap", "update", "batchUpdate"
    );
    
    private static final List<String> JPA_METHODS = Arrays.asList(
        "createQuery", "createNativeQuery", "createNamedQuery", "persist", "merge", "remove", "find"
    );
    
    /**
     * Check if a class is a JPA repository
     */
    public boolean isJpaRepository(ClassOrInterfaceDeclaration classDecl) {
        // Check for @Repository annotation
        boolean hasRepoAnnotation = classDecl.getAnnotations().stream()
            .anyMatch(ann -> ann.getNameAsString().equals("Repository"));
        
        // Check if extends JpaRepository or CrudRepository
        boolean extendsJpaRepo = classDecl.getExtendedTypes().stream()
            .anyMatch(ext -> {
                String extName = ext.getNameAsString();
                return extName.contains("Repository") || extName.contains("JpaRepository") || 
                       extName.contains("CrudRepository");
            });
        
        return hasRepoAnnotation || extendsJpaRepo;
    }
    
    /**
     * Extract database operations from a method
     */
    public List<DatabaseOperation> extractDatabaseOperations(MethodDeclaration method, String className) {
        List<DatabaseOperation> operations = new ArrayList<>();
        
        // Check for @Query annotation
        Optional<AnnotationExpr> queryAnn = method.getAnnotationByName("Query");
        if (queryAnn.isPresent()) {
            DatabaseOperation op = extractQueryAnnotation(method, queryAnn.get(), className);
            if (op != null) {
                operations.add(op);
            }
        }
        
        // Check for @Transactional annotation
        boolean isTransactional = method.getAnnotationByName("Transactional").isPresent();
        
        // Analyze method body for database calls
        Optional<BlockStmt> body = method.getBody();
        if (body.isPresent()) {
            operations.addAll(extractDatabaseCallsFromBody(body.get(), className, method.getNameAsString()));
        }
        
        // Mark operations as transactional
        if (isTransactional) {
            operations.forEach(op -> op.isTransactional = true);
        }
        
        // Check for derived query methods (JPA repository naming convention)
        if (isDerivedQueryMethod(method.getNameAsString())) {
            DatabaseOperation op = new DatabaseOperation();
            op.type = "SELECT";
            op.methodName = method.getNameAsString();
            op.className = className;
            op.isDerivedQuery = true;
            method.getBegin().ifPresent(pos -> op.lineNumber = pos.line);
            operations.add(op);
        }
        
        return operations;
    }
    
    /**
     * Extract query from @Query annotation
     */
    private DatabaseOperation extractQueryAnnotation(MethodDeclaration method, AnnotationExpr annotation, String className) {
        DatabaseOperation op = new DatabaseOperation();
        op.className = className;
        op.methodName = method.getNameAsString();
        method.getBegin().ifPresent(pos -> op.lineNumber = pos.line);
        
        // Extract query string from annotation
        String annotationStr = annotation.toString();
        
        // Try to extract value or query parameter
        Pattern valuePattern = Pattern.compile("value\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = valuePattern.matcher(annotationStr);
        if (matcher.find()) {
            op.query = matcher.group(1);
        } else {
            // Try without value=
            Pattern simplePattern = Pattern.compile("@Query\\(\"([^\"]+)\"\\)");
            matcher = simplePattern.matcher(annotationStr);
            if (matcher.find()) {
                op.query = matcher.group(1);
            }
        }
        
        // Detect query type
        if (op.query != null) {
            op.type = detectQueryType(op.query);
            op.isNativeQuery = annotationStr.contains("nativeQuery") && annotationStr.contains("true");
        }
        
        return op;
    }
    
    /**
     * Extract database calls from method body
     */
    private List<DatabaseOperation> extractDatabaseCallsFromBody(BlockStmt body, String className, String methodName) {
        List<DatabaseOperation> operations = new ArrayList<>();
        
        // Find all method calls
        body.findAll(MethodCallExpr.class).forEach(methodCall -> {
            String calledMethod = methodCall.getNameAsString();
            
            // Check for JDBC template methods
            if (JDBC_METHODS.contains(calledMethod)) {
                DatabaseOperation op = new DatabaseOperation();
                op.type = detectOperationType(calledMethod);
                op.methodName = methodName;
                op.className = className;
                op.dbFramework = "JDBC";
                methodCall.getBegin().ifPresent(pos -> op.lineNumber = pos.line);
                
                // Try to extract SQL from string literal argument
                op.query = extractQueryFromArguments(methodCall);
                if (op.query != null) {
                    op.type = detectQueryType(op.query);
                }
                
                operations.add(op);
            }
            
            // Check for JPA EntityManager methods
            if (JPA_METHODS.contains(calledMethod)) {
                DatabaseOperation op = new DatabaseOperation();
                op.type = detectOperationType(calledMethod);
                op.methodName = methodName;
                op.className = className;
                op.dbFramework = "JPA";
                methodCall.getBegin().ifPresent(pos -> op.lineNumber = pos.line);
                
                // Try to extract query
                op.query = extractQueryFromArguments(methodCall);
                if (op.query != null) {
                    op.type = detectQueryType(op.query);
                }
                
                operations.add(op);
            }
            
            // Check for JPA repository method calls
            methodCall.getScope().ifPresent(scope -> {
                String scopeStr = scope.toString();
                if (scopeStr.toLowerCase().contains("repository") || scopeStr.toLowerCase().contains("repo")) {
                    if (isJpaRepositoryMethod(calledMethod)) {
                        DatabaseOperation op = new DatabaseOperation();
                        op.type = detectOperationType(calledMethod);
                        op.methodName = methodName;
                        op.className = className;
                        op.repositoryMethod = scopeStr + "." + calledMethod;
                        op.dbFramework = "JPA";
                        methodCall.getBegin().ifPresent(pos -> op.lineNumber = pos.line);
                        operations.add(op);
                    }
                }
            });
        });
        
        return operations;
    }
    
    /**
     * Check if method name follows JPA derived query convention
     */
    private boolean isDerivedQueryMethod(String methodName) {
        return JPA_QUERY_METHODS.stream().anyMatch(methodName::startsWith);
    }
    
    /**
     * Check if method is a JPA repository method
     */
    private boolean isJpaRepositoryMethod(String methodName) {
        return methodName.equals("save") || methodName.equals("saveAll") ||
               methodName.equals("delete") || methodName.equals("deleteAll") ||
               methodName.equals("findById") || methodName.equals("findAll") ||
               methodName.equals("existsById") || methodName.equals("count") ||
               isDerivedQueryMethod(methodName);
    }
    
    /**
     * Detect query type from SQL string
     */
    private String detectQueryType(String query) {
        if (SELECT_PATTERN.matcher(query).find()) return "SELECT";
        if (INSERT_PATTERN.matcher(query).find()) return "INSERT";
        if (UPDATE_PATTERN.matcher(query).find()) return "UPDATE";
        if (DELETE_PATTERN.matcher(query).find()) return "DELETE";
        return "QUERY";
    }
    
    /**
     * Detect operation type from method name
     */
    private String detectOperationType(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.contains("find") || lower.contains("get") || lower.contains("query") || 
            lower.contains("search") || lower.contains("select")) {
            return "SELECT";
        }
        if (lower.contains("save") || lower.contains("insert") || lower.contains("create") || 
            lower.contains("persist")) {
            return "INSERT";
        }
        if (lower.contains("update") || lower.contains("merge")) {
            return "UPDATE";
        }
        if (lower.contains("delete") || lower.contains("remove")) {
            return "DELETE";
        }
        return "QUERY";
    }
    
    /**
     * Extract query string from method arguments
     */
    private String extractQueryFromArguments(MethodCallExpr methodCall) {
        if (!methodCall.getArguments().isEmpty()) {
            // First argument is often the query string
            var firstArg = methodCall.getArguments().get(0);
            if (firstArg instanceof StringLiteralExpr) {
                return ((StringLiteralExpr) firstArg).getValue();
            }
        }
        return null;
    }
    
    /**
     * Extract transaction information from method
     */
    public TransactionInfo extractTransactionInfo(MethodDeclaration method) {
        Optional<AnnotationExpr> transactionalAnn = method.getAnnotationByName("Transactional");
        if (!transactionalAnn.isPresent()) {
            return null;
        }
        
        TransactionInfo info = new TransactionInfo();
        info.methodName = method.getNameAsString();
        method.getBegin().ifPresent(pos -> info.lineNumber = pos.line);
        
        // Extract transaction attributes from annotation
        String annStr = transactionalAnn.get().toString();
        info.propagation = extractAttribute(annStr, "propagation");
        info.isolation = extractAttribute(annStr, "isolation");
        info.timeout = extractAttribute(annStr, "timeout");
        info.readOnly = annStr.contains("readOnly") && annStr.contains("true");
        
        return info;
    }
    
    /**
     * Extract attribute value from annotation string
     */
    private String extractAttribute(String annotationStr, String attributeName) {
        Pattern pattern = Pattern.compile(attributeName + "\\s*=\\s*([^,\\)]+)");
        Matcher matcher = pattern.matcher(annotationStr);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    // Data classes
    
    public static class DatabaseOperation {
        public String type;  // SELECT, INSERT, UPDATE, DELETE
        public String query;
        public String methodName;
        public String className;
        public String repositoryMethod;
        public int lineNumber;
        public boolean isNativeQuery;
        public boolean isDerivedQuery;
        public boolean isTransactional;
        public String dbFramework;  // JDBC, JPA, MyBatis
    }
    
    public static class TransactionInfo {
        public String methodName;
        public int lineNumber;
        public String propagation;
        public String isolation;
        public String timeout;
        public boolean readOnly;
    }
}
