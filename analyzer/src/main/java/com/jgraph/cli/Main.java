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
            
            // Parse project
            JavaProjectParser parser = new JavaProjectParser();
            AnalysisResult result = parser.parseProject(projectPath.getAbsolutePath());
            
            // Display summary
            System.out.println("Analysis Summary:");
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
            exporter.export(result, outputPath);
            
            System.out.println("Analysis exported to: " + new File(outputPath).getAbsolutePath());
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
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
