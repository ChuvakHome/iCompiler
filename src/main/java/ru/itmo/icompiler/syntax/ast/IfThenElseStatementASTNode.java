package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class IfThenElseStatementASTNode extends ASTNode {
	private ExpressionASTNode conditionExprNode;
	private ASTNode trueBranchNode;
	private ASTNode elseBranchNode;
	
	public IfThenElseStatementASTNode(ASTNode parentNode, ExpressionASTNode condition, ASTNode trueBranch, ASTNode elseBranch) {
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
