package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class ReturnStatementASTNode extends ASTNode {
	private ExpressionASTNode resultNode;
	
	public ReturnStatementASTNode(ASTNode parentNode, ExpressionASTNode resultNode) {
		super(parentNode, ASTNodeType.RETURN_STMT_NODE);
		
		this.resultNode = resultNode;
	}
	
	public ExpressionASTNode getResultNode() {
		return resultNode;
	}
	
	public String toString(int tabs) {
		return String.format("%s[arg = %s]", getNodeType(), resultNode.toString(tabs));
	}
}
