package ru.itmo.icompiler.syntax.ast;

public class BreakStatementASTNode extends ASTNode {
	private LoopStatementASTNode loopNode;
	
	public BreakStatementASTNode(ASTNode parentNode) {
		super(parentNode, ASTNodeType.BREAK_STMT_NODE);
	}
	
	public void setLoopNode(LoopStatementASTNode loopNode) {
		this.loopNode = loopNode;
	}
	
	public LoopStatementASTNode getLoopNode() {
		return loopNode;
	}
}
