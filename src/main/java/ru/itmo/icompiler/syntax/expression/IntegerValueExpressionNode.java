package ru.itmo.icompiler.syntax.expression;

import ru.itmo.icompiler.syntax.ASTNode;

public class IntegerValueExpressionNode extends ExpressionASTNode {
	private int value;
	
	public IntegerValueExpressionNode(ASTNode parentNode, int value) {
		super(parentNode, ExpressionNodeType.INTEGER_VALUE_EXPR_NODE);
		
		this.value = value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public String toString() {
		return String.format("%s::%s{value = %d}",
					getNodeType(), getExpressionNodeType(),
					value
				);
	}
}
