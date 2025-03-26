package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class VariableAssignmentASTNode extends ASTNode {
	private String variable;
	private ExpressionASTNode valueNode;
	
	public VariableAssignmentASTNode(ASTNode parentNode, String variable, ExpressionASTNode valueNode) {
		super(parentNode, ASTNodeType.VAR_ASSIGN_NODE);
		
		this.variable = variable;
		this.valueNode = valueNode;
	}
	
	public ExpressionASTNode getValueNode() {
		return this.valueNode;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public String toString(int tabs) {
		return String.format("%s[var = %s, expr = %s]", 
					getNodeType(),
					variable,
					valueNode.toString(tabs + 1)
				);
	}
}
