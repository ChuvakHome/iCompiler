package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public class VariableDeclarationASTNode extends ASTNode {
	private VarType varType;
	private String varName;
	private Token token;
	
	public VariableDeclarationASTNode(ASTNode parentNode, VarType type, Token token, String variable) {
		super(parentNode, ASTNodeType.VAR_DECL_NODE);
		this.varType = type;
		this.varName = variable;
		this.token = token;
	}
	
	public void setVarType(VarType varType) {
		this.varType = varType;
	}
	
	public VarType getVarType() {
		return varType;
	}
	
	public String getVarName() {
		return varName;
	}
	
	public Token getToken() {
		return token;
	}
	
	public String toString() {
		return String.format("%s[var = %s, type = %s]", getNodeType(), varName, varType);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
