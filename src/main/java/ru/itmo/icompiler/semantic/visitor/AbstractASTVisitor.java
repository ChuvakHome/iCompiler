package ru.itmo.icompiler.semantic.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.CompoundStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ProgramASTNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BooleanValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;
import ru.itmo.icompiler.syntax.ast.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RealValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RoutineCallExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;

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
			
			switch (node.getExpressionNodeType()) {
				case BOOLEAN_VALUE_EXPR_NODE: 
					return expressionVisitor.visit((BooleanValueExpressionNode) node, ctx);
				case INTEGER_VALUE_EXPR_NODE:
					return expressionVisitor.visit((IntegerValueExpressionNode) node, ctx);
				case REAL_VALUE_EXPR_NODE:
					return expressionVisitor.visit((RealValueExpressionNode) node, ctx);
				case VARIABLE_EXPR_NODE:
					return expressionVisitor.visit((VariableExpressionNode) node, ctx);
				case FUN_CALL_EXPR_NODE:
					return expressionVisitor.visit((RoutineCallExpressionNode) node, ctx);
				case PROPERTY_ACCESS_EXPR_NODE:
					return expressionVisitor.visit((PropertyAccessExpressionNode) node, ctx);
				case UNOP_EXPR_NODE:
					return expressionVisitor.visit((UnaryOperatorExpressionNode) node, ctx);
				case BINOP_EXPR_NODE:
					return expressionVisitor.visit((BinaryOperatorExpressionNode) node, ctx);
				default:
					return ctx;
			}
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
		}
		
		return ctx;
	}
}
