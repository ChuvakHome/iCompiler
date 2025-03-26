package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.semantic.VarType;

public class VariableDeclarationASTNode extends ASTNode {
	private VarType varType;
	private String varName;
	
	public VariableDeclarationASTNode(ASTNode parentNode, VarType type, String variable) {
		super(parentNode, ASTNodeType.VAR_DECL_NODE);
		this.varType = type;
		this.varName = variable;
	}
	
	public VarType getVarType() {
		return varType;
	}
	
	public String getVarName() {
		return varName;
	}
	
	public String toString() {
		return String.format("%s[var = %s, type = %s]", getNodeType(), varName, varType);
	}
}
