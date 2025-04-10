package ru.itmo.icompiler.syntax.ast;

public abstract class LoopStatementASTNode extends ASTNode {
	protected CompoundStatementASTNode bodyNode;
	
	public LoopStatementASTNode(ASTNode parentNode, ASTNodeType nodeType, CompoundStatementASTNode bodyNode) {
		super(parentNode, nodeType);
		
		this.bodyNode = bodyNode;
		addChild(this.bodyNode);
	}
	
	public void addBodyStatement(ASTNode statement) {
		bodyNode.addChild(statement);
	}
	
	public ASTNode getBody() {
		return bodyNode;
	}
}
