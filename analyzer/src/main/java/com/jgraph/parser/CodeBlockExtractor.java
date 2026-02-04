package com.jgraph.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.jgraph.model.source.CodeBlock;
import com.jgraph.model.source.OperationSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Extracts structured code blocks from methods
 */
public class CodeBlockExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeBlockExtractor.class);
    private static final int MAX_CODE_LENGTH = 200; // Limit code string length
    
    /**
     * Extract all code blocks from a method
     */
    public List<CodeBlock> extractCodeBlocks(MethodDeclaration method) {
        List<CodeBlock> blocks = new ArrayList<>();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return blocks;
        }
        
        BlockStmt blockStmt = body.get();
        
        // Extract statements in order
        for (Statement stmt : blockStmt.getStatements()) {
            CodeBlock block = extractStatement(stmt);
            if (block != null) {
                blocks.add(block);
            }
        }
        
        return blocks;
    }
    
    /**
     * Count operations in a method
     */
    public OperationSummary countOperations(MethodDeclaration method) {
        OperationSummary summary = new OperationSummary();
        
        Optional<BlockStmt> body = method.getBody();
        if (!body.isPresent()) {
            return summary;
        }
        
        BlockStmt blockStmt = body.get();
        
        // Count conditionals
        summary.setConditionals(blockStmt.findAll(IfStmt.class).size() + 
                               blockStmt.findAll(SwitchStmt.class).size());
        
        // Count loops
        summary.setLoops(blockStmt.findAll(ForStmt.class).size() +
                        blockStmt.findAll(ForEachStmt.class).size() +
                        blockStmt.findAll(WhileStmt.class).size() +
                        blockStmt.findAll(DoStmt.class).size());
        
        // Count method calls
        summary.setMethodCalls(blockStmt.findAll(MethodCallExpr.class).size());
        
        // Count assignments
        summary.setAssignments(blockStmt.findAll(AssignExpr.class).size() +
                              blockStmt.findAll(VariableDeclarator.class).size());
        
        // Count returns
        summary.setReturns(blockStmt.findAll(ReturnStmt.class).size());
        
        // Count exception handling
        summary.setExceptionHandling(blockStmt.findAll(TryStmt.class).size() +
                                    blockStmt.findAll(ThrowStmt.class).size());
        
        // Count lambdas
        summary.setLambdas(blockStmt.findAll(LambdaExpr.class).size());
        
        // Count stream operations (approximate)
        int streamOps = 0;
        for (MethodCallExpr methodCall : blockStmt.findAll(MethodCallExpr.class)) {
            String methodName = methodCall.getNameAsString();
            if (isStreamOperation(methodName)) {
                streamOps++;
            }
        }
        summary.setStreams(streamOps);
        
        return summary;
    }
    
    /**
     * Extract a statement into a CodeBlock
     */
    private CodeBlock extractStatement(Statement stmt) {
        int lineNumber = stmt.getBegin().map(pos -> pos.line).orElse(0);
        
        if (stmt instanceof ExpressionStmt) {
            return extractExpressionStatement((ExpressionStmt) stmt, lineNumber);
        } else if (stmt instanceof IfStmt) {
            return extractConditional((IfStmt) stmt, lineNumber);
        } else if (stmt instanceof ForStmt || stmt instanceof ForEachStmt || 
                   stmt instanceof WhileStmt || stmt instanceof DoStmt) {
            return extractLoop(stmt, lineNumber);
        } else if (stmt instanceof ReturnStmt) {
            return extractReturn((ReturnStmt) stmt, lineNumber);
        } else if (stmt instanceof TryStmt) {
            return extractTryCatch((TryStmt) stmt, lineNumber);
        } else if (stmt instanceof SwitchStmt) {
            return extractSwitch((SwitchStmt) stmt, lineNumber);
        }
        
        return null;
    }
    
    /**
     * Extract expression statement (assignments, method calls, etc.)
     */
    private CodeBlock extractExpressionStatement(ExpressionStmt stmt, int lineNumber) {
        Expression expr = stmt.getExpression();
        
        if (expr instanceof VariableDeclarationExpr) {
            VariableDeclarationExpr varDecl = (VariableDeclarationExpr) expr;
            CodeBlock block = new CodeBlock("VARIABLE_DECLARATION", lineNumber);
            block.setCode(simplifyCode(stmt.toString()));
            block.setDescription("Declare variable(s)");
            return block;
        } else if (expr instanceof AssignExpr) {
            CodeBlock block = new CodeBlock("ASSIGNMENT", lineNumber);
            block.setCode(simplifyCode(stmt.toString()));
            block.setDescription("Assign value to variable");
            return block;
        } else if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) expr;
            CodeBlock block = new CodeBlock("METHOD_CALL", lineNumber);
            block.setCode(simplifyCode(stmt.toString()));
            block.setMethodName(methodCall.getNameAsString());
            
            // Check if it's a stream operation
            if (isStreamOperation(methodCall.getNameAsString())) {
                block.setStreamOperation(methodCall.getNameAsString());
                block.setDescription("Stream operation: " + methodCall.getNameAsString());
            } else {
                block.setDescription("Call method: " + methodCall.getNameAsString());
            }
            
            return block;
        }
        
        return null;
    }
    
    /**
     * Extract conditional (if/else)
     */
    private CodeBlock extractConditional(IfStmt ifStmt, int lineNumber) {
        CodeBlock block = new CodeBlock("CONDITIONAL", lineNumber);
        block.setCondition(ifStmt.getCondition().toString());
        block.setCode(simplifyCode("if (" + ifStmt.getCondition() + ")"));
        block.setDescription("Check condition: " + simplifyCondition(ifStmt.getCondition().toString()));
        
        // Extract then blocks
        Statement thenStmt = ifStmt.getThenStmt();
        if (thenStmt instanceof BlockStmt) {
            BlockStmt thenBlock = (BlockStmt) thenStmt;
            for (Statement stmt : thenBlock.getStatements()) {
                CodeBlock thenCodeBlock = extractStatement(stmt);
                if (thenCodeBlock != null) {
                    block.addThenBlock(thenCodeBlock);
                }
            }
        } else {
            CodeBlock thenCodeBlock = extractStatement(thenStmt);
            if (thenCodeBlock != null) {
                block.addThenBlock(thenCodeBlock);
            }
        }
        
        // Extract else blocks
        if (ifStmt.getElseStmt().isPresent()) {
            Statement elseStmt = ifStmt.getElseStmt().get();
            if (elseStmt instanceof BlockStmt) {
                BlockStmt elseBlock = (BlockStmt) elseStmt;
                for (Statement stmt : elseBlock.getStatements()) {
                    CodeBlock elseCodeBlock = extractStatement(stmt);
                    if (elseCodeBlock != null) {
                        block.addElseBlock(elseCodeBlock);
                    }
                }
            } else if (!(elseStmt instanceof IfStmt)) {
                // Don't add else-if as else block, it will be extracted separately
                CodeBlock elseCodeBlock = extractStatement(elseStmt);
                if (elseCodeBlock != null) {
                    block.addElseBlock(elseCodeBlock);
                }
            }
        }
        
        return block;
    }
    
    /**
     * Extract loop
     */
    private CodeBlock extractLoop(Statement stmt, int lineNumber) {
        CodeBlock block = new CodeBlock("LOOP", lineNumber);
        
        if (stmt instanceof ForStmt) {
            ForStmt forStmt = (ForStmt) stmt;
            block.setLoopType("for");
            block.setCode(simplifyCode("for (...)"));
            block.setDescription("For loop");
        } else if (stmt instanceof ForEachStmt) {
            ForEachStmt forEachStmt = (ForEachStmt) stmt;
            block.setLoopType("forEach");
            block.setIterator(forEachStmt.getVariable().toString());
            block.setCode(simplifyCode("for (" + forEachStmt.getVariable() + " : " + forEachStmt.getIterable() + ")"));
            block.setDescription("For-each loop over " + forEachStmt.getIterable());
        } else if (stmt instanceof WhileStmt) {
            WhileStmt whileStmt = (WhileStmt) stmt;
            block.setLoopType("while");
            block.setCondition(whileStmt.getCondition().toString());
            block.setCode(simplifyCode("while (" + whileStmt.getCondition() + ")"));
            block.setDescription("While loop");
        } else if (stmt instanceof DoStmt) {
            block.setLoopType("do-while");
            block.setCode(simplifyCode("do { ... } while (...)"));
            block.setDescription("Do-while loop");
        }
        
        return block;
    }
    
    /**
     * Extract return statement
     */
    private CodeBlock extractReturn(ReturnStmt returnStmt, int lineNumber) {
        CodeBlock block = new CodeBlock("RETURN", lineNumber);
        
        if (returnStmt.getExpression().isPresent()) {
            Expression expr = returnStmt.getExpression().get();
            block.setCode(simplifyCode("return " + expr.toString()));
            block.setDescription("Return value");
            
            // Check for lambdas in return
            if (expr.findAll(LambdaExpr.class).size() > 0) {
                block.setLambdaExpression(expr.toString());
            }
        } else {
            block.setCode("return");
            block.setDescription("Return void");
        }
        
        return block;
    }
    
    /**
     * Extract try-catch block
     */
    private CodeBlock extractTryCatch(TryStmt tryStmt, int lineNumber) {
        CodeBlock block = new CodeBlock("TRY_CATCH", lineNumber);
        block.setCode(simplifyCode("try { ... } catch (...)"));
        
        String exceptions = tryStmt.getCatchClauses().stream()
            .map(c -> c.getParameter().getType().asString())
            .collect(Collectors.joining(", "));
        
        block.setDescription("Try-catch handling: " + exceptions);
        
        return block;
    }
    
    /**
     * Extract switch statement
     */
    private CodeBlock extractSwitch(SwitchStmt switchStmt, int lineNumber) {
        CodeBlock block = new CodeBlock("CONDITIONAL", lineNumber);
        block.setCode(simplifyCode("switch (" + switchStmt.getSelector() + ")"));
        block.setDescription("Switch on: " + switchStmt.getSelector());
        block.setCondition(switchStmt.getSelector().toString());
        
        return block;
    }
    
    /**
     * Simplify code string (remove extra whitespace, limit length)
     */
    private String simplifyCode(String code) {
        if (code == null) {
            return "";
        }
        
        // Remove extra whitespace
        code = code.replaceAll("\\s+", " ").trim();
        
        // Limit length
        if (code.length() > MAX_CODE_LENGTH) {
            code = code.substring(0, MAX_CODE_LENGTH) + "...";
        }
        
        return code;
    }
    
    /**
     * Simplify condition for description
     */
    private String simplifyCondition(String condition) {
        if (condition.length() > 50) {
            return condition.substring(0, 50) + "...";
        }
        return condition;
    }
    
    /**
     * Check if method name is a stream operation
     */
    private boolean isStreamOperation(String methodName) {
        return methodName.equals("stream") || methodName.equals("map") || 
               methodName.equals("filter") || methodName.equals("collect") ||
               methodName.equals("reduce") || methodName.equals("forEach") ||
               methodName.equals("flatMap") || methodName.equals("sorted") ||
               methodName.equals("distinct") || methodName.equals("limit") ||
               methodName.equals("skip") || methodName.equals("peek") ||
               methodName.equals("anyMatch") || methodName.equals("allMatch") ||
               methodName.equals("noneMatch") || methodName.equals("findFirst") ||
               methodName.equals("findAny") || methodName.equals("count");
    }
}
