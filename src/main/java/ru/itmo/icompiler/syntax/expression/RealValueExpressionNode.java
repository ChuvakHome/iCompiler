package ru.itmo.icompiler.syntax.expression;

import java.util.Locale;

import ru.itmo.icompiler.syntax.ASTNode;

public class RealValueExpressionNode extends ExpressionASTNode {
	private float value;
	
	public RealValueExpressionNode(ASTNode parentNode, float value) {
		super(parentNode, ExpressionNodeType.REAL_VALUE_EXPR_NODE);
		
		this.value = value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
	
	public String toString() {
		return String.format(Locale.ENGLISH, "%s::%s{value = %f}",
					getNodeType(), getExpressionNodeType(),
					value
				);
	}
}
