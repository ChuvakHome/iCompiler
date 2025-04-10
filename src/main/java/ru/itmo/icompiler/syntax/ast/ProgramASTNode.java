package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public class ProgramASTNode extends ASTNode {
	public ProgramASTNode() {
		super(null, ASTNodeType.PROGRAM_START_NODE);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
