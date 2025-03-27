package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class WhileStatementASTNode extends LoopStatementASTNode {
	private ExpressionASTNode conditionExprNode;
	
	public WhileStatementASTNode(ASTNode parentNode, ExpressionASTNode condition, ASTNode body) {
		super(parentNode, ASTNodeType.WHILE_LOOP_NODE, body);

		conditionExprNode = condition;
		addChild(conditionExprNode);
	}
	
	public WhileStatementASTNode(ASTNode parentNode, ExpressionASTNode condition) {
		this(parentNode, condition, new CompoundStatementASTNode(null));
	}
	
	public ExpressionASTNode getConditionExpression() {
		return conditionExprNode;
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
			
		return String.format(
				"%s[%scondition = %s,%sbody = %s]", 
				getNodeType(),
				sep, conditionExprNode != null ? conditionExprNode.toString(tabs + 1) : "<none>",
				sep, bodyNode.toString(tabs + 1)
			);
	}
}
