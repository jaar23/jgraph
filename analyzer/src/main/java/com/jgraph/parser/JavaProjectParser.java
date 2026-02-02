package com.jgraph.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.jgraph.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main parser for Java project analysis
 */
public class JavaProjectParser {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaProjectParser.class);
    
    private final JavaParser javaParser;
    private final AnnotationDetector annotationDetector;
    private final MethodCallAnalyzer methodCallAnalyzer;
    
    private final AnalysisResult analysisResult;
    private final Map<String, ClassOrInterfaceDeclaration> classMap = new HashMap<>();
    
    public JavaProjectParser() {
        this.javaParser = new JavaParser();
        this.annotationDetector = new AnnotationDetector();
        this.methodCallAnalyzer = new MethodCallAnalyzer();
        this.analysisResult = new AnalysisResult();
    }
    
    /**
     * Parse a Java project directory
     */
    public AnalysisResult parseProject(String projectPath) {
        logger.info("Starting analysis of project: {}", projectPath);
        
        Path path = Paths.get(projectPath);
        File projectDir = path.toFile();
        
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid project path: " + projectPath);
        }
        
        // Set project info
        ProjectInfo projectInfo = new ProjectInfo(projectDir.getName(), projectPath);
        analysisResult.setProject(projectInfo);
        
        // Find all Java files
        List<Path> javaFiles = findJavaFiles(path);
        projectInfo.setTotalFiles(javaFiles.size());
        logger.info("Found {} Java files", javaFiles.size());
        
        // First pass: Parse all files and build class map
        for (Path javaFile : javaFiles) {
            parseFile(javaFile);
        }
        
        // Second pass: Analyze method calls and build call graph
        analyzeMethodCalls();
        
        // Build call graph structure
        buildCallGraph();
        
        // Calculate statistics
        calculateStatistics();
        
        logger.info("Analysis complete");
        return analysisResult;
    }
    
    /**
     * Find all .java files in project
     */
    private List<Path> findJavaFiles(Path projectPath) {
        try (Stream<Path> paths = Files.walk(projectPath)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.toString().contains("/test/") && !p.toString().contains("\\test\\"))
                .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error finding Java files", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Parse a single Java file
     */
    private void parseFile(Path filePath) {
        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(filePath);
            
            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                CompilationUnit cu = parseResult.getResult().get();
                analyzeCompilationUnit(cu, filePath);
            } else {
                logger.warn("Failed to parse file: {}", filePath);
                parseResult.getProblems().forEach(p -> logger.warn("  Problem: {}", p.getMessage()));
            }
        } catch (IOException e) {
            logger.error("Error reading file: {}", filePath, e);
        }
    }
    
    /**
     * Analyze a compilation unit (parsed Java file)
     */
    private void analyzeCompilationUnit(CompilationUnit cu, Path filePath) {
        // Find all classes and interfaces
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String fullClassName = getFullClassName(cu, classDecl);
            classMap.put(fullClassName, classDecl);
            
            // Detect controllers (endpoints)
            if (annotationDetector.isController(classDecl)) {
                analyzeController(classDecl, cu, filePath);
            }
            
            // Detect services
            if (annotationDetector.isService(classDecl)) {
                analyzeService(classDecl, cu, filePath);
            }
            
            // Detect repositories
            if (annotationDetector.isRepository(classDecl)) {
                analyzeRepository(classDecl, cu, filePath);
            }
            
            // Detect dependencies
            analyzeDependencies(classDecl, fullClassName);
        });
    }
    
    /**
     * Analyze a controller class for endpoints
     */
    private void analyzeController(ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, Path filePath) {
        String className = getFullClassName(cu, classDecl);
        String baseMapping = annotationDetector.getRequestMapping(classDecl);
        
        // Analyze each method
        classDecl.getMethods().forEach(method -> {
            if (annotationDetector.isEndpointMethod(method)) {
                Endpoint endpoint = createEndpoint(method, className, baseMapping, filePath);
                analysisResult.getEndpoints().add(endpoint);
            }
        });
    }
    
    /**
     * Create Endpoint object from method
     */
    private Endpoint createEndpoint(MethodDeclaration method, String className, 
                                    String baseMapping, Path filePath) {
        String endpointId = "endpoint-" + analysisResult.getEndpoints().size();
        Endpoint endpoint = new Endpoint(endpointId);
        
        endpoint.setType("REST");
        endpoint.setControllerClass(className);
        endpoint.setMethodName(method.getNameAsString());
        endpoint.setHttpMethod(annotationDetector.getHttpMethod(method));
        
        // Build full path
        String methodPath = annotationDetector.getMethodPath(method);
        String fullPath = combinePaths(baseMapping, methodPath);
        endpoint.setPath(fullPath);
        
        // Extract parameters
        method.getParameters().forEach(param -> {
            Parameter parameter = new Parameter();
            parameter.setName(param.getNameAsString());
            parameter.setType(param.getTypeAsString());
            
            // Check for parameter annotations
            param.getAnnotations().forEach(ann -> {
                String annName = ann.getNameAsString();
                if (annName.equals("PathVariable") || annName.equals("RequestParam") || 
                    annName.equals("RequestBody")) {
                    parameter.setAnnotation("@" + annName);
                    parameter.setRequired(true);
                }
            });
            
            endpoint.getParameters().add(parameter);
        });
        
        // Return type
        endpoint.setReturnType(method.getTypeAsString());
        
        // Annotations
        method.getAnnotations().forEach(ann -> 
            endpoint.getAnnotations().add("@" + ann.getNameAsString())
        );
        
        // Javadoc
        method.getJavadocComment().ifPresent(javadoc -> 
            endpoint.setJavadoc(javadoc.getContent().trim())
        );
        
        // Line number and file path
        method.getBegin().ifPresent(pos -> endpoint.setLineNumber(pos.line));
        endpoint.setFilePath(filePath.toString());
        
        return endpoint;
    }
    
    /**
     * Analyze a service class
     */
    private void analyzeService(ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, Path filePath) {
        String className = getFullClassName(cu, classDecl);
        
        // Analyze each method
        classDecl.getMethods().forEach(method -> {
            if (method.isPublic()) {
                ServiceMethod serviceMethod = createServiceMethod(method, className, filePath);
                analysisResult.getServices().add(serviceMethod);
            }
        });
    }
    
    /**
     * Create ServiceMethod object
     */
    private ServiceMethod createServiceMethod(MethodDeclaration method, String className, Path filePath) {
        String serviceId = "service-" + analysisResult.getServices().size();
        ServiceMethod serviceMethod = new ServiceMethod(serviceId);
        
        serviceMethod.setClassName(className);
        serviceMethod.setMethodName(method.getNameAsString());
        
        // Parameters
        method.getParameters().forEach(param -> 
            serviceMethod.getParameters().add(param.getTypeAsString() + " " + param.getNameAsString())
        );
        
        // Return type
        serviceMethod.setReturnType(method.getTypeAsString());
        
        // Annotations
        method.getAnnotations().forEach(ann -> 
            serviceMethod.getAnnotations().add("@" + ann.getNameAsString())
        );
        
        // Javadoc
        method.getJavadocComment().ifPresent(javadoc -> 
            serviceMethod.setJavadoc(javadoc.getContent().trim())
        );
        
        // Line number and file path
        method.getBegin().ifPresent(pos -> serviceMethod.setLineNumber(pos.line));
        serviceMethod.setFilePath(filePath.toString());
        
        return serviceMethod;
    }
    
    /**
     * Analyze a repository class/interface
     */
    private void analyzeRepository(ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, Path filePath) {
        String className = getFullClassName(cu, classDecl);
        
        // Get extended types (e.g., JpaRepository<User, Long>)
        String extendedType = "";
        if (!classDecl.getExtendedTypes().isEmpty()) {
            extendedType = classDecl.getExtendedTypes().get(0).asString();
        }
        
        // Analyze each method
        final String finalClassName = className;
        final String finalExtendedType = extendedType;
        final Path finalFilePath = filePath;
        classDecl.getMethods().forEach(method -> {
            RepositoryMethod repoMethod = createRepositoryMethod(method, finalClassName, finalExtendedType, finalFilePath);
            analysisResult.getRepositories().add(repoMethod);
        });
    }
    
    /**
     * Create RepositoryMethod object
     */
    private RepositoryMethod createRepositoryMethod(MethodDeclaration method, String className, 
                                                     String extendedType, Path filePath) {
        String repoId = "repo-" + analysisResult.getRepositories().size();
        RepositoryMethod repoMethod = new RepositoryMethod(repoId);
        
        repoMethod.setClassName(className);
        repoMethod.setInterfaceName(extendedType);
        repoMethod.setMethodName(method.getNameAsString());
        
        // Parameters
        method.getParameters().forEach(param -> 
            repoMethod.getParameters().add(param.getTypeAsString() + " " + param.getNameAsString())
        );
        
        // Return type
        repoMethod.setReturnType(method.getTypeAsString());
        
        // Annotations
        method.getAnnotations().forEach(ann -> 
            repoMethod.getAnnotations().add("@" + ann.getNameAsString())
        );
        
        // Javadoc
        method.getJavadocComment().ifPresent(javadoc -> 
            repoMethod.setJavadoc(javadoc.getContent().trim())
        );
        
        // Line number and file path
        method.getBegin().ifPresent(pos -> repoMethod.setLineNumber(pos.line));
        repoMethod.setFilePath(filePath.toString());
        
        return repoMethod;
    }
    
    /**
     * Analyze dependency injections in a class
     */
    private void analyzeDependencies(ClassOrInterfaceDeclaration classDecl, String className) {
        // Find @Autowired fields
        classDecl.getFields().forEach(field -> {
            field.getAnnotations().forEach(ann -> {
                String annName = ann.getNameAsString();
                if (annName.equals("Autowired") || annName.equals("Inject") || annName.equals("Resource")) {
                    field.getVariables().forEach(var -> {
                        Dependency dep = new Dependency();
                        dep.setFrom(className);
                        dep.setTo(var.getTypeAsString());
                        dep.setType("FIELD_INJECTION");
                        dep.setAnnotation("@" + annName);
                        dep.setFieldName(var.getNameAsString());
                        analysisResult.getDependencies().add(dep);
                    });
                }
            });
        });
    }
    
    /**
     * Analyze method calls (second pass)
     */
    private void analyzeMethodCalls() {
        // Analyze calls in endpoints
        for (Endpoint endpoint : analysisResult.getEndpoints()) {
            List<String> calls = methodCallAnalyzer.extractMethodCalls(endpoint, classMap);
            endpoint.setCallChain(calls);
        }
        
        // Analyze calls in services
        for (ServiceMethod service : analysisResult.getServices()) {
            List<String> calls = methodCallAnalyzer.extractMethodCalls(service, classMap);
            service.setCalls(calls);
        }
    }
    
    /**
     * Build call graph nodes and edges
     */
    private void buildCallGraph() {
        CallGraph callGraph = analysisResult.getCallGraph();
        
        // Add endpoint nodes
        for (Endpoint endpoint : analysisResult.getEndpoints()) {
            GraphNode node = new GraphNode(endpoint.getId(), "endpoint", 
                endpoint.getHttpMethod() + " " + endpoint.getPath());
            node.setClassName(endpoint.getControllerClass());
            node.setMethodName(endpoint.getMethodName());
            callGraph.addNode(node);
        }
        
        // Add service nodes
        for (ServiceMethod service : analysisResult.getServices()) {
            GraphNode node = new GraphNode(service.getId(), "service", 
                service.getClassName() + "." + service.getMethodName());
            node.setClassName(service.getClassName());
            node.setMethodName(service.getMethodName());
            callGraph.addNode(node);
        }
        
        // Add repository nodes
        for (RepositoryMethod repo : analysisResult.getRepositories()) {
            GraphNode node = new GraphNode(repo.getId(), "repository", 
                repo.getClassName() + "." + repo.getMethodName());
            node.setClassName(repo.getClassName());
            node.setMethodName(repo.getMethodName());
            callGraph.addNode(node);
        }
        
        // Add edges from endpoints to services
        for (Endpoint endpoint : analysisResult.getEndpoints()) {
            for (String call : endpoint.getCallChain()) {
                // Find matching service
                for (ServiceMethod service : analysisResult.getServices()) {
                    String serviceCall = service.getClassName() + "." + service.getMethodName();
                    if (call.contains(serviceCall) || call.contains(service.getMethodName())) {
                        GraphEdge edge = new GraphEdge(endpoint.getId(), service.getId(), 
                            "method_call", "calls");
                        callGraph.addEdge(edge);
                        service.getCalledBy().add(endpoint.getControllerClass() + "." + endpoint.getMethodName());
                    }
                }
            }
        }
        
        // Add edges from services to repositories
        for (ServiceMethod service : analysisResult.getServices()) {
            for (String call : service.getCalls()) {
                for (RepositoryMethod repo : analysisResult.getRepositories()) {
                    String repoCall = repo.getClassName() + "." + repo.getMethodName();
                    if (call.contains(repoCall) || call.contains(repo.getMethodName())) {
                        GraphEdge edge = new GraphEdge(service.getId(), repo.getId(), 
                            "method_call", "calls");
                        callGraph.addEdge(edge);
                        repo.getCalledBy().add(service.getClassName() + "." + service.getMethodName());
                    }
                }
            }
        }
    }
    
    /**
     * Calculate statistics
     */
    private void calculateStatistics() {
        Statistics stats = analysisResult.getStatistics();
        stats.setTotalEndpoints(analysisResult.getEndpoints().size());
        stats.setTotalServices(analysisResult.getServices().size());
        stats.setTotalRepositories(analysisResult.getRepositories().size());
        // TODO: Implement circular dependency detection
        stats.setCircularDependencies(0);
        // TODO: Calculate max call depth
        stats.setMaxCallDepth(0);
    }
    
    /**
     * Get full class name including package
     */
    private String getFullClassName(CompilationUnit cu, ClassOrInterfaceDeclaration classDecl) {
        String packageName = cu.getPackageDeclaration()
            .map(pd -> pd.getNameAsString())
            .orElse("");
        
        String className = classDecl.getNameAsString();
        return packageName.isEmpty() ? className : packageName + "." + className;
    }
    
    /**
     * Combine base path and method path
     */
    private String combinePaths(String base, String method) {
        if (base == null) base = "";
        if (method == null) method = "";
        
        if (base.isEmpty()) return method;
        if (method.isEmpty()) return base;
        
        if (!base.startsWith("/")) base = "/" + base;
        if (!method.startsWith("/")) method = "/" + method;
        
        return base + method;
    }
}
