package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class IfThenElseStatementASTNode extends ASTNode {
	private ExpressionASTNode conditionExprNode;
	private ASTNode trueBranchNode;
	private ASTNode elseBranchNode;
	
	public IfThenElseStatementASTNode(ASTNode parentNode, ExpressionASTNode condition, ASTNode trueBranch, ASTNode elseBranch) {
		super(parentNode, ASTNodeType.IF_ELSE_STMT_NODE);

		conditionExprNode = condition;
		addChild(conditionExprNode);
		
		trueBranchNode = trueBranch;
		addChild(trueBranchNode);
		
		elseBranchNode = elseBranch;
		addChild(elseBranchNode);
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
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
			
		return elseBranchNode != null 
				? String.format(
							"%s[%scondition = %s,%sbranch1 = %s,%sbranch0 = %s]",
							getNodeType(),
							sep, conditionExprNode != null ? conditionExprNode.toString(tabs + 1) : "<none>",
							sep, trueBranchNode.toString(tabs + 1),
							sep, elseBranchNode.toString(tabs + 1)
						)
				: String.format(
							"%s[%scondition = %s,%sbranch1 = %s]", 
							getNodeType(),
							sep, conditionExprNode != null ? conditionExprNode.toString(tabs + 1) : "<none>",
							sep, trueBranchNode.toString(tabs + 1)
						);
	}
}
