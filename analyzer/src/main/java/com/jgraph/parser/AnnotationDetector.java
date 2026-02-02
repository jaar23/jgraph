package com.jgraph.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Detects Spring and Jakarta EE annotations
 */
public class AnnotationDetector {
    
    // Spring Controller annotations
    private static final Set<String> CONTROLLER_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "RestController", "Controller"
    ));
    
    // Spring Service annotations
    private static final Set<String> SERVICE_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "Service", "Component"
    ));
    
    // Spring Repository annotations
    private static final Set<String> REPOSITORY_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "Repository"
    ));
    
    // Spring request mapping annotations
    private static final Set<String> REQUEST_MAPPING_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "RequestMapping", "GetMapping", "PostMapping", "PutMapping", 
        "DeleteMapping", "PatchMapping"
    ));
    
    // Jakarta EE annotations
    private static final Set<String> JAKARTA_PATH_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "Path"
    ));
    
    private static final Set<String> JAKARTA_HTTP_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "PATCH"
    ));
    
    /**
     * Check if class is a controller
     */
    public boolean isController(ClassOrInterfaceDeclaration classDecl) {
        return hasAnyAnnotation(classDecl, CONTROLLER_ANNOTATIONS) ||
               hasAnyAnnotation(classDecl, JAKARTA_PATH_ANNOTATIONS);
    }
    
    /**
     * Check if class is a service
     */
    public boolean isService(ClassOrInterfaceDeclaration classDecl) {
        return hasAnyAnnotation(classDecl, SERVICE_ANNOTATIONS) ||
               hasAnnotation(classDecl, "Stateless") ||
               hasAnnotation(classDecl, "Singleton") ||
               hasAnnotation(classDecl, "ApplicationScoped");
    }
    
    /**
     * Check if class is a repository
     */
    public boolean isRepository(ClassOrInterfaceDeclaration classDecl) {
        return hasAnyAnnotation(classDecl, REPOSITORY_ANNOTATIONS) ||
               classDecl.getNameAsString().endsWith("Repository") ||
               classDecl.getNameAsString().endsWith("DAO");
    }
    
    /**
     * Check if method is an endpoint
     */
    public boolean isEndpointMethod(MethodDeclaration method) {
        return hasAnyAnnotation(method, REQUEST_MAPPING_ANNOTATIONS) ||
               hasAnyAnnotation(method, JAKARTA_HTTP_ANNOTATIONS);
    }
    
    /**
     * Get the base request mapping from class
     */
    public String getRequestMapping(ClassOrInterfaceDeclaration classDecl) {
        Optional<AnnotationExpr> requestMapping = classDecl.getAnnotationByName("RequestMapping");
        if (requestMapping.isPresent()) {
            return extractPathValue(requestMapping.get());
        }
        
        // Check for Jakarta @Path
        Optional<AnnotationExpr> path = classDecl.getAnnotationByName("Path");
        if (path.isPresent()) {
            return extractPathValue(path.get());
        }
        
        return "";
    }
    
    /**
     * Get HTTP method from method annotations
     */
    public String getHttpMethod(MethodDeclaration method) {
        for (AnnotationExpr ann : method.getAnnotations()) {
            String name = ann.getNameAsString();
            
            // Spring annotations
            if (name.equals("GetMapping") || name.equals("GET")) return "GET";
            if (name.equals("PostMapping") || name.equals("POST")) return "POST";
            if (name.equals("PutMapping") || name.equals("PUT")) return "PUT";
            if (name.equals("DeleteMapping") || name.equals("DELETE")) return "DELETE";
            if (name.equals("PatchMapping") || name.equals("PATCH")) return "PATCH";
            
            // RequestMapping with method attribute
            if (name.equals("RequestMapping")) {
                String methodAttr = extractMethodAttribute(ann);
                if (!methodAttr.isEmpty()) {
                    return methodAttr;
                }
                return "GET"; // Default to GET
            }
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Get path from method annotations
     */
    public String getMethodPath(MethodDeclaration method) {
        for (AnnotationExpr ann : method.getAnnotations()) {
            String name = ann.getNameAsString();
            
            if (REQUEST_MAPPING_ANNOTATIONS.contains(name) || name.equals("Path")) {
                return extractPathValue(ann);
            }
        }
        
        return "";
    }
    
    /**
     * Extract path value from annotation
     */
    private String extractPathValue(AnnotationExpr ann) {
        // SingleMemberAnnotation: @GetMapping("/users")
        if (ann instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr singleAnn = (SingleMemberAnnotationExpr) ann;
            return cleanPath(singleAnn.getMemberValue().toString());
        }
        
        // NormalAnnotation: @RequestMapping(value = "/users", method = GET)
        if (ann instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normalAnn = (NormalAnnotationExpr) ann;
            for (MemberValuePair pair : normalAnn.getPairs()) {
                if (pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path")) {
                    return cleanPath(pair.getValue().toString());
                }
            }
        }
        
        return "";
    }
    
    /**
     * Extract method attribute from RequestMapping
     */
    private String extractMethodAttribute(AnnotationExpr ann) {
        if (ann instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normalAnn = (NormalAnnotationExpr) ann;
            for (MemberValuePair pair : normalAnn.getPairs()) {
                if (pair.getNameAsString().equals("method")) {
                    String value = pair.getValue().toString();
                    // Extract RequestMethod.GET -> GET
                    if (value.contains(".")) {
                        return value.substring(value.lastIndexOf(".") + 1);
                    }
                    return value;
                }
            }
        }
        return "";
    }
    
    /**
     * Clean path string (remove quotes and braces)
     */
    private String cleanPath(String path) {
        if (path == null) return "";
        
        // Remove quotes
        path = path.replaceAll("\"", "");
        
        // Remove curly braces for arrays: {"/users", "/user"} -> /users
        if (path.startsWith("{") && path.contains(",")) {
            path = path.substring(1, path.indexOf(","));
            path = path.trim();
        }
        
        path = path.replaceAll("[{}\\[\\]]", "");
        
        return path.trim();
    }
    
    /**
     * Check if class has any of the given annotations
     */
    private boolean hasAnyAnnotation(ClassOrInterfaceDeclaration classDecl, Set<String> annotations) {
        for (String ann : annotations) {
            if (hasAnnotation(classDecl, ann)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if method has any of the given annotations
     */
    private boolean hasAnyAnnotation(MethodDeclaration method, Set<String> annotations) {
        for (String ann : annotations) {
            if (method.getAnnotationByName(ann).isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if class has annotation
     */
    private boolean hasAnnotation(ClassOrInterfaceDeclaration classDecl, String annotationName) {
        return classDecl.getAnnotationByName(annotationName).isPresent();
    }
}
