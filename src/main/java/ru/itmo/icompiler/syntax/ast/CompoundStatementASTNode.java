package ru.itmo.icompiler.syntax.ast;

public class CompoundStatementASTNode extends ASTNode {
	public CompoundStatementASTNode(ASTNode parentNode) {
		super(parentNode, ASTNodeType.COMPOUND_STMT_NODE);
	}
}
