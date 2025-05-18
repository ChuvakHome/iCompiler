package ru.itmo.icompiler.semantic.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.DeadCodeSemanticException;
import ru.itmo.icompiler.semantic.exception.NoReturnSemanticException;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.BreakStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForEachStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForInRangeStatementASTNode;
import ru.itmo.icompiler.syntax.ast.IfThenElseStatementASTNode;
import ru.itmo.icompiler.syntax.ast.PrintStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ReturnStatementASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDefinitionASTNode;
import ru.itmo.icompiler.syntax.ast.TypeDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.VariableAssignmentASTNode;
import ru.itmo.icompiler.syntax.ast.VariableDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.WhileStatementASTNode;

public class CFGASTVisitor extends AbstractASTVisitor {

    private HashMap<ASTNode, ArrayList<ASTNode>> cfg;
    private ArrayList<ASTNode> parents;
    private ArrayList<ASTNode> breaks;

    public CFGASTVisitor(AbstractExpressionASTVisitor expressionisitor) {
        super(expressionisitor);
        cfg = null;
        parents = null;
        breaks = null;
    }

    @Override
    public SemanticContext visit(RoutineDefinitionASTNode node, SemanticContext ctx) {
        cfg = new HashMap<>();
        parents = new ArrayList<>();
        parents.add(node.getRoutineDeclaration());

        node.getBody().accept(this, ctx);

        VarType result = node.getRoutineDeclaration().getResultType();
        if (result != VarType.VOID_TYPE) {
            HashMap<ASTNode, ArrayList<ASTNode>> reverseCfg = new HashMap<>();

            for (Map.Entry<ASTNode, ArrayList<ASTNode>> en : cfg.entrySet()) {
                ASTNode nodeTo = en.getKey();
                ArrayList<ASTNode> val = en.getValue();

                if (reverseCfg.get(nodeTo) == null) {
                    reverseCfg.put(nodeTo, new ArrayList<>());
                }

                for (ASTNode nodeFrom : val) {
                    ArrayList<ASTNode> list = reverseCfg.get(nodeFrom);
                    if (list == null) {
                        list = new ArrayList<>();
                        reverseCfg.put(nodeFrom, list);
                    }
                    list.add(nodeTo);
                }
            }

            for (Map.Entry<ASTNode, ArrayList<ASTNode>> en : reverseCfg.entrySet()) {
                ASTNode key = en.getKey();
                ArrayList<ASTNode> val = en.getValue();

                if (key instanceof ReturnStatementASTNode) {
                    continue;
                }

                if (val != null && !val.isEmpty()) {
                    continue;
                }

                ctx.addCompilerError(new NoReturnSemanticException(node.getRoutineDeclaration().getToken().lineNumber, node.getRoutineDeclaration().getToken().lineOffset));
                break;
            }
        }

        for (Map.Entry<ASTNode, ArrayList<ASTNode>> en : cfg.entrySet()) {
            ASTNode key = en.getKey();
            ArrayList<ASTNode> val = en.getValue();

            if (val != null && !val.isEmpty()) {
                continue;
            }

            ctx.addCompilerError(new DeadCodeSemanticException(key.getLineNumber(), key.getLineOffset()));
        }

        cfg = null;
        return ctx;
    }

    @Override
    public SemanticContext visit(IfThenElseStatementASTNode node, SemanticContext ctx) {
        ASTNode condition = node.getConditionExpression();
        cfg.put(condition, parents);

        parents = new ArrayList<>();
        parents.add(condition);
        node.getTrueBranch().accept(this, ctx);

        ASTNode elseBranch = node.getElseBranch();

        if (elseBranch != null) {
            ArrayList<ASTNode> newParents = parents;

            parents = new ArrayList<>();
            parents.add(condition);
            node.getElseBranch().accept(this, ctx);

            parents.addAll(newParents);
        } else {
            parents.add(condition);
        }

        return ctx;
    }

    @Override
    public SemanticContext visit(ForInRangeStatementASTNode node, SemanticContext ctx) {
        ASTNode cycle = node;
        cfg.put(cycle, parents);

        parents = new ArrayList<>();
        parents.add(cycle);

        ArrayList<ASTNode> oldBreaks = breaks;
        breaks = new ArrayList<>();

        node.getBody().accept(this, ctx);

        cfg.get(cycle).addAll(parents);

        parents = new ArrayList<>();
        parents.add(cycle);
        parents.addAll(breaks);

        breaks = oldBreaks;

        return ctx;
    }

    @Override
    public SemanticContext visit(ForEachStatementASTNode node, SemanticContext ctx) {
        ASTNode cycle = node;
        cfg.put(cycle, parents);

        parents = new ArrayList<>();
        parents.add(cycle);

        ArrayList<ASTNode> oldBreaks = breaks;
        breaks = new ArrayList<>();

        node.getBody().accept(this, ctx);

        cfg.get(cycle).addAll(parents);

        parents = new ArrayList<>();
        parents.add(cycle);
        parents.addAll(breaks);

        breaks = oldBreaks;

        return ctx;
    }

    @Override
    public SemanticContext visit(WhileStatementASTNode node, SemanticContext ctx) {
        ASTNode cycle = node;
        cfg.put(cycle, parents);

        parents = new ArrayList<>();
        parents.add(cycle);

        ArrayList<ASTNode> oldBreaks = breaks;
        breaks = new ArrayList<>();

        node.getBody().accept(this, ctx);

        cfg.get(cycle).addAll(parents);

        parents = new ArrayList<>();
        parents.add(cycle);
        parents.addAll(breaks);

        breaks = oldBreaks;

        return ctx;
    }

    @Override
    public SemanticContext visit(BreakStatementASTNode node, SemanticContext ctx) {
        cfg.put(node, parents);
        breaks.add(node);

        parents = new ArrayList<>();

        return ctx;
    }

    @Override
    public SemanticContext visit(VariableDeclarationASTNode node, SemanticContext ctx) {
        if (cfg == null) {
            // Not in routine
            return ctx;
        }
        
        cfg.put(node, parents);

        parents = new ArrayList<>();
        parents.add(node);

        return ctx;
    }

    @Override
    public SemanticContext visit(VariableAssignmentASTNode node, SemanticContext ctx) {
        if (cfg == null) {
            // Not in routine
            return ctx;
        }

        cfg.put(node, parents);

        parents = new ArrayList<>();
        parents.add(node);

        return ctx;
    }

    @Override
    public SemanticContext visit(TypeDeclarationASTNode node, SemanticContext ctx) {
        // Ignore
        return ctx;
    }

    @Override
    public SemanticContext visit(RoutineDeclarationASTNode node, SemanticContext ctx) {
        // Ignore
        return ctx;
    }

    @Override
    public SemanticContext visit(ReturnStatementASTNode node, SemanticContext ctx) {
        cfg.put(node, parents);

        parents = new ArrayList<>();

        return ctx;
    }

    @Override
    public SemanticContext visit(PrintStatementASTNode node, SemanticContext ctx) {
        cfg.put(node, parents);

        parents = new ArrayList<>();
        parents.add(node);

        return ctx;
    }
}
