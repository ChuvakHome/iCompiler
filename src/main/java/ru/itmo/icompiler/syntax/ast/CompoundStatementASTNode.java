package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public class CompoundStatementASTNode extends ASTNode {
	public CompoundStatementASTNode(ASTNode parentNode) {
		super(parentNode, ASTNodeType.COMPOUND_STMT_NODE);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
