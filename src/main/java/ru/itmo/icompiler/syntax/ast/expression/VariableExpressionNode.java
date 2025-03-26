package ru.itmo.icompiler.syntax.ast.expression;

import ru.itmo.icompiler.syntax.ast.ASTNode;

public class VariableExpressionNode extends ExpressionASTNode {
	private String variable;
	
	public VariableExpressionNode(ASTNode parentNode, String variable) {
		super(parentNode, ExpressionNodeType.VARIABLE_EXPR_NODE);
		this.variable = variable;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public String toString() {
		return String.format("%s::%s{var = %s}",
					getNodeType(), getExpressionNodeType(),
					variable
				);
	}
}
