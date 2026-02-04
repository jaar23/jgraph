package com.jgraph.parser;

import com.jgraph.model.ModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects and discovers multi-module project structure
 */
public class MultiModuleDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiModuleDetector.class);
    
    /**
     * Detect if a project is multi-module and discover all modules
     */
    public static List<ModuleInfo> detectModules(String projectPath) {
        List<ModuleInfo> modules = new ArrayList<>();
        Path rootPath = Paths.get(projectPath);
        
        // Check for Maven multi-module project
        Path pomPath = rootPath.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            logger.info("Detected Maven project at: {}", projectPath);
            modules = detectMavenModules(rootPath, pomPath);
            if (!modules.isEmpty()) {
                return modules;
            }
        }
        
        // Check for Gradle multi-project
        Path settingsGradle = rootPath.resolve("settings.gradle");
        Path settingsGradleKts = rootPath.resolve("settings.gradle.kts");
        
        if (Files.exists(settingsGradle)) {
            logger.info("Detected Gradle project at: {}", projectPath);
            modules = detectGradleModules(rootPath, settingsGradle);
            if (!modules.isEmpty()) {
                return modules;
            }
        } else if (Files.exists(settingsGradleKts)) {
            logger.info("Detected Gradle Kotlin project at: {}", projectPath);
            modules = detectGradleModules(rootPath, settingsGradleKts);
            if (!modules.isEmpty()) {
                return modules;
            }
        }
        
        // Check for Gradle without settings file (single module or multi-module with build files)
        Path buildGradle = rootPath.resolve("build.gradle");
        Path buildGradleKts = rootPath.resolve("build.gradle.kts");
        
        if (Files.exists(buildGradle) || Files.exists(buildGradleKts)) {
            logger.info("Detected single Gradle module at: {}", projectPath);
            Path gradleFile = Files.exists(buildGradle) ? buildGradle : buildGradleKts;
            ModuleInfo module = BuildFileParser.parseGradle(gradleFile);
            if (module != null) {
                module.setRelativePath(".");
                modules.add(module);
            }
        }
        
        // If no build files found, treat as single simple Java project
        if (modules.isEmpty()) {
            logger.info("No build system detected, treating as simple Java project");
            ModuleInfo module = createSimpleModule(rootPath);
            modules.add(module);
        }
        
        return modules;
    }
    
    /**
     * Detect Maven modules
     */
    private static List<ModuleInfo> detectMavenModules(Path rootPath, Path pomPath) {
        List<ModuleInfo> modules = new ArrayList<>();
        
        // Parse parent POM to get module list
        List<String> moduleNames = BuildFileParser.parsePomModules(pomPath);
        
        if (moduleNames.isEmpty()) {
            // Single module project
            logger.info("Single Maven module detected");
            ModuleInfo module = BuildFileParser.parsePom(pomPath);
            if (module != null) {
                module.setRelativePath(".");
                modules.add(module);
            }
        } else {
            // Multi-module project
            logger.info("Maven multi-module project detected with {} modules", moduleNames.size());
            
            // Add parent module
            ModuleInfo parentModule = BuildFileParser.parsePom(pomPath);
            if (parentModule != null) {
                parentModule.setRelativePath(".");
                parentModule.setType(ModuleInfo.ModuleType.PARENT);
                modules.add(parentModule);
            }
            
            // Add child modules
            for (String moduleName : moduleNames) {
                Path modulePath = rootPath.resolve(moduleName);
                Path modulePom = modulePath.resolve("pom.xml");
                
                if (Files.exists(modulePom)) {
                    ModuleInfo module = BuildFileParser.parsePom(modulePom);
                    if (module != null) {
                        module.setRelativePath(moduleName);
                        modules.add(module);
                        logger.info("  Found module: {} at {}", module.getName(), moduleName);
                    }
                } else {
                    logger.warn("Module POM not found: {}", modulePom);
                }
            }
        }
        
        return modules;
    }
    
    /**
     * Detect Gradle modules/subprojects
     */
    private static List<ModuleInfo> detectGradleModules(Path rootPath, Path settingsPath) {
        List<ModuleInfo> modules = new ArrayList<>();
        
        // Parse settings.gradle to get subproject list
        List<String> subprojectNames = BuildFileParser.parseGradleSettings(settingsPath);
        
        if (subprojectNames.isEmpty()) {
            // Single module project
            logger.info("Single Gradle module detected");
            Path buildGradle = rootPath.resolve("build.gradle");
            Path buildGradleKts = rootPath.resolve("build.gradle.kts");
            
            Path buildFile = Files.exists(buildGradle) ? buildGradle : buildGradleKts;
            if (Files.exists(buildFile)) {
                ModuleInfo module = BuildFileParser.parseGradle(buildFile);
                if (module != null) {
                    module.setRelativePath(".");
                    modules.add(module);
                }
            }
        } else {
            // Multi-project build
            logger.info("Gradle multi-project build detected with {} subprojects", subprojectNames.size());
            
            // Add root project
            Path buildGradle = rootPath.resolve("build.gradle");
            Path buildGradleKts = rootPath.resolve("build.gradle.kts");
            
            Path rootBuildFile = Files.exists(buildGradle) ? buildGradle : buildGradleKts;
            if (Files.exists(rootBuildFile)) {
                ModuleInfo rootModule = BuildFileParser.parseGradle(rootBuildFile);
                if (rootModule != null) {
                    rootModule.setRelativePath(".");
                    rootModule.setType(ModuleInfo.ModuleType.PARENT);
                    modules.add(rootModule);
                }
            }
            
            // Add subprojects
            for (String subprojectName : subprojectNames) {
                Path subprojectPath = rootPath.resolve(subprojectName);
                Path subprojectBuildGradle = subprojectPath.resolve("build.gradle");
                Path subprojectBuildGradleKts = subprojectPath.resolve("build.gradle.kts");
                
                Path buildFile = Files.exists(subprojectBuildGradle) ? subprojectBuildGradle : subprojectBuildGradleKts;
                
                if (Files.exists(buildFile)) {
                    ModuleInfo module = BuildFileParser.parseGradle(buildFile);
                    if (module != null) {
                        module.setRelativePath(subprojectName);
                        modules.add(module);
                        logger.info("  Found subproject: {} at {}", module.getName(), subprojectName);
                    }
                } else {
                    logger.warn("Build file not found for subproject: {}", subprojectName);
                }
            }
        }
        
        return modules;
    }
    
    /**
     * Create a simple module info for projects without build files
     */
    private static ModuleInfo createSimpleModule(Path rootPath) {
        String name = rootPath.getFileName().toString();
        ModuleInfo module = new ModuleInfo(name, rootPath.toString(), ".");
        module.setBuildSystem(ModuleInfo.BuildSystem.UNKNOWN);
        module.setType(ModuleInfo.ModuleType.APPLICATION);
        
        // Add default source directories
        List<String> sourceDirs = new ArrayList<>();
        
        // Check for common source directory patterns
        if (Files.exists(rootPath.resolve("src/main/java"))) {
            sourceDirs.add("src/main/java");
        }
        if (Files.exists(rootPath.resolve("src"))) {
            sourceDirs.add("src");
        }
        if (sourceDirs.isEmpty()) {
            sourceDirs.add("."); // Fallback to root
        }
        
        module.setSourceDirectories(sourceDirs);
        return module;
    }
    
    /**
     * Check if a directory contains Java source files
     */
    public static boolean hasJavaFiles(Path directory) {
        if (!Files.isDirectory(directory)) {
            return false;
        }
        
        try {
            return Files.walk(directory)
                .anyMatch(p -> p.toString().endsWith(".java"));
        } catch (Exception e) {
            logger.error("Error checking for Java files in: {}", directory, e);
            return false;
        }
    }
}
