package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class ForInRangeStatementASTNode extends LoopStatementASTNode {
	private String iterVar;
	private ExpressionASTNode fromExpr;
	private ExpressionASTNode toExpr;
	private boolean reversed;
	
	public ForInRangeStatementASTNode(ASTNode parentNode, String iterVariable, ExpressionASTNode fromExpr, ExpressionASTNode toExpr, boolean reversed, CompoundStatementASTNode bodyNode) {
		super(parentNode, ASTNodeType.FOR_IN_RANGE_LOOP_NODE, bodyNode);
		
		this.iterVar = iterVariable;
		this.fromExpr = fromExpr;
		this.toExpr = toExpr;
		this.reversed = reversed;
	}

	public String getIterVariable() {
		return iterVar;
	}
	
	public void setFromExpression(ExpressionASTNode fromExpression) {
		this.fromExpr = fromExpression;
	}
	
	public ExpressionASTNode getFromExpression() {
		return fromExpr;
	}
	
	public ExpressionASTNode getToExpression() {
		return toExpr;
	}
	
	public void setToExpression(ExpressionASTNode toExpression) {
		this.toExpr = toExpression;
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
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	@Override
    public Token getToken() {
        return fromExpr.getToken();
    }
}
