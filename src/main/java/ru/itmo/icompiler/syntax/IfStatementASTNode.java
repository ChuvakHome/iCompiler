package ru.itmo.icompiler.syntax;

import ru.itmo.icompiler.syntax.expression.ExpressionASTNode;

public class IfStatementASTNode extends ASTNode {
	private ExpressionASTNode conditionExprNode;
	private ASTNode trueBranchNode;
	
	public IfStatementASTNode(ASTNode parentNode, ExpressionASTNode condition, ASTNode trueBranch) {
		super(parentNode, ASTNodeType.IF_STMT_NODE);

		conditionExprNode = condition;
		trueBranchNode = trueBranch;
	}
	
	public ExpressionASTNode getConditionExpression() {
		return conditionExprNode;
	}
	
	public ASTNode getTrueBranch() {
		return trueBranchNode;
	}
}
