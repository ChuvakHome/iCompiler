package ru.itmo.icompiler.syntax.ast.expression;

import ru.itmo.icompiler.syntax.ast.ASTNode;

public class BooleanValueExpressionNode extends ExpressionASTNode {
	private boolean value;
	
	public BooleanValueExpressionNode(ASTNode parentNode, boolean value) {
		super(parentNode, ExpressionNodeType.BOOLEAN_VALUE_EXPR_NODE);
		
		this.value = value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public String toString() {
		return String.format("%s::%s{value = %s}",
					getNodeType(), getExpressionNodeType(),
					value
				);
	}
}
