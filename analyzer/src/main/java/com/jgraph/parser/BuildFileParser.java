package com.jgraph.parser;

import com.jgraph.model.ModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for build configuration files (pom.xml, build.gradle, settings.gradle)
 */
public class BuildFileParser {
    
    private static final Logger logger = LoggerFactory.getLogger(BuildFileParser.class);
    
    /**
     * Parse Maven pom.xml file
     */
    public static ModuleInfo parsePom(Path pomPath) {
        try {
            File pomFile = pomPath.toFile();
            if (!pomFile.exists()) {
                logger.warn("POM file does not exist: {}", pomPath);
                return null;
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile);
            doc.getDocumentElement().normalize();
            
            Element root = doc.getDocumentElement();
            
            ModuleInfo moduleInfo = new ModuleInfo();
            moduleInfo.setPath(pomPath.getParent().toString());
            moduleInfo.setBuildSystem(ModuleInfo.BuildSystem.MAVEN);
            
            // Extract basic information
            moduleInfo.setGroupId(getElementText(root, "groupId"));
            moduleInfo.setArtifactId(getElementText(root, "artifactId"));
            moduleInfo.setVersion(getElementText(root, "version"));
            moduleInfo.setPackaging(getElementText(root, "packaging", "jar"));
            moduleInfo.setName(getElementText(root, "name", moduleInfo.getArtifactId()));
            
            // Check for parent
            NodeList parentNodes = root.getElementsByTagName("parent");
            if (parentNodes.getLength() > 0) {
                Element parent = (Element) parentNodes.item(0);
                String parentGroupId = getElementText(parent, "groupId");
                String parentArtifactId = getElementText(parent, "artifactId");
                moduleInfo.setParent(parentGroupId + ":" + parentArtifactId);
                
                // Inherit groupId/version from parent if not specified
                if (moduleInfo.getGroupId() == null) {
                    moduleInfo.setGroupId(parentGroupId);
                }
                if (moduleInfo.getVersion() == null) {
                    moduleInfo.setVersion(getElementText(parent, "version"));
                }
            }
            
            // Extract dependencies
            List<String> dependencies = new ArrayList<>();
            NodeList dependencyNodes = root.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Element dependency = (Element) dependencyNodes.item(i);
                String groupId = getElementText(dependency, "groupId");
                String artifactId = getElementText(dependency, "artifactId");
                if (groupId != null && artifactId != null) {
                    dependencies.add(groupId + ":" + artifactId);
                }
            }
            moduleInfo.setDependencies(dependencies);
            
            // Determine module type
            moduleInfo.setType(determineModuleType(moduleInfo));
            
            // Extract Java version
            String javaVersion = extractJavaVersion(root);
            moduleInfo.setJavaVersion(javaVersion);
            
            // Add source directories
            List<String> sourceDirs = new ArrayList<>();
            sourceDirs.add("src/main/java");
            sourceDirs.add("src/main/resources");
            moduleInfo.setSourceDirectories(sourceDirs);
            
            return moduleInfo;
            
        } catch (Exception e) {
            logger.error("Error parsing POM file: {}", pomPath, e);
            return null;
        }
    }
    
    /**
     * Parse Maven parent pom.xml to extract module list
     */
    public static List<String> parsePomModules(Path pomPath) {
        List<String> modules = new ArrayList<>();
        
        try {
            File pomFile = pomPath.toFile();
            if (!pomFile.exists()) {
                return modules;
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile);
            doc.getDocumentElement().normalize();
            
            Element root = doc.getDocumentElement();
            
            // Find <modules> section
            NodeList modulesNodes = root.getElementsByTagName("modules");
            if (modulesNodes.getLength() > 0) {
                Element modulesElement = (Element) modulesNodes.item(0);
                NodeList moduleNodes = modulesElement.getElementsByTagName("module");
                
                for (int i = 0; i < moduleNodes.getLength(); i++) {
                    String moduleName = moduleNodes.item(i).getTextContent().trim();
                    if (!moduleName.isEmpty()) {
                        modules.add(moduleName);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing POM modules: {}", pomPath, e);
        }
        
        return modules;
    }
    
    /**
     * Parse Gradle build.gradle file
     */
    public static ModuleInfo parseGradle(Path gradlePath) {
        try {
            File gradleFile = gradlePath.toFile();
            if (!gradleFile.exists()) {
                logger.warn("Gradle file does not exist: {}", gradlePath);
                return null;
            }
            
            String content = Files.readString(gradlePath);
            
            ModuleInfo moduleInfo = new ModuleInfo();
            moduleInfo.setPath(gradlePath.getParent().toString());
            moduleInfo.setBuildSystem(ModuleInfo.BuildSystem.GRADLE);
            
            // Extract group, name, version
            moduleInfo.setGroupId(extractGradleProperty(content, "group"));
            moduleInfo.setVersion(extractGradleProperty(content, "version"));
            
            // Try to extract artifactId from rootProject.name or project.name
            String name = extractGradleProperty(content, "rootProject.name");
            if (name == null) {
                name = extractGradleProperty(content, "project.name");
            }
            if (name == null) {
                name = gradlePath.getParent().getFileName().toString();
            }
            moduleInfo.setArtifactId(name);
            moduleInfo.setName(name);
            
            // Extract dependencies
            List<String> dependencies = extractGradleDependencies(content);
            moduleInfo.setDependencies(dependencies);
            
            // Determine module type
            moduleInfo.setType(determineGradleModuleType(content));
            
            // Extract Java version
            String javaVersion = extractGradleJavaVersion(content);
            moduleInfo.setJavaVersion(javaVersion);
            
            // Add source directories
            List<String> sourceDirs = new ArrayList<>();
            sourceDirs.add("src/main/java");
            sourceDirs.add("src/main/kotlin");
            sourceDirs.add("src/main/resources");
            moduleInfo.setSourceDirectories(sourceDirs);
            
            return moduleInfo;
            
        } catch (Exception e) {
            logger.error("Error parsing Gradle file: {}", gradlePath, e);
            return null;
        }
    }
    
    /**
     * Parse Gradle settings.gradle to extract subprojects
     */
    public static List<String> parseGradleSettings(Path settingsPath) {
        List<String> subprojects = new ArrayList<>();
        
        try {
            File settingsFile = settingsPath.toFile();
            if (!settingsFile.exists()) {
                return subprojects;
            }
            
            String content = Files.readString(settingsPath);
            
            // Look for include statements
            Pattern includePattern = Pattern.compile("include\\s*[\\(\\s]\\s*['\"]([^'\"]+)['\"]");
            Matcher matcher = includePattern.matcher(content);
            
            while (matcher.find()) {
                String module = matcher.group(1);
                // Remove leading colon if present
                if (module.startsWith(":")) {
                    module = module.substring(1);
                }
                // Convert Gradle notation (e.g., ":api:core") to path (e.g., "api/core")
                module = module.replace(":", "/");
                subprojects.add(module);
            }
            
        } catch (Exception e) {
            logger.error("Error parsing Gradle settings: {}", settingsPath, e);
        }
        
        return subprojects;
    }
    
    // Helper methods
    
    private static String getElementText(Element parent, String tagName) {
        return getElementText(parent, tagName, null);
    }
    
    private static String getElementText(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent().trim();
            return text.isEmpty() ? defaultValue : text;
        }
        return defaultValue;
    }
    
    private static String extractJavaVersion(Element root) {
        // Try maven.compiler.source
        NodeList properties = root.getElementsByTagName("properties");
        if (properties.getLength() > 0) {
            Element propsElement = (Element) properties.item(0);
            String version = getElementText(propsElement, "maven.compiler.source");
            if (version != null) return version;
            
            version = getElementText(propsElement, "java.version");
            if (version != null) return version;
        }
        
        return null;
    }
    
    private static ModuleInfo.ModuleType determineModuleType(ModuleInfo moduleInfo) {
        String packaging = moduleInfo.getPackaging();
        String name = moduleInfo.getName() != null ? moduleInfo.getName().toLowerCase() : "";
        
        // Check packaging
        if ("pom".equals(packaging)) {
            return ModuleInfo.ModuleType.PARENT;
        }
        if ("war".equals(packaging)) {
            return ModuleInfo.ModuleType.WEB;
        }
        
        // Check name patterns
        if (name.contains("api") || name.endsWith("-api")) {
            return ModuleInfo.ModuleType.API;
        }
        if (name.contains("service") || name.endsWith("-service")) {
            return ModuleInfo.ModuleType.SERVICE;
        }
        if (name.contains("data") || name.contains("persistence") || name.endsWith("-data")) {
            return ModuleInfo.ModuleType.DATA;
        }
        if (name.contains("common") || name.contains("shared") || name.endsWith("-common")) {
            return ModuleInfo.ModuleType.COMMON;
        }
        if (name.contains("web") || name.endsWith("-web")) {
            return ModuleInfo.ModuleType.WEB;
        }
        
        // Check dependencies for Spring Boot
        for (String dep : moduleInfo.getDependencies()) {
            if (dep.contains("spring-boot-starter-web")) {
                return ModuleInfo.ModuleType.WEB;
            }
        }
        
        return ModuleInfo.ModuleType.LIBRARY;
    }
    
    private static String extractGradleProperty(String content, String propertyName) {
        Pattern pattern = Pattern.compile(propertyName + "\\s*=\\s*['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private static List<String> extractGradleDependencies(String content) {
        List<String> dependencies = new ArrayList<>();
        
        // Pattern to match dependencies like: implementation 'group:artifact:version'
        Pattern pattern = Pattern.compile("(?:implementation|api|compile)\\s+['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String dep = matcher.group(1);
            // Extract just group:artifact (remove version)
            String[] parts = dep.split(":");
            if (parts.length >= 2) {
                dependencies.add(parts[0] + ":" + parts[1]);
            }
        }
        
        return dependencies;
    }
    
    private static ModuleInfo.ModuleType determineGradleModuleType(String content) {
        // Check for plugins
        if (content.contains("org.springframework.boot")) {
            if (content.contains("spring-boot-starter-web")) {
                return ModuleInfo.ModuleType.WEB;
            }
            return ModuleInfo.ModuleType.APPLICATION;
        }
        
        if (content.contains("war") || content.contains("'war'")) {
            return ModuleInfo.ModuleType.WEB;
        }
        
        if (content.contains("java-library")) {
            return ModuleInfo.ModuleType.LIBRARY;
        }
        
        return ModuleInfo.ModuleType.LIBRARY;
    }
    
    private static String extractGradleJavaVersion(String content) {
        // Look for sourceCompatibility or targetCompatibility
        Pattern pattern = Pattern.compile("(?:source|target)Compatibility\\s*=\\s*['\"]?([0-9.]+)['\"]?");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Look for JavaVersion enum
        pattern = Pattern.compile("JavaVersion\\.VERSION_([0-9_]+)");
        matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).replace("_", ".");
        }
        
        return null;
    }
}
