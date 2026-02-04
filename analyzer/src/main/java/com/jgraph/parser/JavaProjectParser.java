package com.jgraph.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
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
    private MethodCallAnalyzer methodCallAnalyzer;
    private final LoggingAnalyzer loggingAnalyzer;
    private final ControlFlowAnalyzer controlFlowAnalyzer;
    private final ExceptionAnalyzer exceptionAnalyzer;
    private final DatabaseAnalyzer databaseAnalyzer;
    private final ExternalCallAnalyzer externalCallAnalyzer;
    private final DataFlowAnalyzer dataFlowAnalyzer;
    private TypeResolver typeResolver;
    
    // Source code extraction
    private final SourceCodeExtractor sourceCodeExtractor;
    private final com.jgraph.model.source.SourceMap sourceMap;
    private SourceCodeExtractor.DetailLevel sourceDetailLevel = SourceCodeExtractor.DetailLevel.STANDARD;
    private boolean extractSource = true;
    
    private final AnalysisResult analysisResult;
    private final Map<String, ClassOrInterfaceDeclaration> classMap = new HashMap<>();
    private final Map<String, CompilationUnit> compilationUnitMap = new HashMap<>();
    private final List<CompilationUnit> allCompilationUnits = new ArrayList<>();
    
    public JavaProjectParser() {
        // Configure JavaParser to support Java 21 features (including pattern matching, switch expressions, etc.)
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        
        this.javaParser = new JavaParser(parserConfiguration);
        this.annotationDetector = new AnnotationDetector();
        this.loggingAnalyzer = new LoggingAnalyzer();
        this.controlFlowAnalyzer = new ControlFlowAnalyzer();
        this.exceptionAnalyzer = new ExceptionAnalyzer();
        this.databaseAnalyzer = new DatabaseAnalyzer();
        this.externalCallAnalyzer = new ExternalCallAnalyzer();
        this.dataFlowAnalyzer = new DataFlowAnalyzer();
        this.sourceCodeExtractor = new SourceCodeExtractor();
        this.sourceMap = new com.jgraph.model.source.SourceMap();
        this.analysisResult = new AnalysisResult();
    }
    
    /**
     * Set source extraction detail level
     */
    public void setSourceDetailLevel(SourceCodeExtractor.DetailLevel level) {
        this.sourceDetailLevel = level;
    }
    
    /**
     * Enable or disable source extraction
     */
    public void setExtractSource(boolean extractSource) {
        this.extractSource = extractSource;
    }
    
    /**
     * Get the source map
     */
    public com.jgraph.model.source.SourceMap getSourceMap() {
        return this.sourceMap;
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
        
        // Set source map project name and detail level
        sourceMap.setProjectName(projectDir.getName());
        sourceMap.setDetailLevel(sourceDetailLevel.name());
        
        // Detect multi-module structure
        List<ModuleInfo> modules = MultiModuleDetector.detectModules(projectPath);
        projectInfo.setModules(modules);
        
        // Set build system
        if (!modules.isEmpty()) {
            ModuleInfo.BuildSystem buildSystem = modules.get(0).getBuildSystem();
            if (buildSystem != null) {
                projectInfo.setBuildSystem(buildSystem.name());
            }
        }
        
        logger.info("Detected {} module(s)", modules.size());
        for (ModuleInfo module : modules) {
            logger.info("  Module: {} ({}) at {}", module.getName(), module.getType(), module.getRelativePath());
        }
        
        // Find all Java files (across all modules)
        List<Path> javaFiles = findJavaFilesInModules(path, modules);
        projectInfo.setTotalFiles(javaFiles.size());
        logger.info("Found {} Java files across all modules", javaFiles.size());
        
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
     * Find all .java files across multiple modules
     */
    private List<Path> findJavaFilesInModules(Path rootPath, List<ModuleInfo> modules) {
        List<Path> allJavaFiles = new ArrayList<>();
        
        for (ModuleInfo module : modules) {
            // Skip parent/aggregator modules that typically don't have source code
            if (module.getType() == ModuleInfo.ModuleType.PARENT) {
                logger.debug("Skipping parent module: {}", module.getName());
                continue;
            }
            
            Path modulePath = Paths.get(module.getPath());
            List<String> sourceDirs = module.getSourceDirectories();
            
            if (sourceDirs.isEmpty()) {
                // Fallback to scanning entire module directory
                logger.debug("Scanning entire module directory: {}", modulePath);
                List<Path> moduleFiles = findJavaFiles(modulePath);
                allJavaFiles.addAll(moduleFiles);
                module.setTotalFiles(moduleFiles.size());
            } else {
                // Scan specific source directories
                int moduleFileCount = 0;
                for (String sourceDir : sourceDirs) {
                    Path sourcePath = modulePath.resolve(sourceDir);
                    if (Files.exists(sourcePath) && Files.isDirectory(sourcePath)) {
                        logger.debug("Scanning source directory: {}", sourcePath);
                        List<Path> dirFiles = findJavaFiles(sourcePath);
                        allJavaFiles.addAll(dirFiles);
                        moduleFileCount += dirFiles.size();
                    }
                }
                module.setTotalFiles(moduleFileCount);
                logger.info("  Module {} has {} Java files", module.getName(), moduleFileCount);
            }
        }
        
        return allJavaFiles;
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
        // Store for later exception handler analysis
        allCompilationUnits.add(cu);
        
        // Detect logging framework
        loggingAnalyzer.detectFramework(cu);
        
        // Find all classes and interfaces
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String fullClassName = getFullClassName(cu, classDecl);
            classMap.put(fullClassName, classDecl);
            compilationUnitMap.put(fullClassName, cu);
            
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
        
        // Set fully qualified type
        endpoint.setFullyQualifiedType(className);
        
        // Analyze control flow
        ControlFlow controlFlow = controlFlowAnalyzer.analyze(method);
        endpoint.setControlFlow(controlFlow);
        
        // Extract log statements
        List<LogStatement> logs = loggingAnalyzer.extractLogStatements(method, endpointId, filePath.toString());
        analysisResult.getLogStatements().addAll(logs);
        
        // Extract exception handling
        List<ExceptionAnalyzer.TryCatchInfo> tryCatchInfos = exceptionAnalyzer.extractTryCatchBlocks(method);
        for (ExceptionAnalyzer.TryCatchInfo info : tryCatchInfos) {
            TryCatchBlock block = new TryCatchBlock(
                className, method.getNameAsString(), info.caughtTypes,
                info.hasFinally, info.hasRethrow, null, // wrapsException not tracked by analyzer
                filePath.toString(), info.startLine
            );
            analysisResult.getTryCatchBlocks().add(block);
        }
        
        // Extract database operations (endpoints typically don't have direct DB access, but check anyway)
        List<DatabaseAnalyzer.DatabaseOperation> dbOps = databaseAnalyzer.extractDatabaseOperations(method, className);
        for (DatabaseAnalyzer.DatabaseOperation dbOp : dbOps) {
            DatabaseOperation operation = convertDatabaseOperation(dbOp, filePath.toString());
            analysisResult.getDatabaseOperations().add(operation);
        }
        
        // Extract external calls
        List<ExternalCallAnalyzer.ExternalCall> externalCallInfos = externalCallAnalyzer.extractExternalCalls(method, className);
        for (ExternalCallAnalyzer.ExternalCall callInfo : externalCallInfos) {
            ExternalCall call = convertExternalCall(callInfo, filePath.toString());
            analysisResult.getExternalCalls().add(call);
        }
        
        // Extract source code representation
        if (extractSource) {
            extractMethodSource(method, endpointId, endpoint);
        }
        
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
        
        // Set fully qualified type
        serviceMethod.setFullyQualifiedType(className);
        
        // Analyze control flow
        ControlFlow controlFlow = controlFlowAnalyzer.analyze(method);
        serviceMethod.setControlFlow(controlFlow);
        
        // Extract log statements
        List<LogStatement> logs = loggingAnalyzer.extractLogStatements(method, serviceId, filePath.toString());
        analysisResult.getLogStatements().addAll(logs);
        
        // Extract exception handling
        List<ExceptionAnalyzer.TryCatchInfo> tryCatchInfos = exceptionAnalyzer.extractTryCatchBlocks(method);
        for (ExceptionAnalyzer.TryCatchInfo info : tryCatchInfos) {
            TryCatchBlock block = new TryCatchBlock(
                className, method.getNameAsString(), info.caughtTypes,
                info.hasFinally, info.hasRethrow, null, // wrapsException not tracked by analyzer
                filePath.toString(), info.startLine
            );
            analysisResult.getTryCatchBlocks().add(block);
        }
        
        // Extract database operations
        List<DatabaseAnalyzer.DatabaseOperation> dbOps = databaseAnalyzer.extractDatabaseOperations(method, className);
        for (DatabaseAnalyzer.DatabaseOperation dbOp : dbOps) {
            DatabaseOperation operation = convertDatabaseOperation(dbOp, filePath.toString());
            analysisResult.getDatabaseOperations().add(operation);
        }
        
        // Extract transaction info
        DatabaseAnalyzer.TransactionInfo txInfo = databaseAnalyzer.extractTransactionInfo(method);
        if (txInfo != null) {
            // Parse timeout from string to int
            int timeoutVal = -1;
            if (txInfo.timeout != null) {
                try {
                    timeoutVal = Integer.parseInt(txInfo.timeout);
                } catch (NumberFormatException e) {
                    // Keep default -1 if parsing fails
                }
            }
            
            Transaction transaction = new Transaction(
                className, txInfo.methodName,
                txInfo.propagation, txInfo.isolation,
                txInfo.readOnly, timeoutVal,
                null, null, // rollbackFor and noRollbackFor not tracked by analyzer
                filePath.toString(), txInfo.lineNumber
            );
            analysisResult.getTransactions().add(transaction);
        }
        
        // Extract external calls
        List<ExternalCallAnalyzer.ExternalCall> externalCallInfos = externalCallAnalyzer.extractExternalCalls(method, className);
        for (ExternalCallAnalyzer.ExternalCall callInfo : externalCallInfos) {
            ExternalCall call = convertExternalCall(callInfo, filePath.toString());
            analysisResult.getExternalCalls().add(call);
        }
        
        // Extract data flow
        DataFlowAnalyzer.DataFlowInfo dataFlowInfo = dataFlowAnalyzer.extractDataFlow(method);
        DataFlow dataFlow = convertDataFlow(dataFlowInfo, serviceId, className, filePath.toString());
        analysisResult.getDataFlows().add(dataFlow);
        
        // Extract source code representation
        if (extractSource) {
            extractMethodSource(method, serviceId, serviceMethod);
        }
        
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
        
        // Extract source code representation
        if (extractSource) {
            extractMethodSource(method, repoId, repoMethod);
        }
        
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
        // Initialize TypeResolver and MethodCallAnalyzer after all classes are parsed
        this.typeResolver = new TypeResolver(classMap);
        this.methodCallAnalyzer = new MethodCallAnalyzer(typeResolver);
        
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
        
        // Extract global exception handlers
        extractGlobalExceptionHandlers();
    }
    
    /**
     * Extract global exception handlers (@ControllerAdvice, @ExceptionHandler)
     */
    private void extractGlobalExceptionHandlers() {
        List<ExceptionAnalyzer.ExceptionHandlerInfo> handlers = 
            exceptionAnalyzer.findExceptionHandlers(allCompilationUnits);
        
        for (ExceptionAnalyzer.ExceptionHandlerInfo info : handlers) {
            ExceptionHandler handler = new ExceptionHandler(
                info.handlerClass, info.handlerMethod,
                info.handledExceptionTypes, false, // isGlobal not tracked, default to false
                null, // responseStatus not tracked
                null, info.lineNumber // sourceFile not tracked
            );
            analysisResult.getExceptionHandlers().add(handler);
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
        
        // Add log statement nodes
        for (LogStatement log : analysisResult.getLogStatements()) {
            GraphNode node = new GraphNode(log.getId(), "log", 
                log.getLevel() + ": " + truncate(log.getMessage(), 40));
            node.setLogLevel(log.getLevel().toString());
            node.setLogMessage(log.getMessage());
            callGraph.addNode(node);
        }
        
        // Add exception nodes (try-catch blocks)
        int exceptionNodeCounter = 0;
        for (TryCatchBlock block : analysisResult.getTryCatchBlocks()) {
            String nodeId = "exception-" + exceptionNodeCounter++;
            String label = "Try-Catch: " + String.join(", ", block.getCaughtExceptions());
            GraphNode node = new GraphNode(nodeId, "exception", truncate(label, 50));
            callGraph.addNode(node);
            
            // Find the parent method and create edge
            String parentMethod = block.getParentClassName() + "." + block.getParentMethodName();
            for (Endpoint endpoint : analysisResult.getEndpoints()) {
                String endpointMethod = endpoint.getControllerClass() + "." + endpoint.getMethodName();
                if (endpointMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(endpoint.getId(), nodeId, "exception", "handles");
                    callGraph.addEdge(edge);
                    break;
                }
            }
            for (ServiceMethod service : analysisResult.getServices()) {
                String serviceMethod = service.getClassName() + "." + service.getMethodName();
                if (serviceMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(service.getId(), nodeId, "exception", "handles");
                    callGraph.addEdge(edge);
                    break;
                }
            }
        }
        
        // Add exception handler nodes
        for (ExceptionHandler handler : analysisResult.getExceptionHandlers()) {
            String nodeId = "handler-" + handler.getClassName() + "-" + handler.getMethodName();
            String label = "Handler: " + handler.getMethodName();
            GraphNode node = new GraphNode(nodeId, "exception", truncate(label, 50));
            callGraph.addNode(node);
        }
        
        // Add database operation nodes
        int dbNodeCounter = 0;
        for (DatabaseOperation dbOp : analysisResult.getDatabaseOperations()) {
            String nodeId = "db-" + dbNodeCounter++;
            String label = dbOp.getOperationType() + ": " + (dbOp.getQuery() != null ? dbOp.getQuery() : dbOp.getMethodName());
            GraphNode node = new GraphNode(nodeId, "database", truncate(label, 50));
            callGraph.addNode(node);
            
            // Find the parent method and create edge
            String parentMethod = dbOp.getClassName() + "." + dbOp.getMethodName();
            for (ServiceMethod service : analysisResult.getServices()) {
                String serviceMethod = service.getClassName() + "." + service.getMethodName();
                if (serviceMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(service.getId(), nodeId, "database", "queries");
                    callGraph.addEdge(edge);
                    break;
                }
            }
            for (RepositoryMethod repo : analysisResult.getRepositories()) {
                String repoMethod = repo.getClassName() + "." + repo.getMethodName();
                if (repoMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(repo.getId(), nodeId, "database", "queries");
                    callGraph.addEdge(edge);
                    break;
                }
            }
        }
        
        // Add transaction nodes
        int txNodeCounter = 0;
        for (Transaction tx : analysisResult.getTransactions()) {
            String nodeId = "tx-" + txNodeCounter++;
            String label = "TX: " + tx.getMethodName() + " (" + tx.getPropagation() + ")";
            GraphNode node = new GraphNode(nodeId, "database", truncate(label, 50));
            callGraph.addNode(node);
            
            // Find the parent method and create edge
            String parentMethod = tx.getClassName() + "." + tx.getMethodName();
            for (ServiceMethod service : analysisResult.getServices()) {
                String serviceMethod = service.getClassName() + "." + service.getMethodName();
                if (serviceMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(service.getId(), nodeId, "database", "transactional");
                    callGraph.addEdge(edge);
                    break;
                }
            }
        }
        
        // Add external call nodes
        int extNodeCounter = 0;
        for (ExternalCall extCall : analysisResult.getExternalCalls()) {
            String nodeId = "ext-" + extNodeCounter++;
            String label = extCall.getType() + ": " + (extCall.getMethod() != null ? extCall.getMethod() + " " : "") + 
                          (extCall.getEndpoint() != null ? extCall.getEndpoint() : "");
            GraphNode node = new GraphNode(nodeId, "external", truncate(label, 50));
            callGraph.addNode(node);
            
            // Find the parent method and create edge
            String parentMethod = extCall.getCallingClass() + "." + extCall.getCallingMethod();
            for (Endpoint endpoint : analysisResult.getEndpoints()) {
                String endpointMethod = endpoint.getControllerClass() + "." + endpoint.getMethodName();
                if (endpointMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(endpoint.getId(), nodeId, "external", "calls");
                    callGraph.addEdge(edge);
                    break;
                }
            }
            for (ServiceMethod service : analysisResult.getServices()) {
                String serviceMethod = service.getClassName() + "." + service.getMethodName();
                if (serviceMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(service.getId(), nodeId, "external", "calls");
                    callGraph.addEdge(edge);
                    break;
                }
            }
        }
        
        // Add data flow nodes
        int dfNodeCounter = 0;
        for (DataFlow df : analysisResult.getDataFlows()) {
            String nodeId = "dataflow-" + dfNodeCounter++;
            String label = "DataFlow: " + df.getMethodName();
            GraphNode node = new GraphNode(nodeId, "dataflow", truncate(label, 50));
            callGraph.addNode(node);
            
            // Connect to parent method
            String parentMethod = df.getClassName() + "." + df.getMethodName();
            for (ServiceMethod service : analysisResult.getServices()) {
                String serviceMethod = service.getClassName() + "." + service.getMethodName();
                if (serviceMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(service.getId(), nodeId, "dataflow", "tracks");
                    callGraph.addEdge(edge);
                    break;
                }
            }
            for (Endpoint endpoint : analysisResult.getEndpoints()) {
                String endpointMethod = endpoint.getControllerClass() + "." + endpoint.getMethodName();
                if (endpointMethod.equals(parentMethod)) {
                    GraphEdge edge = new GraphEdge(endpoint.getId(), nodeId, "dataflow", "tracks");
                    callGraph.addEdge(edge);
                    break;
                }
            }
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
        
        // Add edges from methods to log statements
        for (LogStatement log : analysisResult.getLogStatements()) {
            String methodId = log.getMethodId();
            if (methodId != null && !methodId.isEmpty()) {
                GraphEdge edge = new GraphEdge(methodId, log.getId(), "log", "logs");
                callGraph.addEdge(edge);
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
        
        // Log statistics
        stats.setTotalLogStatements(analysisResult.getLogStatements().size());
        Map<String, Integer> logsByLevel = new HashMap<>();
        for (LogStatement log : analysisResult.getLogStatements()) {
            String level = log.getLevel().toString();
            logsByLevel.put(level, logsByLevel.getOrDefault(level, 0) + 1);
        }
        stats.setLogsByLevel(logsByLevel);
        
        // Exception statistics
        stats.setTotalExceptionHandlers(analysisResult.getExceptionHandlers().size());
        stats.setTotalTryCatchBlocks(analysisResult.getTryCatchBlocks().size());
        
        // Database statistics
        stats.setTotalDatabaseOperations(analysisResult.getDatabaseOperations().size());
        stats.setTotalTransactions(analysisResult.getTransactions().size());
        
        // External call statistics
        stats.setTotalExternalCalls(analysisResult.getExternalCalls().size());
        Map<String, Integer> callsByType = new HashMap<>();
        for (ExternalCall call : analysisResult.getExternalCalls()) {
            String type = call.getType();
            callsByType.put(type, callsByType.getOrDefault(type, 0) + 1);
        }
        stats.setExternalCallsByType(callsByType);
        
        // Data flow statistics
        stats.setTotalDataFlows(analysisResult.getDataFlows().size());
        
        // TODO: Implement circular dependency detection
        stats.setCircularDependencies(0);
        // TODO: Calculate max call depth
        stats.setMaxCallDepth(0);
    }
    
    /**
     * Convert DatabaseAnalyzer.DatabaseOperation to model DatabaseOperation
     */
    private DatabaseOperation convertDatabaseOperation(DatabaseAnalyzer.DatabaseOperation dbOp, String sourceFile) {
        // Determine operation type from the type or query
        String operationType = dbOp.type != null ? dbOp.type : "UNKNOWN";
        
        return new DatabaseOperation(
            dbOp.dbFramework, operationType, dbOp.query,
            dbOp.methodName, dbOp.className,
            dbOp.repositoryMethod, null, // entityType not tracked by analyzer
            dbOp.isNativeQuery, new ArrayList<>(), // parameters not tracked by analyzer
            sourceFile, dbOp.lineNumber
        );
    }
    
    /**
     * Convert ExternalCallAnalyzer.ExternalCall to model ExternalCall
     */
    private ExternalCall convertExternalCall(ExternalCallAnalyzer.ExternalCall callInfo, String sourceFile) {
        // Determine the endpoint based on the type
        String endpoint = null;
        if (callInfo.url != null) {
            endpoint = callInfo.url;
        } else if (callInfo.topic != null) {
            endpoint = callInfo.topic;
        } else if (callInfo.cacheKey != null) {
            endpoint = callInfo.cacheKey;
        }
        
        // Determine the method (HTTP method or operation)
        String method = callInfo.httpMethod != null ? callInfo.httpMethod : callInfo.operation;
        
        return new ExternalCall(
            callInfo.type, method, endpoint,
            callInfo.callingMethod, callInfo.className,
            callInfo.clientType, callInfo.isAsync,
            null, null, // requestBody and responseType not tracked by analyzer
            new ArrayList<>(), // parameters not tracked by analyzer
            sourceFile, callInfo.lineNumber
        );
    }
    
    /**
     * Convert DataFlowAnalyzer.DataFlowInfo to model DataFlow
     */
    private DataFlow convertDataFlow(DataFlowAnalyzer.DataFlowInfo dataFlowInfo, String methodId, 
                                     String className, String sourceFile) {
        DataFlow dataFlow = new DataFlow(methodId, className, dataFlowInfo.methodName, sourceFile);
        
        // Convert parameters
        for (DataFlowAnalyzer.ParameterInfo paramInfo : dataFlowInfo.parameters) {
            ParameterFlow paramFlow = new ParameterFlow(paramInfo.name, paramInfo.type, paramInfo.isInputData);
            paramFlow.setAnnotations(paramInfo.annotations);
            dataFlow.getParameters().add(paramFlow);
        }
        
        // Convert variable flows
        for (DataFlowAnalyzer.VariableFlow varFlow : dataFlowInfo.variableFlows) {
            VariableTransformation varTrans = new VariableTransformation(
                varFlow.variableName, varFlow.type, varFlow.transformationType,
                varFlow.initializedFrom, varFlow.lineNumber
            );
            varTrans.setTransformationMethod(varFlow.transformationMethod);
            dataFlow.getVariables().add(varTrans);
        }
        
        // Convert return flows
        for (DataFlowAnalyzer.ReturnFlow returnFlow : dataFlowInfo.returnFlows) {
            ReturnValue returnValue = new ReturnValue(returnFlow.expression, returnFlow.returnType, 
                                                     returnFlow.lineNumber);
            returnValue.setSourceVariable(returnFlow.variableName);
            returnValue.setSourceMethod(returnFlow.methodName);
            dataFlow.getReturns().add(returnValue);
        }
        
        // Convert method call flows to data transfers
        for (DataFlowAnalyzer.MethodCallFlow methodCallFlow : dataFlowInfo.methodCallFlows) {
            DataTransfer dataTransfer = new DataTransfer(methodId, methodCallFlow.methodName, 
                                                        methodCallFlow.lineNumber);
            
            for (DataFlowAnalyzer.ArgumentFlow argFlow : methodCallFlow.arguments) {
                ArgumentMapping argMapping = new ArgumentMapping(argFlow.position, argFlow.type,
                                                                argFlow.variableName, argFlow.expression);
                dataTransfer.getArguments().add(argMapping);
            }
            
            dataFlow.getDataTransfers().add(dataTransfer);
        }
        
        return dataFlow;
    }
    
    /**
     * Truncate a string to specified length
     */
    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
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
    
    /**
     * Extract source code representation for a method
     */
    private void extractMethodSource(MethodDeclaration method, String methodId, Object methodObject) {
        try {
            // Extract source information
            com.jgraph.model.source.MethodSource methodSource = 
                sourceCodeExtractor.extractMethodSource(method, methodId, sourceDetailLevel);
            
            // Add to source map
            sourceMap.addMethod(methodId, methodSource);
            
            // Set reference and summary in the method object
            if (methodObject instanceof Endpoint) {
                Endpoint endpoint = (Endpoint) methodObject;
                endpoint.setSourceMapRef(methodId);
                endpoint.setComplexity(methodSource.getComplexity());
                endpoint.setOperations(methodSource.getOperations());
                endpoint.setSummary(methodSource.getSummary());
            } else if (methodObject instanceof ServiceMethod) {
                ServiceMethod serviceMethod = (ServiceMethod) methodObject;
                serviceMethod.setSourceMapRef(methodId);
                serviceMethod.setComplexity(methodSource.getComplexity());
                serviceMethod.setOperations(methodSource.getOperations());
                serviceMethod.setSummary(methodSource.getSummary());
            } else if (methodObject instanceof RepositoryMethod) {
                RepositoryMethod repoMethod = (RepositoryMethod) methodObject;
                repoMethod.setSourceMapRef(methodId);
                repoMethod.setComplexity(methodSource.getComplexity());
                repoMethod.setOperations(methodSource.getOperations());
                repoMethod.setSummary(methodSource.getSummary());
            }
            
        } catch (Exception e) {
            logger.error("Error extracting source for method {}: {}", methodId, e.getMessage());
        }
    }
}
