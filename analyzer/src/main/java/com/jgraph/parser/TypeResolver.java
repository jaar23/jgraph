package com.jgraph.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves types across the codebase for accurate method call matching
 */
public class TypeResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(TypeResolver.class);
    
    // Maps simple class names to fully qualified names
    private Map<String, String> importMap = new HashMap<>();
    
    // Maps field names to their types
    private Map<String, String> fieldTypeMap = new HashMap<>();
    
    // Maps class names to their declarations
    private Map<String, ClassOrInterfaceDeclaration> classMap;
    
    public TypeResolver(Map<String, ClassOrInterfaceDeclaration> classMap) {
        this.classMap = classMap;
    }
    
    /**
     * Build type resolution context from a compilation unit
     */
    public void buildContext(CompilationUnit cu, ClassOrInterfaceDeclaration classDecl) {
        // Clear previous context
        importMap.clear();
        fieldTypeMap.clear();
        
        // Build import map
        buildImportMap(cu);
        
        // Build field type map
        buildFieldTypeMap(classDecl);
    }
    
    /**
     * Build map of simple class names to fully qualified names from imports
     */
    private void buildImportMap(CompilationUnit cu) {
        for (ImportDeclaration imp : cu.getImports()) {
            if (!imp.isAsterisk()) {
                String fqn = imp.getNameAsString();
                String simpleName = getSimpleName(fqn);
                importMap.put(simpleName, fqn);
            }
        }
        
        // Add package context
        cu.getPackageDeclaration().ifPresent(pkg -> {
            String packageName = pkg.getNameAsString();
            importMap.put("__package__", packageName);
        });
    }
    
    /**
     * Build map of field names to their types
     */
    private void buildFieldTypeMap(ClassOrInterfaceDeclaration classDecl) {
        for (FieldDeclaration field : classDecl.getFields()) {
            for (VariableDeclarator var : field.getVariables()) {
                String fieldName = var.getNameAsString();
                String fieldType = resolveType(var.getTypeAsString());
                fieldTypeMap.put(fieldName, fieldType);
            }
        }
    }
    
    /**
     * Resolve a simple type name to fully qualified name
     */
    public String resolveType(String simpleType) {
        // Already fully qualified
        if (simpleType.contains(".")) {
            return simpleType;
        }
        
        // Check imports
        if (importMap.containsKey(simpleType)) {
            return importMap.get(simpleType);
        }
        
        // Check if it's in same package
        String packageName = importMap.get("__package__");
        if (packageName != null) {
            String potentialFqn = packageName + "." + simpleType;
            if (classMap.containsKey(potentialFqn)) {
                return potentialFqn;
            }
        }
        
        // Check java.lang package
        String javaLangFqn = "java.lang." + simpleType;
        if (isJavaLangClass(simpleType)) {
            return javaLangFqn;
        }
        
        // Return as-is if can't resolve
        return simpleType;
    }
    
    /**
     * Resolve the type of a field by name
     */
    public Optional<String> resolveFieldType(String fieldName) {
        return Optional.ofNullable(fieldTypeMap.get(fieldName));
    }
    
    /**
     * Get the fully qualified name for a scope variable
     * @param scope The variable name (e.g., "userService")
     * @return Fully qualified class name if resolvable
     */
    public Optional<String> resolveScopeType(String scope) {
        // Check if it's a field
        if (fieldTypeMap.containsKey(scope)) {
            return Optional.of(fieldTypeMap.get(scope));
        }
        
        // TODO: Add support for local variables (requires method-level context)
        
        return Optional.empty();
    }
    
    /**
     * Check if a class exists in the project
     */
    public boolean classExists(String fullyQualifiedName) {
        return classMap.containsKey(fullyQualifiedName);
    }
    
    /**
     * Get class declaration by FQN
     */
    public Optional<ClassOrInterfaceDeclaration> getClassDeclaration(String fullyQualifiedName) {
        return Optional.ofNullable(classMap.get(fullyQualifiedName));
    }
    
    /**
     * Determine if class is interface or concrete class
     */
    public String getClassType(String fullyQualifiedName) {
        Optional<ClassOrInterfaceDeclaration> classDecl = getClassDeclaration(fullyQualifiedName);
        if (classDecl.isPresent()) {
            ClassOrInterfaceDeclaration decl = classDecl.get();
            if (decl.isInterface()) {
                return "interface";
            } else if (decl.isAbstract()) {
                return "abstract";
            } else {
                return "class";
            }
        }
        return "unknown";
    }
    
    /**
     * Extract simple name from fully qualified name
     */
    private String getSimpleName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn;
    }
    
    /**
     * Check if a class is in java.lang package
     */
    private boolean isJavaLangClass(String className) {
        // Common java.lang classes
        return className.equals("String") || className.equals("Integer") || 
               className.equals("Long") || className.equals("Double") ||
               className.equals("Float") || className.equals("Boolean") ||
               className.equals("Object") || className.equals("Class") ||
               className.equals("Exception") || className.equals("Throwable") ||
               className.equals("RuntimeException");
    }
    
    /**
     * Get all resolved field types for debugging
     */
    public Map<String, String> getFieldTypeMap() {
        return new HashMap<>(fieldTypeMap);
    }
    
    /**
     * Get all imports for debugging
     */
    public Map<String, String> getImportMap() {
        return new HashMap<>(importMap);
    }
}
