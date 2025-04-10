package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public class RoutineDefinitionASTNode extends ASTNode {
	private RoutineDeclarationASTNode routineDecl;
	private ASTNode body;
	
	public RoutineDefinitionASTNode(ASTNode parentNode, RoutineDeclarationASTNode routineDeclaration, ASTNode body) {
		super(parentNode, ASTNodeType.ROUTINE_DEF_NODE);
		
		this.routineDecl = routineDeclaration;
		this.body = body;
	}
	
	public RoutineDeclarationASTNode getRoutineDeclaration() {
		return routineDecl;
	}
	
	public ASTNode getBody() {
		return body;
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(
				"%s[%sdecl = %s,%sbody = %s]", 
				ASTNodeType.ROUTINE_DEF_NODE, 
				sep, routineDecl.toString(tabs + 1), 
				sep, body.toString(tabs + 1)
			);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
