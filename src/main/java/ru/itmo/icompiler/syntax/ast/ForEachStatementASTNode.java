package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class ForEachStatementASTNode extends LoopStatementASTNode {
	private String iterVar;
	private ExpressionASTNode arrayExpr;
	private boolean reversed;
	
	public ForEachStatementASTNode(ASTNode parentNode, String iterVariable, ExpressionASTNode arrayExpr, boolean reversed, CompoundStatementASTNode bodyNode) {
		super(parentNode, ASTNodeType.FOR_EACH_LOOP_NODE, bodyNode);
		
		this.iterVar = iterVariable;
		this.arrayExpr = arrayExpr;
		this.reversed = reversed;
	}

	public String getIterVariable() {
		return iterVar;
	}
	
	public ExpressionASTNode getArrayExpression() {
		return arrayExpr;
	}
	
	public boolean isReversed() {
		return reversed;
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(
				reversed 
					? "%s[iterVar = %s, reversed range: {%sarray = %s}]%s"
					: "%s[iterVar = %s, range: {%sarray = %s}]%s",
				getNodeType(),
				iterVar,
				sep, arrayExpr != null ? arrayExpr.toString(tabs + 1) : "<none>",
				stringifyChildren(tabs + 1)
			);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
