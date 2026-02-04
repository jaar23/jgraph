#!/bin/bash

# JGraph Runner Script - Enhanced Source Code Capture Edition

echo "========================================"
echo "  JGraph - Java Project Analyzer"
echo "  Enhanced with Source Code Capture"
echo "========================================"
echo ""

# Check if command is provided
if [ -z "$1" ]; then
    echo "Usage:"
    echo "  ./run.sh build                              - Build the analyzer"
    echo "  ./run.sh analyze <project-path> [options]   - Analyze a Java project"
    echo "  ./run.sh frontend                           - Start the frontend viewer"
    echo "  ./run.sh test                               - Test with sample project"
    echo ""
    echo "Analyze Options:"
    echo "  -o, --output <file>           Output file (default: analysis.json)"
    echo "  --source-detail <level>       Detail level: MINIMAL, STANDARD, DETAILED (default: STANDARD)"
    echo "  --no-source                   Disable source code capture"
    echo ""
    echo "Examples:"
    echo "  ./run.sh analyze /path/to/project"
    echo "  ./run.sh analyze /path/to/project -o output.json"
    echo "  ./run.sh analyze /path/to/project --source-detail MINIMAL"
    echo "  ./run.sh analyze /path/to/project --no-source"
    echo ""
    exit 1
fi

COMMAND=$1

case $COMMAND in
    build)
        echo "Building analyzer with source capture features..."
        cd analyzer
        
        # Check if Docker is available
        if command -v docker &> /dev/null; then
            echo "Using Docker with Maven..."
            docker run --rm -v "$(pwd)":/app -w /app \
                maven:3.9-eclipse-temurin-17 mvn clean package -DskipTests
        else
            echo "Docker not found, trying local Maven..."
            if command -v mvn &> /dev/null; then
                mvn clean package -DskipTests
            else
                echo "Error: Neither Docker nor Maven found. Please install one of them."
                exit 1
            fi
        fi
        
        echo ""
        echo "✅ Build complete!"
        echo "   JAR: analyzer/target/jgraph-analyzer-1.0.0.jar"
        echo "   Features: Source code capture, complexity analysis, NLG summaries"
        echo ""
        ;;
    
    analyze)
        if [ -z "$2" ]; then
            echo "Error: Please provide project path"
            echo "Usage: ./run.sh analyze <project-path> [options]"
            exit 1
        fi
        
        PROJECT_PATH=$2
        shift 2
        
        # Parse additional arguments
        OUTPUT="analysis.json"
        EXTRA_ARGS=""
        
        while [[ $# -gt 0 ]]; do
            case $1 in
                -o|--output)
                    OUTPUT="$2"
                    shift 2
                    ;;
                --source-detail)
                    EXTRA_ARGS="$EXTRA_ARGS --source-detail $2"
                    shift 2
                    ;;
                --no-source)
                    EXTRA_ARGS="$EXTRA_ARGS --no-source"
                    shift
                    ;;
                *)
                    echo "Unknown option: $1"
                    exit 1
                    ;;
            esac
        done
        
        echo "📊 Analyzing Java Project"
        echo "   Project: $PROJECT_PATH"
        echo "   Output:  $OUTPUT"
        if [ -n "$EXTRA_ARGS" ]; then
            echo "   Options: $EXTRA_ARGS"
        fi
        echo ""
        
        cd analyzer
        if [ -f target/jgraph-analyzer-1.0.0.jar ]; then
            # Check if we can run Java directly
            if command -v java &> /dev/null; then
                java -jar target/jgraph-analyzer-1.0.0.jar "$PROJECT_PATH" -o "$OUTPUT" $EXTRA_ARGS
            elif command -v docker &> /dev/null; then
                # Use Docker if Java not available
                docker run --rm \
                    -v "$(pwd)":/analyzer \
                    -v "$(realpath "$PROJECT_PATH")":/project \
                    -w /analyzer \
                    eclipse-temurin:17 \
                    java -jar target/jgraph-analyzer-1.0.0.jar /project -o "$OUTPUT" $EXTRA_ARGS
            else
                echo "Error: Neither Java nor Docker found. Please install one of them."
                exit 1
            fi
            
            echo ""
            echo "✅ Analysis complete!"
            echo "   Main analysis: $OUTPUT"
            if [[ ! "$EXTRA_ARGS" =~ "--no-source" ]]; then
                SOURCE_MAP="${OUTPUT%.json}-source-map.json"
                if [ -f "$SOURCE_MAP" ]; then
                    echo "   Source map:    $SOURCE_MAP"
                fi
            fi
            echo ""
            echo "💡 Open the frontend viewer and upload the JSON files to visualize!"
            echo ""
        else
            echo "❌ Error: Analyzer JAR not found."
            echo "   Run './run.sh build' first to build the analyzer."
            exit 1
        fi
        ;;
    
    test)
        echo "🧪 Testing with sample project..."
        echo ""
        
        if [ ! -d "test-project" ]; then
            echo "❌ Error: test-project directory not found"
            exit 1
        fi
        
        # Run analysis with different detail levels
        echo "1️⃣  Testing STANDARD detail level..."
        ./run.sh analyze test-project -o test-standard.json --source-detail STANDARD
        
        echo ""
        echo "2️⃣  Testing MINIMAL detail level..."
        ./run.sh analyze test-project -o test-minimal.json --source-detail MINIMAL
        
        echo ""
        echo "3️⃣  Testing without source capture..."
        ./run.sh analyze test-project -o test-no-source.json --no-source
        
        echo ""
        echo "✅ All tests complete!"
        echo ""
        echo "📊 File Sizes:"
        ls -lh analyzer/test-*.json 2>/dev/null | awk '{print "   " $9 ": " $5}'
        echo ""
        ;;
    
    frontend)
        echo "🌐 Starting frontend viewer..."
        echo ""
        cd frontend
        
        if [ ! -d "node_modules" ]; then
            echo "📦 Installing dependencies..."
            npm install
            echo ""
        fi
        
        echo "✨ Frontend features:"
        echo "   • Interactive graph visualization"
        echo "   • Source code details panel"
        echo "   • Complexity metrics display"
        echo "   • Natural language summaries"
        echo "   • User annotations support"
        echo ""
        
        npm run dev
        ;;
    
    clean)
        echo "🧹 Cleaning build artifacts..."
        
        # Clean analyzer
        if [ -d "analyzer/target" ]; then
            rm -rf analyzer/target
            echo "   ✓ Cleaned analyzer/target"
        fi
        
        # Clean test outputs
        rm -f analyzer/test-*.json analyzer/analysis*.json
        echo "   ✓ Cleaned test outputs"
        
        # Clean frontend
        if [ -d "frontend/dist" ]; then
            rm -rf frontend/dist
            echo "   ✓ Cleaned frontend/dist"
        fi
        
        echo ""
        echo "✅ Clean complete!"
        ;;
    
    help)
        echo "JGraph - Enhanced Java Project Analyzer"
        echo ""
        echo "COMMANDS:"
        echo "  build      Build the analyzer JAR"
        echo "  analyze    Analyze a Java project with source capture"
        echo "  frontend   Start the web-based viewer"
        echo "  test       Run analysis on test project with all options"
        echo "  clean      Remove build artifacts and test outputs"
        echo "  help       Show this help message"
        echo ""
        echo "FEATURES:"
        echo "  ✓ Source code capture with complexity analysis"
        echo "  ✓ Natural language summaries (auto-generated)"
        echo "  ✓ Variable lifecycle tracking"
        echo "  ✓ Logic flow visualization"
        echo "  ✓ Smart compression (up to 76% size reduction)"
        echo "  ✓ Three detail levels: MINIMAL, STANDARD, DETAILED"
        echo ""
        echo "For more information, see README.md"
        ;;
    
    *)
        echo "❌ Unknown command: $COMMAND"
        echo ""
        echo "Available commands: build, analyze, frontend, test, clean, help"
        echo "Run './run.sh help' for detailed usage information"
        exit 1
        ;;
esac
