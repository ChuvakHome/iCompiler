package ru.itmo.icompiler.syntax;

import ru.itmo.icompiler.syntax.expression.ExpressionASTNode;

public class IfElseStatementASTNode extends ASTNode {
	private ExpressionASTNode conditionExprNode;
	private ASTNode trueBranchNode;
	private ASTNode elseBranchNode;
	
	public IfElseStatementASTNode(ASTNode parentNode, ExpressionASTNode condition, ASTNode trueBranch, ASTNode elseBranch) {
		super(parentNode, ASTNodeType.IF_ELSE_STMT_NODE);

		conditionExprNode = condition;
		trueBranchNode = trueBranch;
		elseBranchNode = elseBranch;
	}
	
	public ExpressionASTNode getConditionExpression() {
		return conditionExprNode;
	}
	
	public ASTNode getTrueBranch() {
		return trueBranchNode;
	}
	
	public ASTNode getElseBranch() {
		return elseBranchNode;
	}
}
