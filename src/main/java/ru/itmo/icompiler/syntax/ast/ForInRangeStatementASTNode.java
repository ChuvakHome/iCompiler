package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class ForInRangeStatementASTNode extends LoopStatementASTNode {
	private String iterVar;
	private ExpressionASTNode fromExpr;
	private ExpressionASTNode toExpr;
	private boolean reversed;
	
	public ForInRangeStatementASTNode(ASTNode parentNode, String iterVariable, ExpressionASTNode fromExpr, ExpressionASTNode toExpr, boolean reversed, ASTNode bodyNode) {
		super(parentNode, ASTNodeType.FOR_IN_RANGE_LOOP_NODE, bodyNode);
		
		this.iterVar = iterVariable;
		this.fromExpr = fromExpr;
		this.toExpr = toExpr;
		this.reversed = reversed;
	}

	public String getIterVariable() {
		return iterVar;
	}
	
	public ExpressionASTNode getFromExpression() {
		return fromExpr;
	}
	
	public ExpressionASTNode getToExpression() {
		return toExpr;
	}
	
	public boolean isReversed() {
		return reversed;
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(
				reversed 
					? "%s[iterVar = %s, reversed range: {%sleft = %s,%sright = %s}]%s"
					: "%s[iterVar = %s, range: {%sleft = %s,%sright = %s}]%s",
				getNodeType(),
				iterVar,
				sep, fromExpr != null ? fromExpr.toString(tabs + 1) : "<none>",
				sep, toExpr != null ? toExpr.toString(tabs + 1) : "<none>",
				stringifyChildren(tabs + 1)
			);
	}
}
