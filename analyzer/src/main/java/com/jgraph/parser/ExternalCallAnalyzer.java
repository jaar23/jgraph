package com.jgraph.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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
 * Analyzes external API calls (REST, messaging, cache) in Java code
 */
public class ExternalCallAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalCallAnalyzer.class);
    
    // REST client patterns
    private static final List<String> REST_TEMPLATE_METHODS = Arrays.asList(
        "getForObject", "getForEntity", "postForObject", "postForEntity", 
        "put", "delete", "exchange", "execute", "patchForObject"
    );
    
    private static final List<String> WEB_CLIENT_METHODS = Arrays.asList(
        "get", "post", "put", "delete", "patch", "head", "options"
    );
    
    // Messaging patterns
    private static final List<String> KAFKA_METHODS = Arrays.asList(
        "send", "sendDefault", "sendAndReceive"
    );
    
    private static final List<String> RABBITMQ_METHODS = Arrays.asList(
        "convertAndSend", "send", "sendAndReceive"
    );
    
    // Cache patterns
    private static final List<String> CACHE_METHODS = Arrays.asList(
        "get", "put", "evict", "clear", "putIfAbsent"
    );
    
    /**
     * Extract external REST API calls from a method
     */
    public List<ExternalCall> extractExternalCalls(MethodDeclaration method, String className) {
        List<ExternalCall> calls = new ArrayList<>();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return calls;
        }
        
        // Analyze method body
        calls.addAll(extractRestCalls(body.get(), className, method.getNameAsString()));
        calls.addAll(extractMessagingCalls(body.get(), className, method.getNameAsString()));
        calls.addAll(extractCacheCalls(body.get(), className, method.getNameAsString()));
        
        return calls;
    }
    
    /**
     * Extract REST API calls (RestTemplate, WebClient, HttpClient)
     */
    private List<ExternalCall> extractRestCalls(BlockStmt body, String className, String methodName) {
        List<ExternalCall> calls = new ArrayList<>();
        
        body.findAll(MethodCallExpr.class).forEach(methodCall -> {
            String calledMethod = methodCall.getNameAsString();
            
            // Check for RestTemplate methods
            if (REST_TEMPLATE_METHODS.contains(calledMethod)) {
                methodCall.getScope().ifPresent(scope -> {
                    String scopeStr = scope.toString();
                    if (scopeStr.toLowerCase().contains("resttemplate") || 
                        scopeStr.toLowerCase().contains("rest")) {
                        
                        ExternalCall call = new ExternalCall();
                        call.type = "REST";
                        call.clientType = "RestTemplate";
                        call.httpMethod = mapRestTemplateMethodToHttp(calledMethod);
                        call.className = className;
                        call.callingMethod = methodName;
                        call.url = extractUrlFromArguments(methodCall);
                        call.isAsync = false;
                        methodCall.getBegin().ifPresent(pos -> call.lineNumber = pos.line);
                        
                        calls.add(call);
                    }
                });
            }
            
            // Check for WebClient methods
            if (WEB_CLIENT_METHODS.contains(calledMethod)) {
                methodCall.getScope().ifPresent(scope -> {
                    String scopeStr = scope.toString();
                    if (scopeStr.toLowerCase().contains("webclient") || 
                        scopeStr.toLowerCase().contains("client")) {
                        
                        ExternalCall call = new ExternalCall();
                        call.type = "REST";
                        call.clientType = "WebClient";
                        call.httpMethod = calledMethod.toUpperCase();
                        call.className = className;
                        call.callingMethod = methodName;
                        call.isAsync = true;  // WebClient is reactive/async
                        methodCall.getBegin().ifPresent(pos -> call.lineNumber = pos.line);
                        
                        // WebClient typically chains .uri() method
                        call.url = extractWebClientUrl(body, methodCall);
                        
                        calls.add(call);
                    }
                });
            }
            
            // Check for Feign client (interface methods with @FeignClient)
            if (methodCall.getScope().isPresent()) {
                String scopeStr = methodCall.getScope().get().toString();
                if (scopeStr.toLowerCase().contains("client") || 
                    scopeStr.toLowerCase().contains("feign")) {
                    
                    ExternalCall call = new ExternalCall();
                    call.type = "REST";
                    call.clientType = "FeignClient";
                    call.httpMethod = "UNKNOWN";
                    call.className = className;
                    call.callingMethod = methodName;
                    call.targetMethod = scopeStr + "." + calledMethod;
                    call.isAsync = false;
                    methodCall.getBegin().ifPresent(pos -> call.lineNumber = pos.line);
                    
                    calls.add(call);
                }
            }
        });
        
        return calls;
    }
    
    /**
     * Extract messaging calls (Kafka, RabbitMQ)
     */
    private List<ExternalCall> extractMessagingCalls(BlockStmt body, String className, String methodName) {
        List<ExternalCall> calls = new ArrayList<>();
        
        body.findAll(MethodCallExpr.class).forEach(methodCall -> {
            String calledMethod = methodCall.getNameAsString();
            
            // Check for Kafka methods
            if (KAFKA_METHODS.contains(calledMethod)) {
                methodCall.getScope().ifPresent(scope -> {
                    String scopeStr = scope.toString();
                    if (scopeStr.toLowerCase().contains("kafka") || 
                        scopeStr.toLowerCase().contains("template")) {
                        
                        ExternalCall call = new ExternalCall();
                        call.type = "MESSAGING";
                        call.clientType = "KafkaTemplate";
                        call.operation = calledMethod;
                        call.className = className;
                        call.callingMethod = methodName;
                        call.isAsync = true;
                        methodCall.getBegin().ifPresent(pos -> call.lineNumber = pos.line);
                        
                        // Try to extract topic name
                        call.topic = extractTopicFromArguments(methodCall);
                        
                        calls.add(call);
                    }
                });
            }
            
            // Check for RabbitMQ methods
            if (RABBITMQ_METHODS.contains(calledMethod)) {
                methodCall.getScope().ifPresent(scope -> {
                    String scopeStr = scope.toString();
                    if (scopeStr.toLowerCase().contains("rabbit") || 
                        scopeStr.toLowerCase().contains("amqp")) {
                        
                        ExternalCall call = new ExternalCall();
                        call.type = "MESSAGING";
                        call.clientType = "RabbitTemplate";
                        call.operation = calledMethod;
                        call.className = className;
                        call.callingMethod = methodName;
                        call.isAsync = false;
                        methodCall.getBegin().ifPresent(pos -> call.lineNumber = pos.line);
                        
                        // Try to extract exchange/queue name
                        call.topic = extractTopicFromArguments(methodCall);
                        
                        calls.add(call);
                    }
                });
            }
        });
        
        return calls;
    }
    
    /**
     * Extract cache operations (Redis, @Cacheable)
     */
    private List<ExternalCall> extractCacheCalls(BlockStmt body, String className, String methodName) {
        List<ExternalCall> calls = new ArrayList<>();
        
        body.findAll(MethodCallExpr.class).forEach(methodCall -> {
            String calledMethod = methodCall.getNameAsString();
            
            // Check for cache operations
            if (CACHE_METHODS.contains(calledMethod)) {
                methodCall.getScope().ifPresent(scope -> {
                    String scopeStr = scope.toString();
                    if (scopeStr.toLowerCase().contains("cache") || 
                        scopeStr.toLowerCase().contains("redis")) {
                        
                        ExternalCall call = new ExternalCall();
                        call.type = "CACHE";
                        call.clientType = detectCacheType(scopeStr);
                        call.operation = calledMethod;
                        call.className = className;
                        call.callingMethod = methodName;
                        call.isAsync = false;
                        methodCall.getBegin().ifPresent(pos -> call.lineNumber = pos.line);
                        
                        // Try to extract cache key
                        call.cacheKey = extractCacheKeyFromArguments(methodCall);
                        
                        calls.add(call);
                    }
                });
            }
        });
        
        return calls;
    }
    
    /**
     * Map RestTemplate method name to HTTP method
     */
    private String mapRestTemplateMethodToHttp(String methodName) {
        if (methodName.startsWith("get")) return "GET";
        if (methodName.startsWith("post")) return "POST";
        if (methodName.startsWith("put")) return "PUT";
        if (methodName.startsWith("delete")) return "DELETE";
        if (methodName.startsWith("patch")) return "PATCH";
        return "UNKNOWN";
    }
    
    /**
     * Extract URL from method arguments
     */
    private String extractUrlFromArguments(MethodCallExpr methodCall) {
        if (!methodCall.getArguments().isEmpty()) {
            var firstArg = methodCall.getArguments().get(0);
            if (firstArg instanceof StringLiteralExpr) {
                return ((StringLiteralExpr) firstArg).getValue();
            }
            // Return the expression as string (might be a variable)
            return firstArg.toString();
        }
        return null;
    }
    
    /**
     * Extract URL from WebClient chain (looks for .uri() call)
     */
    private String extractWebClientUrl(BlockStmt body, MethodCallExpr startCall) {
        // WebClient uses fluent API: webClient.get().uri("/path")
        // Look for .uri() in the same statement
        String statement = startCall.toString();
        Pattern pattern = Pattern.compile("\\.uri\\([\"']([^\"']+)[\"']\\)");
        Matcher matcher = pattern.matcher(statement);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Extract topic/queue name from method arguments
     */
    private String extractTopicFromArguments(MethodCallExpr methodCall) {
        if (!methodCall.getArguments().isEmpty()) {
            var firstArg = methodCall.getArguments().get(0);
            if (firstArg instanceof StringLiteralExpr) {
                return ((StringLiteralExpr) firstArg).getValue();
            }
            return firstArg.toString();
        }
        return null;
    }
    
    /**
     * Extract cache key from method arguments
     */
    private String extractCacheKeyFromArguments(MethodCallExpr methodCall) {
        if (!methodCall.getArguments().isEmpty()) {
            var firstArg = methodCall.getArguments().get(0);
            return firstArg.toString();
        }
        return null;
    }
    
    /**
     * Detect cache type from variable name
     */
    private String detectCacheType(String variableName) {
        String lower = variableName.toLowerCase();
        if (lower.contains("redis")) return "Redis";
        if (lower.contains("cache")) return "Cache";
        return "Cache";
    }
    
    // Data class for external call information
    
    public static class ExternalCall {
        public String type;  // REST, MESSAGING, CACHE
        public String clientType;  // RestTemplate, WebClient, KafkaTemplate, etc.
        public String httpMethod;  // GET, POST, etc. (for REST)
        public String operation;  // send, get, put, etc.
        public String url;
        public String topic;  // Kafka topic or RabbitMQ queue
        public String cacheKey;
        public String className;
        public String callingMethod;
        public String targetMethod;  // For Feign clients
        public int lineNumber;
        public boolean isAsync;
    }
}
