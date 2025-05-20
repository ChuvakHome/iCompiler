package ru.itmo.icompiler.semantic.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.CompoundStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ProgramASTNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public abstract class AbstractASTVisitor implements ASTVisitor<SemanticContext, SemanticContext> {
	private Stack<Map<String, int[]>> definitionsInfo = new Stack<>();
	
	protected AbstractExpressionASTVisitor expressionVisitor;
	
	public AbstractASTVisitor(AbstractExpressionASTVisitor expressionisitor) {
		this.expressionVisitor = expressionisitor;
		
		definitionsInfo.add(new HashMap<>());
	}
	
	protected void openScope() {
		definitionsInfo.push(new HashMap<>());
	}
	
	protected void addDefinitionInfo(String name, int[] lines) {
		definitionsInfo.peek().put(name, lines);
	}
	
	protected int[] lookupDefinitionInfo(String name) {
		return definitionsInfo.peek().get(name);
	}
	
	protected void closeScope() {
		definitionsInfo.pop();
	}
	
	public SemanticContext visit(ProgramASTNode node, SemanticContext ctx) {
		for (ASTNode child: node.getChildren())
			child.accept(this, ctx);
		
		return ctx;
	}
	
	public SemanticContext visit(CompoundStatementASTNode node, SemanticContext ctx) {
		for (ASTNode child: node.getChildren())
			ctx = child.accept(this, ctx);
		
		return ctx;
	}
	
	public SemanticContext visit(ExpressionASTNode node, SemanticContext ctx) {
		try {
			node.validate(ctx);
			
			node.accept(expressionVisitor, ctx);
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
		}
		
		return ctx;
	}
}
