package com.jgraph.cli;

import com.jgraph.exporter.JsonExporter;
import com.jgraph.model.AnalysisResult;
import com.jgraph.parser.JavaProjectParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Command line interface for JGraph analyzer
 */
@Command(
    name = "jgraph",
    mixinStandardHelpOptions = true,
    version = "JGraph 1.0.0",
    description = "Analyzes Java web projects (Spring/Jakarta EE) and generates architectural graphs"
)
public class Main implements Callable<Integer> {
    
    @Parameters(
        index = "0",
        description = "Path to Java project directory to analyze"
    )
    private File projectPath;
    
    @Option(
        names = {"-o", "--output"},
        description = "Output JSON file path (default: analysis.json)",
        defaultValue = "analysis.json"
    )
    private String outputPath;
    
    @Option(
        names = {"-v", "--verbose"},
        description = "Enable verbose output"
    )
    private boolean verbose;
    
    @Option(
        names = {"--include-tests"},
        description = "Include test files in analysis"
    )
    private boolean includeTests;
    
    @Option(
        names = {"-m", "--module"},
        description = "Analyze only specific module(s) in a multi-module project (comma-separated)"
    )
    private String modules;
    
    @Option(
        names = {"--list-modules"},
        description = "List all detected modules and exit"
    )
    private boolean listModules;
    
    @Option(
        names = {"--source-detail"},
        description = "Source code detail level: minimal, standard, detailed (default: standard)",
        defaultValue = "standard"
    )
    private String sourceDetailLevel;
    
    @Option(
        names = {"--no-source"},
        description = "Disable source code extraction (smaller output)"
    )
    private boolean noSource;
    
    @Override
    public Integer call() throws Exception {
        try {
            // Validate project path
            if (!projectPath.exists()) {
                System.err.println("Error: Project path does not exist: " + projectPath);
                return 1;
            }
            
            if (!projectPath.isDirectory()) {
                System.err.println("Error: Project path is not a directory: " + projectPath);
                return 1;
            }
            
            System.out.println("JGraph Analyzer v1.0.0");
            System.out.println("========================");
            System.out.println("Analyzing project: " + projectPath.getAbsolutePath());
            System.out.println();
            
            // List modules mode
            if (listModules) {
                return listProjectModules(projectPath);
            }
            
            // Parse project
            JavaProjectParser parser = new JavaProjectParser();
            
            // Configure source extraction
            if (noSource) {
                parser.setExtractSource(false);
            } else {
                // Set detail level
                com.jgraph.parser.SourceCodeExtractor.DetailLevel detailLevel;
                switch (sourceDetailLevel.toLowerCase()) {
                    case "minimal":
                        detailLevel = com.jgraph.parser.SourceCodeExtractor.DetailLevel.MINIMAL;
                        break;
                    case "detailed":
                        detailLevel = com.jgraph.parser.SourceCodeExtractor.DetailLevel.DETAILED;
                        break;
                    default:
                        detailLevel = com.jgraph.parser.SourceCodeExtractor.DetailLevel.STANDARD;
                }
                parser.setSourceDetailLevel(detailLevel);
            }
            
            AnalysisResult result = parser.parseProject(projectPath.getAbsolutePath());
            
            // Display summary
            System.out.println("Analysis Summary:");
            
            // Show module information if multi-module
            if (result.getProject().isMultiModule()) {
                System.out.println("  Multi-Module:      Yes");
                System.out.println("  Build System:      " + result.getProject().getBuildSystem());
                System.out.println("  Total Modules:     " + result.getProject().getTotalModules());
            }
            
            System.out.println("  Total Files:       " + result.getProject().getTotalFiles());
            System.out.println("  Endpoints:         " + result.getStatistics().getTotalEndpoints());
            System.out.println("  Services:          " + result.getStatistics().getTotalServices());
            System.out.println("  Repositories:      " + result.getStatistics().getTotalRepositories());
            System.out.println("  Dependencies:      " + result.getDependencies().size());
            System.out.println("  Call Graph Nodes:  " + result.getCallGraph().getNodes().size());
            System.out.println("  Call Graph Edges:  " + result.getCallGraph().getEdges().size());
            System.out.println();
            
            // Export to JSON
            JsonExporter exporter = new JsonExporter();
            if (noSource) {
                exporter.export(result, outputPath);
                System.out.println("Analysis exported to: " + new File(outputPath).getAbsolutePath());
            } else {
                // Export with source map
                com.jgraph.model.source.SourceMap sourceMap = parser.getSourceMap();
                exporter.export(result, sourceMap, outputPath);
                
                System.out.println("Analysis exported to: " + new File(outputPath).getAbsolutePath());
                if (!sourceMap.getMethods().isEmpty()) {
                    String sourceMapPath = outputPath.replace(".json", "-source-map.json");
                    System.out.println("Source map exported to: " + new File(sourceMapPath).getAbsolutePath());
                    System.out.println("  Methods in source map: " + sourceMap.getMethods().size());
                    System.out.println("  Detail level: " + sourceDetailLevel);
                }
            }
            System.out.println();
            System.out.println("Success! Open the JSON file in the JGraph web viewer to visualize.");
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }
    
    /**
     * List all modules in the project
     */
    private Integer listProjectModules(File projectPath) {
        try {
            System.out.println("Detecting modules in: " + projectPath.getAbsolutePath());
            System.out.println();
            
            java.util.List<com.jgraph.model.ModuleInfo> modules = 
                com.jgraph.parser.MultiModuleDetector.detectModules(projectPath.getAbsolutePath());
            
            if (modules.isEmpty()) {
                System.out.println("No modules detected.");
                return 0;
            }
            
            System.out.println("Found " + modules.size() + " module(s):");
            System.out.println();
            
            for (com.jgraph.model.ModuleInfo module : modules) {
                System.out.println("Module: " + module.getName());
                System.out.println("  Path:         " + module.getRelativePath());
                System.out.println("  Type:         " + module.getType());
                System.out.println("  Build System: " + module.getBuildSystem());
                if (module.getGroupId() != null) {
                    System.out.println("  Group ID:     " + module.getGroupId());
                }
                if (module.getArtifactId() != null) {
                    System.out.println("  Artifact ID:  " + module.getArtifactId());
                }
                if (module.getVersion() != null) {
                    System.out.println("  Version:      " + module.getVersion());
                }
                System.out.println();
            }
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("Error detecting modules: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
