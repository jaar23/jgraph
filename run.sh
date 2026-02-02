#!/bin/bash

# JGraph Runner Script

echo "========================================"
echo "  JGraph - Java Project Analyzer"
echo "========================================"
echo ""

# Check if command is provided
if [ -z "$1" ]; then
    echo "Usage:"
    echo "  ./run.sh analyze <project-path>  - Analyze a Java project"
    echo "  ./run.sh frontend                - Start the frontend viewer"
    echo "  ./run.sh build                   - Build the analyzer"
    echo ""
    exit 1
fi

COMMAND=$1

case $COMMAND in
    build)
        echo "Building analyzer..."
        cd analyzer
        docker run --rm -v "$(pwd)":/usr/src/app -w /usr/src/app \
            maven:3.9-eclipse-temurin-11 mvn clean package -DskipTests
        echo ""
        echo "Build complete! JAR: analyzer/target/jgraph-analyzer-1.0.0.jar"
        ;;
    
    analyze)
        if [ -z "$2" ]; then
            echo "Error: Please provide project path"
            echo "Usage: ./run.sh analyze <project-path>"
            exit 1
        fi
        
        PROJECT_PATH=$2
        OUTPUT=${3:-analysis.json}
        
        echo "Analyzing: $PROJECT_PATH"
        echo "Output: $OUTPUT"
        echo ""
        
        cd analyzer
        if [ -f target/jgraph-analyzer-1.0.0.jar ]; then
            docker run --rm \
                -v "$(pwd)":/analyzer \
                -v "$PROJECT_PATH":/project \
                -w /analyzer \
                eclipse-temurin:11 \
                java -jar target/jgraph-analyzer-1.0.0.jar /project --output "$OUTPUT"
        else
            echo "Error: Analyzer JAR not found. Run './run.sh build' first."
            exit 1
        fi
        ;;
    
    frontend)
        echo "Starting frontend..."
        cd frontend
        if [ ! -d "node_modules" ]; then
            echo "Installing dependencies..."
            npm install
        fi
        npm run dev
        ;;
    
    *)
        echo "Unknown command: $COMMAND"
        echo "Use: build, analyze, or frontend"
        exit 1
        ;;
esac
