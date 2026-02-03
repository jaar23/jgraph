package com.jgraph.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.expr.Expression;
import com.jgraph.model.Branch;
import com.jgraph.model.ControlFlow;
import com.jgraph.model.ReturnPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzes control flow within methods
 */
public class ControlFlowAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(ControlFlowAnalyzer.class);
    
    /**
     * Analyze control flow in a method
     */
    public ControlFlow analyze(MethodDeclaration method) {
        ControlFlow controlFlow = new ControlFlow();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return controlFlow;
        }
        
        BlockStmt block = body.get();
        
        // Analyze branches
        List<Branch> branches = extractBranches(block);
        controlFlow.setBranches(branches);
        
        // Analyze return points
        List<ReturnPoint> returnPoints = extractReturnPoints(block);
        controlFlow.setReturnPoints(returnPoints);
        controlFlow.setHasEarlyReturns(returnPoints.size() > 1);
        
        // Check for try-catch
        boolean hasTryCatch = !block.findAll(TryStmt.class).isEmpty();
        controlFlow.setHasTryCatch(hasTryCatch);
        
        // Check for loops
        boolean hasLoops = hasLoops(block);
        controlFlow.setHasLoops(hasLoops);
        
        return controlFlow;
    }
    
    /**
     * Extract all conditional branches
     */
    private List<Branch> extractBranches(BlockStmt block) {
        List<Branch> branches = new ArrayList<>();
        
        // If statements
        block.findAll(IfStmt.class).forEach(ifStmt -> {
            Expression condition = ifStmt.getCondition();
            int lineNumber = ifStmt.getBegin().map(pos -> pos.line).orElse(0);
            
            Branch branch = new Branch("if", condition.toString(), lineNumber);
            branch.setHasElse(ifStmt.getElseStmt().isPresent());
            branches.add(branch);
        });
        
        // Switch statements
        block.findAll(SwitchStmt.class).forEach(switchStmt -> {
            Expression selector = switchStmt.getSelector();
            int lineNumber = switchStmt.getBegin().map(pos -> pos.line).orElse(0);
            
            Branch branch = new Branch("switch", selector.toString(), lineNumber);
            branches.add(branch);
            
            // Add case branches
            for (SwitchEntry entry : switchStmt.getEntries()) {
                int caseLine = entry.getBegin().map(pos -> pos.line).orElse(0);
                String caseValue = entry.getLabels().isEmpty() ? "default" : 
                                   entry.getLabels().get(0).toString();
                Branch caseBranch = new Branch("case", caseValue, caseLine);
                branches.add(caseBranch);
            }
        });
        
        return branches;
    }
    
    /**
     * Extract all return points
     */
    private List<ReturnPoint> extractReturnPoints(BlockStmt block) {
        List<ReturnPoint> returnPoints = new ArrayList<>();
        
        block.findAll(ReturnStmt.class).forEach(returnStmt -> {
            int lineNumber = returnStmt.getBegin().map(pos -> pos.line).orElse(0);
            String returnValue = returnStmt.getExpression()
                .map(Expression::toString)
                .orElse("void");
            
            ReturnPoint returnPoint = new ReturnPoint(lineNumber, returnValue);
            
            // Check if it's inside a conditional
            returnPoint.setConditional(isInsideConditional(returnStmt));
            
            returnPoints.add(returnPoint);
        });
        
        return returnPoints;
    }
    
    /**
     * Check if statement is inside a conditional block
     */
    private boolean isInsideConditional(ReturnStmt returnStmt) {
        return returnStmt.findAncestor(IfStmt.class).isPresent() ||
               returnStmt.findAncestor(SwitchStmt.class).isPresent() ||
               returnStmt.findAncestor(TryStmt.class).isPresent();
    }
    
    /**
     * Check if block contains loops
     */
    private boolean hasLoops(BlockStmt block) {
        return !block.findAll(ForStmt.class).isEmpty() ||
               !block.findAll(WhileStmt.class).isEmpty() ||
               !block.findAll(DoStmt.class).isEmpty() ||
               !block.findAll(ForEachStmt.class).isEmpty();
    }
}
