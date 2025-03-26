package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.semantic.VarType;

public class TypeDeclarationASTNode extends ASTNode {
	private String typename;
	private VarType type;
	
	public TypeDeclarationASTNode(ASTNode parentNode, String typename, VarType type) {
		super(parentNode, ASTNodeType.TYPE_ALIAS_DECL_NODE);
		this.typename = typename;
		this.type = type;
	}
	
	public String getTypename() {
		return typename;
	}
	
	public VarType getType() {
		return type;
	}
}
