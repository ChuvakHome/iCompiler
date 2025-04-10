package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public class TypeDeclarationASTNode extends ASTNode {
	private Token token;
	private String typename;
	private VarType type;
	
	public TypeDeclarationASTNode(ASTNode parentNode, Token token, String typename, VarType type) {
		super(parentNode, ASTNodeType.TYPE_ALIAS_DECL_NODE);
		
		this.token = token;
		this.typename = typename;
		this.type = type;
	}
	
	public Token getToken() {
		return token;
	}
	
	public String getTypename() {
		return typename;
	}
	
	public VarType getType() {
		return type;
	}
	
	public String toString() {
		return String.format("%s[alias = %s, type = %s]", getNodeType(), typename, type);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
